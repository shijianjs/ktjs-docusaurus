package io.github.shijianjs.ktjs.dom

import kotlin.js.JsAny
import kotlin.js.JsName
import kotlin.js.js
import web.html.HTMLElement

/**
 * Base class for a custom element written in Kotlin.
 *
 * It does two things a site should not have to repeat:
 *
 * 1. Declares **all** of `web.components.CustomElement`'s lifecycle hooks as overridable methods under
 *    their native names: `connectedCallback`, `connectedMoveCallback`, `disconnectedCallback`,
 *    `adoptedCallback`, `attributeChangedCallback`. The official interface spells these as `val`s,
 *    which Kotlin compiles to getters the browser never calls, so the interface is deliberately not
 *    implemented here. Element classes just `override fun connectedCallback()` and friends.
 *
 *    `attributeChangedCallback` only fires for attributes listed in the constructor's static
 *    `observedAttributes`; declaring that is up to the element class.
 *
 * 2. Removes the self-recursive bridges Kotlin/JS emits for every external DOM member (`ParentNode`,
 *    `ChildNode`, `Animatable`, `HTMLOrSVGOrMathMLElement`, `EventTargetLike`, …):
 *
 *    ```js
 *    querySelectorAll(selectors) { return this.querySelectorAll(selectors); }
 *    ```
 *
 *    Call [stripBridgesFrom] once per element class before `customElements.define`. It walks the
 *    class's prototype chain down to (but excluding) `HTMLElement.prototype` and deletes those names,
 *    so instance lookups fall through to the native DOM methods and the normal spelling works:
 *    `querySelectorAll(...)`, `append(...)`, `animate(...)`, `focus()`, … No `…Super` forwarders and
 *    no `@JsName` aliases are needed (`@JsName` is forbidden on overridden members anyway).
 *
 * `getAttribute` / `setAttribute` / `hasAttribute` / `textContent` need no stripping: they are
 * abstract members of the external `Element` class, so no bridge is generated for them.
 */
open class CustomElementAdapter : HTMLElement() {

    @JsName("connectedCallback")
    open fun connectedCallback() {}

    @JsName("connectedMoveCallback")
    open fun connectedMoveCallback() {}

    @JsName("disconnectedCallback")
    open fun disconnectedCallback() {}

    @JsName("adoptedCallback")
    open fun adoptedCallback() {}

    @JsName("attributeChangedCallback")
    open fun attributeChangedCallback(name: String, oldValue: JsAny?, newValue: JsAny?) {}
}

/**
 * Members Kotlin/JS re-declares on a custom element class for the external DOM interfaces it inherits.
 * Every one of them is a self-recursive bridge sitting on that class's prototype.
 */
private val bridgeNames = listOf(
    "dispatchEvent", "animate", "getAnimations", "after", "before", "replaceWith", "append",
    "moveBefore", "prepend", "querySelector", "querySelectorAll", "replaceChildren", "blur", "focus",
)

/**
 * Deletes the bridges from [klass]'s prototype chain, stopping before `HTMLElement.prototype`, where
 * these names are the real DOM methods.
 *
 * Run it once per element class, before `customElements.define` and therefore before any instance
 * exists. Passing the concrete class is enough: an instance resolves a member through the whole
 * chain, so every class in it has to be cleaned — Kotlin emits a bridge on each class, not only on
 * [CustomElementAdapter].
 */
fun stripBridgesFrom(klass: dynamic) {
    val htmlPrototype: dynamic = js("HTMLElement.prototype")
    var prototype: dynamic = klass.prototype
    while (prototype != null && prototype !== htmlPrototype) {
        deleteBridges(prototype)
        prototype = js("Object.getPrototypeOf")(prototype)
    }
}

private fun deleteBridges(prototype: dynamic) {
    val deleteProperty: dynamic = js("Reflect.deleteProperty")
    for (name in bridgeNames) {
        deleteProperty(prototype, name)
    }
}
