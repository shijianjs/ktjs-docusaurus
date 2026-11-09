package example

import js.reflect.JsClass
import js.reflect.unsafeCast
import web.components.customElements
import web.console.console
import web.html.HTMLElement
import web.html.HtmlSource
import kotlin.js.JsName

/**
 * An empty external superclass that maps onto the real `HTMLElement`.
 *
 * The two failures in `MyPanel` / `MyPanelFn` both come from Kotlin re-emitting the members it inherits
 * from `HTMLElement` (as a getter for the lifecycle hook, as self-recursive forwarders for the query
 * methods). This class declares none of those members, so the compiler has nothing to re-emit: the
 * generated JS only has `class MyPanelBridge extends HTMLElement`, and every DOM call goes through the
 * native prototype.
 *
 * `@JsName` makes the reference in the emitted `extends` point at the global `HTMLElement`.
 */
@JsName("HTMLElement")
external open class BridgeHTMLElement

/** The same object seen through the official DOM wrapper: one cast, shared by every bridge class. */
val BridgeHTMLElement.html: HTMLElement get() = unsafeCast<HTMLElement>(this)

/**
 * The same panel, built on [BridgeHTMLElement].
 *
 * Kotlin does not consider this class an `HTMLElement`, so the DOM is reached through [html] — one cast,
 * after which the official `HTMLElement` API is available as usual. That is also why
 * `CustomElementReference` (bounded on `HTMLElement`) cannot be used here and the registry is called
 * directly.
 */
class MyPanelBridge : BridgeHTMLElement() {

    init {
        console.log("MyPanelBridge created!")
    }

    @JsName("connectedCallback")
    fun onConnected() {
        console.log("MyPanelBridge connected!")
        // The call that overflowed in MyPanelFn: now a plain native method call.
        console.log("MyPanelBridge spans=" + html.querySelectorAll("span").length)
        html.setAttribute("data-ok", "true")
        html.innerHTML = HtmlSource("MyPanelBridge content!")
    }

    companion object {
        const val NAME: String = "my-panel-bridge"
    }
}

/** Defines `<my-panel-bridge>`; safe to call twice. */
fun defineBridgePanel() {
    val registry = customElements.unsafeCast<ElementRegistry>()
    if (registry.get(MyPanelBridge.NAME) != null) return
    registry.define(MyPanelBridge.NAME, MyPanelBridge::class.js)
}

/** The registry slice this file needs, with a plain `String` tag name. */
internal external interface ElementRegistry {
    fun define(name: String, constructor: JsClass<*>)

    fun get(name: String): JsAny?
}
