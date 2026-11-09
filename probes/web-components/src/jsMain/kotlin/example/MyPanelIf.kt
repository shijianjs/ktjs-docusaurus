package example

import web.components.CustomElement
import web.components.customElements
import web.console.console
import web.html.HTMLElement
import js.reflect.unsafeCast

/**
 * The bridge superclass plus the official `CustomElement` interface.
 *
 * `MyPanel` shows that `override val connectedCallback` is not called when the class extends
 * `HTMLElement` directly. The question here is whether the interface itself is the problem, or whether
 * the accessor is simply lost among the members Kotlin re-emits from a DOM superclass. With
 * [BridgeHTMLElement] there are no re-emitted members, so this is a clean test of the accessor.
 */
class MyPanelIf : BridgeHTMLElement(), CustomElement {

    override val connectedCallback: () -> Unit = {
        console.log("MyPanelIf connected!")
        // Reached through the extension property, no per-class accessor needed.
        console.log("MyPanelIf spans=" + html.querySelectorAll("span").length)
        html.setAttribute("data-ok", "true")
    }

    companion object {
        const val NAME: String = "my-panel-if"
    }
}

/** Defines `<my-panel-if>`; safe to call twice. */
fun defineIfPanel() {
    val registry = customElements.unsafeCast<ElementRegistry>()
    if (registry.get(MyPanelIf.NAME) != null) return
    registry.define(MyPanelIf.NAME, MyPanelIf::class.js)
}
