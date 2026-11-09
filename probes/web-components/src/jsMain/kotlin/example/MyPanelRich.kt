package example

import web.components.customElements
import web.console.console
import web.cssom.ElementCSSInlineStyle
import web.dom.Element
import web.dom.HTMLOrSVGOrMathMLElement
import web.globals.GlobalEventHandlers
import web.html.ElementContentEditable
import web.html.HtmlSource
import kotlin.js.JsName

/**
 * The bridge superclass carrying the same interfaces as the official `HTMLElement`
 * (`web.html.HTMLElement` declares exactly these five).
 *
 * Declaring them again is what makes the DOM API available directly on `this` — no `unsafeCast` per
 * class. The question this variant answers is whether the interfaces bring back the self-recursive
 * forwarders: those come from members of the *class* it inherits, and an interface has no bodies to
 * re-emit.
 */
@JsName("HTMLElement")
external open class RichHTMLElement :
    Element,
    ElementCSSInlineStyle,
    ElementContentEditable,
    GlobalEventHandlers,
    HTMLOrSVGOrMathMLElement

/** The same panel, with the DOM API on `this`. */
class MyPanelRich : RichHTMLElement() {

    init {
        console.log("MyPanelRich created!")
    }

    @JsName("connectedCallback")
    fun onConnected() {
        console.log("MyPanelRich connected!")
        // No cast: these resolve against the interfaces above.
        console.log("MyPanelRich spans=" + querySelectorAll("span").length)
        setAttribute("data-ok", "true")
        innerHTML = HtmlSource("MyPanelRich content!")
    }

    companion object {
        const val NAME: String = "my-panel-rich"
    }
}

/** Defines `<my-panel-rich>`; safe to call twice. */
fun defineRichPanel() {
    val registry = customElements.unsafeCast<ElementRegistry>()
    if (registry.get(MyPanelRich.NAME) != null) return
    registry.define(MyPanelRich.NAME, MyPanelRich::class.js)
}
