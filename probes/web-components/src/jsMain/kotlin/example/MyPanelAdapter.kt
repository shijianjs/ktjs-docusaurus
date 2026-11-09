package example

import web.components.CustomElementReference
import web.components.customElements
import web.components.define
import web.console.console
import web.dom.TagName
import kotlin.js.JsName

/**
 * An element that follows the official spelling — `HTMLElement` superclass (via [DomAdapter]) plus a
 * `CustomElementReference` companion — while routing every DOM call through the adapter's safe
 * `…Super` methods (and `remove()`, which the adapter was able to override).
 */
class MyPanelAdapter : DomAdapter() {

    init {
        console.log("MyPanelAdapter created!")
    }

    @JsName("connectedCallback")
    fun onConnected() {
        console.log("MyPanelAdapter connected!")
        console.log("MyPanelAdapter data-x=" + getAttributeSuper("data-x"))
        console.log("MyPanelAdapter spans=" + querySelectorAllSuper("span").length)
        setAttributeSuper("data-ok", "true")
        // `remove()` was overridable, so the normal spelling works too.
        remove()
        console.log("MyPanelAdapter remove() ok")
    }

    companion object : CustomElementReference<MyPanelAdapter>(MyPanelAdapter::class.js) {
        val NAME: TagName<MyPanelAdapter> = TagName("my-panel-adapter")
    }
}

/** Defines `<my-panel-adapter>`; safe to call twice. */
fun defineAdapterPanel() {
    if (customElements.get(MyPanelAdapter.NAME) == null) {
        customElements.define(MyPanelAdapter.NAME, MyPanelAdapter.Companion)
    }
}
