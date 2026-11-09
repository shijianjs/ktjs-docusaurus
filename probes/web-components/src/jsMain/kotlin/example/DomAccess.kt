package example

import web.dom.Element
import web.dom.Node
import web.dom.NodeList
import web.html.HtmlSource

/**
 * The class-level DOM members as extensions on [BridgeHTMLElement].
 *
 * `Element`, `Node` and `EventTarget` are external *classes*, so their members cannot come from an
 * interface supertype, and an external interface cannot redeclare them either — Kotlin allows a
 * non-abstract member in an external interface only for nullable properties. Extensions are the
 * remaining option: resolved at compile time, compiled to a direct call on the receiver, and unable to
 * be forwarded.
 *
 * The interfaces that *are* interfaces (`style`, the event-handler properties of
 * `GlobalEventHandlers`, …) are available on `this` through `BridgeWithoutElement`; these cover the
 * rest. `addEventListener` has no wrapper here because kotlin-browser's typed event API needs the
 * generic form; call it through `html.addEventListener(...)` when a class needs it.
 */

// Element

fun BridgeHTMLElement.getAttribute(name: String): String? = html.getAttribute(name)

fun BridgeHTMLElement.setAttribute(name: String, value: String) {
    html.setAttribute(name, value)
}

fun BridgeHTMLElement.hasAttribute(name: String): Boolean = html.hasAttribute(name)

fun BridgeHTMLElement.removeAttribute(name: String) {
    html.removeAttribute(name)
}

fun BridgeHTMLElement.querySelector(selectors: String): Element? = html.querySelector(selectors)

fun BridgeHTMLElement.querySelectorAll(selectors: String): NodeList<Element> = html.querySelectorAll(selectors)

// Node

fun BridgeHTMLElement.append(vararg nodes: Node) {
    html.append(*nodes)
}

fun BridgeHTMLElement.appendChild(node: Node): Node = html.appendChild(node)

fun BridgeHTMLElement.removeChild(child: Node): Node = html.removeChild(child)

fun BridgeHTMLElement.remove() {
    html.remove()
}

val BridgeHTMLElement.childNodes: NodeList<Node> get() = html.childNodes

// HTMLElement

var BridgeHTMLElement.title: String
    get() = html.title
    set(value) {
        html.title = value
    }

var BridgeHTMLElement.lang: String
    get() = html.lang
    set(value) {
        html.lang = value
    }

fun BridgeHTMLElement.click() {
    html.click()
}

var BridgeHTMLElement.textContent: String?
    get() = html.textContent
    set(value) {
        html.textContent = value
    }

var BridgeHTMLElement.innerHTML: HtmlSource
    get() = html.innerHTML
    set(value) {
        html.innerHTML = value
    }
