package example

import web.dom.Element
import web.dom.Node
import web.dom.NodeList
import web.html.HTMLElement

/**
 * An adapter that keeps the official `HTMLElement` superclass and exposes the DOM operations the
 * compiler shadows with self-recursive bridges under differently named methods.
 *
 * Each `…Super` method forwards to `super.foo(...)`, which at ES2015 is a real ES `super` call and
 * therefore starts at the native DOM prototype. Consumers that hold this element call the `…Super`
 * variants instead of `this.foo(...)`.
 */
open class CustomElementAdapter : HTMLElement() {
    fun getAttributeSuper(name: String): String? = super.getAttribute(name)

    fun setAttributeSuper(name: String, value: String) {
        super.setAttribute(name, value)
    }

    fun hasAttributeSuper(name: String): Boolean = super.hasAttribute(name)

    fun removeAttributeSuper(name: String) {
        super.removeAttribute(name)
    }

    fun querySelectorSuper(selectors: String): Element? = super.querySelector(selectors)

    fun querySelectorAllSuper(selectors: String): NodeList<Element> = super.querySelectorAll(selectors)

    fun appendSuper(vararg nodes: Node) {
        super.append(*nodes)
    }

    fun removeSuper() {
        super.remove()
    }

    // Probe: can a generated self-recursive bridge be overridden in the adapter?
    override fun remove() {
        super.remove()
    }

    // Probe result (2026-11-10): `override` + `@JsName("querySelectorAllImpl")` does not work.
    //   e: '@JsName' is prohibited for overridden members.
    //   e: JavaScript name 'querySelectorAll' generated for this declaration clashes with other
    //      declarations.
    // Plain `override fun querySelectorAll(...)` clashes with the compiler-generated bridge as well,
    // so the `querySelectorAllSuper` forwarder above stays the only working spelling.



    var textContentSuper: String?
        get() = super.textContent
        set(value) {
            super.textContent = value
        }
}
