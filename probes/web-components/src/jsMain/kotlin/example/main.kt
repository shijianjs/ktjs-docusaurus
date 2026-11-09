package example

import web.components.customElements
import web.components.define
import web.console.console

fun main() {
    console.log("Web Components example app is running!")
    customElements.define(MyPanel.NAME, MyPanel)
}
