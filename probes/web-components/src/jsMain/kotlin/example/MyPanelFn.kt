package example

import web.components.CustomElementReference
import web.components.customElements
import web.components.define
import web.console.console
import web.dom.TagName
import web.html.HTMLElement
import kotlin.js.JsName

/**
 * The same panel, but with the lifecycle callback written as a real method.
 *
 * `MyPanel` follows the example's spelling — `override val connectedCallback = { … }` — which Kotlin
 * compiles to a `get connectedCallback()` accessor. The browser never calls it, so the panel renders
 * but `connectedCallback` silently does nothing.
 *
 * This variant uses `@JsName("connectedCallback") fun`, which does become a method on the prototype.
 * It gets called — and dies on `querySelectorAll`, because Kotlin emits the inherited external query
 * methods as self-recursive forwarders:
 *
 *     querySelectorAll(selectors) { return this.querySelectorAll(selectors); }
 *
 * Two ways of overriding that forwarder were tried, both fail:
 *
 * 1. `override fun querySelectorAll(...) = super.querySelectorAll(...)` does not compile —
 *    `getAttribute` is final in `Element`, and declaring `querySelectorAll` clashes with the
 *    forwarder the compiler already generated.
 * 2. Calling through `unsafeCast<HTMLElement>(this)` compiles, but still overflows at runtime: the
 *    cast changes the static type only, the object and its polluted prototype are the same.
 *
 * The only thing that works is not inheriting those members in the first place — see
 * `MyPanelBridge`, whose superclass declares no DOM members at all.
 */
class MyPanelFn : HTMLElement() {

    init {
        console.log("MyPanelFn created!")
    }

    @JsName("connectedCallback")
    fun onConnected() {
        console.log("MyPanelFn connected!")
        console.log("MyPanelFn data-x=" + getAttribute("data-x"))
        // `getAttribute` is fine, but the query methods are re-emitted as self-recursive forwarders,
        // so this is the statement that overflows:
        console.log("MyPanelFn spans=" + querySelectorAll("span").length)
    }

    companion object : CustomElementReference<MyPanelFn>(MyPanelFn::class.js) {
        val NAME: TagName<MyPanelFn> = TagName("my-panel-fn")
    }
}

/** Defines both variants; safe to call twice. */
fun defineFnPanel() {
    if (customElements.get(MyPanelFn.NAME) == null) {
        customElements.define(MyPanelFn.NAME, MyPanelFn.Companion)
    }
}
