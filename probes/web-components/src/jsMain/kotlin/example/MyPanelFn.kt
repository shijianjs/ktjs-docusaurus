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
 * Two earlier attempts failed: `override fun` does not compile (`getAttribute` is final in `Element`,
 * and `querySelectorAll` clashes with the forwarder the compiler already generated), and calling
 * through `unsafeCast<HTMLElement>(this)` still overflows because the object and its prototype are
 * unchanged.
 *
 * What does work, without changing the superclass, is calling the parent implementation directly
 * from inside the class — `super.getAttribute(...)`, `super.querySelectorAll(...)`. At ES2015 Kotlin/JS
 * emits real `super.foo(...)` inside the ES class, so lookup starts at the native DOM prototype and
 * never reaches the forwarders on `this`:
 *
 *     MyPanelFn created! / connected! / data-x=42 / spans=0
 *
 * Caveats: every DOM call in the class has to be spelled with `super.`, and `class X extends
 * HTMLElement` still evaluates `HTMLElement` at module scope — such a bundle cannot be loaded by Node
 * (see `MyPanelBridge`'s KDoc for the empty-superclass alternative).
 */
class MyPanelFn : HTMLElement() {

    init {
        console.log("MyPanelFn created!")
    }

    @JsName("connectedCallback")
    fun onConnected() {
        console.log("MyPanelFn connected!")
        // Call the parent implementation directly, bypassing the forwarders on `this`' prototype.
        console.log("MyPanelFn data-x=" + super.getAttribute("data-x"))
        console.log("MyPanelFn spans=" + super.querySelectorAll("span").length)
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
