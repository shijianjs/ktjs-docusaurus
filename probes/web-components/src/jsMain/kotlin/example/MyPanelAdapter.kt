package example

import js.array.ReadonlyArray
import kotlin.js.JsName
import kotlin.js.js
import kotlin.js.jsTypeOf
import web.animations.Keyframe
import web.components.CustomElementReference
import web.components.customElements
import web.components.define
import web.console.console
import web.dom.Node
import web.dom.TagName
import web.dom.document

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
        // Plain spelling: the bridges were deleted from the class prototype chain before define.
        console.log("MyPanelAdapter data-x=" + getAttribute("data-x"))
        console.log("MyPanelAdapter spans=" + querySelectorAll("span").length)

        // Vararg bridge (append) — the compiler forwards through `$externalVarargReceiverTmp`.
        val span: Node = document.createElement("span")
        append(*arrayOf(span))
        console.log("MyPanelAdapter append children=" + childNodes.length)

        // Overloaded bridge with an options parameter (animate).
        val keyframes: ReadonlyArray<Keyframe> = js("""[{"opacity":"0"},{"opacity":"1"}]""")
        val animation = animate(keyframes, 100.0)
        console.log("MyPanelAdapter animate=" + jsTypeOf(animation))

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
    // Static, once: strip the bridges from the class prototype chain before any instance exists.
    stripBridgesFrom(MyPanelAdapter::class.js)
    if (customElements.get(MyPanelAdapter.NAME) == null) {
        customElements.define(MyPanelAdapter.NAME, MyPanelAdapter.Companion)
    }
}
