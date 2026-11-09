package example

import web.components.customElements
import web.console.console
import kotlin.js.JsName

/**
 * Empty bridge superclass + the interface bundle that survives without `Element`.
 *
 * `Element` is an external *class*, so it cannot live in an interface; the remaining four are
 * interfaces and compile. The open question is whether implementing them makes Kotlin emit the same
 * self-recursive forwarders it emits when an external class lists them.
 *
 * It reads `style` (from `ElementCSSInlineStyle`) — an interface member — and then reaches the rest of
 * the DOM through the shared `html` extension.
 */
class MyPanelMixed : BridgeHTMLElement(), BridgeWithoutElement {

    init {
        console.log("MyPanelMixed created!")
    }

    @JsName("connectedCallback")
    fun onConnected() {
        console.log("MyPanelMixed connected!")
        // Interface member: overflows if the interface is implemented with forwarders.
        console.log("MyPanelMixed style.color=" + style.color)
        // Element members are not in the interface, so they go through the extension property.
        console.log("MyPanelMixed spans=" + html.querySelectorAll("span").length)
        html.setAttribute("data-ok", "true")
    }

    companion object {
        const val NAME: String = "my-panel-mixed"
    }
}

/** Defines `<my-panel-mixed>`; safe to call twice. */
fun defineMixedPanel() {
    val registry = customElements.unsafeCast<ElementRegistry>()
    if (registry.get(MyPanelMixed.NAME) != null) return
    registry.define(MyPanelMixed.NAME, MyPanelMixed::class.js)
}
