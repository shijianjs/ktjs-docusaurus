package io.github.shijianjs.ktjs.docusaurus.kit

import js.reflect.unsafeCast
import web.html.HTMLElement

/**
 * A section that can read a host element into itself.
 *
 * This is the adapter interface: `defineElement` supplies a JS class and forwards
 * `connectedCallback`, [bindHomeSection] owns the host → section relationship, and each section only
 * has to know how to read its own declarative input.
 */
interface HomeElementBinding {
    /** Reads the host's attributes and child tags into this section. Called on every connect. */
    fun fillFrom(host: HTMLElement)
}

/**
 * The section bound to a host, stored **on the element itself**.
 *
 * One field, polymorphic value: a host is only ever one kind of section. Keeping it on the element
 * (rather than in a map keyed by it) means the section is released together with the element, and a
 * second `connectedCallback` finds the very same instance instead of appending a second tree.
 */
private external interface HomeHostState {
    var ktjsSection: HomeElementBinding?
}

/**
 * Binds a section to this element: take the instance the element already owns, or build it once with
 * [factory], then let it read the host. Safe to call on every connect (StrictMode, re-mount,
 * client-side navigation).
 */
fun HTMLElement.bindHomeSection(factory: (HTMLElement) -> HomeElementBinding) {
    val state = unsafeCast<HomeHostState>(this)
    val section = state.ktjsSection ?: factory(this).also { state.ktjsSection = it }
    section.fillFrom(this)
}
