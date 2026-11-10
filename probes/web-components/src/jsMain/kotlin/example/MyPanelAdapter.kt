package example

import web.components.CustomElementReference
import web.components.customElements
import web.components.define
import web.console.console
import web.dom.TagName
import kotlin.js.JsName

/**
 * An element that follows the official spelling — `HTMLElement` superclass (via [CustomElementAdapter]) plus a
 * `CustomElementReference` companion — while routing every DOM call through the adapter's safe
 * `…Super` methods (and `remove()`, which the adapter was able to override).
 */
class MyPanelAdapter : CustomElementAdapter() {

    init {
        console.log("MyPanelAdapter created!")
    }

    @JsName("connectedCallback")
    fun onConnected() {
        console.log("MyPanelAdapter connected!")
        // Plain spelling: the bridges were deleted from the prototype by stripAdapterBridges().
        console.log("MyPanelAdapter data-x=" + getAttribute("data-x"))
        console.log("MyPanelAdapter spans=" + querySelectorAll("span").length)
        setAttribute("data-ok", "true")
        // Kept in the DOM so the probe page can assert data-ok; `remove()` is covered by other probes.
        console.log("MyPanelAdapter bridgeless ok")
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
