package example

import web.components.customElements
import web.components.define
import web.console.console

fun main() {
    console.log("Web Components example app is running!")
    // The example as published: `override val connectedCallback`, i.e. an accessor the browser never calls.
    customElements.define(MyPanel.NAME, MyPanel)
    // The same idea with a real method: called, then overflows the stack on the first DOM access.
    defineFnPanel()
    // Empty external superclass: no inherited DOM members to re-emit.
    defineBridgePanel()
    // Bridge superclass + the official CustomElement interface (val connectedCallback).
    defineIfPanel()
    // Bridge superclass that re-declares the official interfaces: DOM API on 	his, no cast.
    defineRichPanel()
    // Empty superclass + the interface bundle without Element (which is a class).
    defineMixedPanel()
}
