package io.github.shijianjs.ktjs.docusaurus.elements

import web.dom.Element
import web.dom.Node
import web.dom.NodeList
import web.html.HTMLElement

/**
 * The official `HTMLElement` superclass, with safe entry points for the DOM methods Kotlin/JS shadows.
 *
 * For members declared with `= definedExternally` on external interfaces (`ParentNode`, `ChildNode`,
 * …), the compiler emits a bridge named e.g. `querySelectorAll` whose body is
 * `return this.querySelectorAll(...)`, i.e. infinite recursion. Two remedies, both verified in
 * `probes/web-components`:
 *
 * - Members whose JS name is still free can simply be overridden (`remove()` below).
 * - The others get a differently named `…Super` method that forwards to `super.foo(...)`. At ES2015
 *   that is a real ES `super` call, which starts at the native DOM prototype and never reaches the
 *   bridge.
 *
 * Consumers holding this element use the `…Super` methods; nothing here uses `dynamic` or a JS class
 * literal.
 */
open class DomAdapter : HTMLElement() {
    fun getAttributeSuper(name: String): String? = super.getAttribute(name)

    fun setAttributeSuper(name: String, value: String) {
        super.setAttribute(name, value)
    }

    fun hasAttributeSuper(name: String): Boolean = super.hasAttribute(name)

    fun querySelectorAllSuper(selectors: String): NodeList<Element> = super.querySelectorAll(selectors)

    fun appendSuper(vararg nodes: Node) {
        super.append(*nodes)
    }

    override fun remove() {
        super.remove()
    }
}
