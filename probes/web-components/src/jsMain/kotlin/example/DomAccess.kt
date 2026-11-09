package example

import web.dom.Node
import web.dom.NodeList
import web.html.HtmlSource

/**
 * The `Element` members as extensions on [BridgeHTMLElement].
 *
 * `Element` is an external class, so it cannot join [BridgeWithoutElement]; its members are reached
 * through [html]. These extensions put the common ones on the element itself, so a site class reads
 * `getAttribute("title")` instead of `html.getAttribute("title")`.
 *
 * Extensions are resolved at compile time and compile to a plain call on the receiver, so they add no
 * members to the prototype and cannot be forwarded.
 */
fun BridgeHTMLElement.getAttribute(name: String): String? = html.getAttribute(name)

fun BridgeHTMLElement.setAttribute(name: String, value: String) {
    html.setAttribute(name, value)
}

fun BridgeHTMLElement.hasAttribute(name: String): Boolean = html.hasAttribute(name)

fun BridgeHTMLElement.querySelector(selectors: String): web.dom.Element? = html.querySelector(selectors)

fun BridgeHTMLElement.querySelectorAll(selectors: String): NodeList<web.dom.Element> = html.querySelectorAll(selectors)

fun BridgeHTMLElement.append(vararg nodes: Node) {
    html.append(*nodes)
}

fun BridgeHTMLElement.removeChild(child: Node): Node = html.removeChild(child)

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
