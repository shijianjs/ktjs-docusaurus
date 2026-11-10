package example

import js.reflect.unsafeCast
import kotlin.js.js
import web.console.console
import web.dom.Element
import web.dom.Node
import web.dom.NodeList
import web.html.HTMLElement

/**
 * An adapter that keeps the official `HTMLElement` superclass and exposes the DOM operations the
 * compiler shadows with self-recursive bridges under differently named methods.
 *
 * Each `…Super` method forwards to `super.foo(...)`, which at ES2015 is a real ES `super` call and
 * therefore starts at the native DOM prototype. Consumers that hold this element call the `…Super`
 * variants instead of `this.foo(...)`.
 */
open class CustomElementAdapter : HTMLElement() {
    init {
        // Probe: strip the bridges from the *runtime* class's prototype (the concrete subclass being
        // constructed), so instance lookups fall through to the native HTMLElement.prototype.
        val runtime: dynamic = unsafeCast<dynamic>(this)
        // `createExternalThis` wraps every class level in a fresh `class extends ctor {}` and copies
        // the Kotlin members onto its prototype, so walk the chain and clean each level - but never
        // touch `HTMLElement.prototype`, where these names are the real DOM methods.
        val htmlPrototype: dynamic = js("HTMLElement.prototype")
        var prototype: dynamic = js("Object.getPrototypeOf")(runtime)
        while (prototype != null && prototype !== htmlPrototype) {
            deleteBridges(prototype)
            prototype = js("Object.getPrototypeOf")(prototype)
        }
        console.log(
            "strip: ctor=" + runtime.constructor.name +
                " ownAfter=" + js("Object.getPrototypeOf")(runtime).hasOwnProperty("querySelectorAll"),
        )
    }

    fun getAttributeSuper(name: String): String? = super.getAttribute(name)

    fun setAttributeSuper(name: String, value: String) {
        super.setAttribute(name, value)
    }

    fun hasAttributeSuper(name: String): Boolean = super.hasAttribute(name)

    fun removeAttributeSuper(name: String) {
        super.removeAttribute(name)
    }

    fun querySelectorSuper(selectors: String): Element? = super.querySelector(selectors)

    fun querySelectorAllSuper(selectors: String): NodeList<Element> = super.querySelectorAll(selectors)

    fun appendSuper(vararg nodes: Node) {
        super.append(*nodes)
    }

    fun removeSuper() {
        super.remove()
    }

    // Probe: can a generated self-recursive bridge be overridden in the adapter?
    override fun remove() {
        super.remove()
    }

    // Probe result (2026-11-10): `override` + `@JsName("querySelectorAllImpl")` does not work.
    //   e: '@JsName' is prohibited for overridden members.
    //   e: JavaScript name 'querySelectorAll' generated for this declaration clashes with other
    //      declarations.
    // Plain `override fun querySelectorAll(...)` clashes with the compiler-generated bridge as well,
    // so the `querySelectorAllSuper` forwarder above stays the only working spelling.



    var textContentSuper: String?
        get() = super.textContent
        set(value) {
            super.textContent = value
        }
}

/**
 * Probe: delete the compiler-generated self-recursive bridges from the prototype.
 *
 * If this works, an instance lookup falls through to the native `HTMLElement.prototype`, so plain
 * `this.querySelectorAll("span")` behaves like the browser's method and no `…Super` forwarder is
 * needed. Must run before any element is created.
 */
private val bridgeNames = listOf(
    "dispatchEvent", "animate", "getAnimations", "after", "before", "replaceWith", "append",
    "moveBefore", "prepend", "querySelector", "querySelectorAll", "replaceChildren", "blur", "focus",
)

fun stripAdapterBridges() {
    deleteBridges(unsafeCast<dynamic>(CustomElementAdapter::class.js).prototype)
}

private fun deleteBridges(prototype: dynamic) {
    val deleteProperty: dynamic = js("Reflect.deleteProperty")
    for (name in bridgeNames) {
        deleteProperty(prototype, name)
    }
}
