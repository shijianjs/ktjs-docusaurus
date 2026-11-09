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
 * It gets called — and dies on its first DOM call, because Kotlin also emits every inherited external
 * member as a self-recursive forwarder:
 *
 *     getAttribute(name) { return this.getAttribute(name); }
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
