package io.github.shijianjs.ktjs.docusaurus.demo.elements

import web.html.HTMLElement

/**
 * Defines a custom element whose `connectedCallback` runs [onConnected].
 *
 * The class syntax comes from a short JavaScript bridge: a Kotlin class extending a DOM type emits
 * self-recursive bridges for the members it inherits from external interfaces (see
 * `probes/web-components`), and emits `class X extends HTMLElement` at module scope, which Node —
 * loading this site's configuration — cannot evaluate. The bridge is a function expression, so merely
 * loading this module is safe in both environments.
 *
 * All element behaviour — attribute parsing, section creation, cleanup — lives in Kotlin.
 */
private val defineElementImpl: (String, (HTMLElement) -> Unit) -> Unit = js(
    """
    function (tag, onConnected) {
      if (globalThis.customElements.get(tag)) {
        return;
      }
      globalThis.customElements.define(tag, class extends globalThis.HTMLElement {
        connectedCallback() {
          onConnected(this);
        }
      });
    }
    """,
)

/** Registers [tag]; [onConnected] runs every time an instance is connected. Safe to call twice. */
internal fun defineElement(tag: String, onConnected: (HTMLElement) -> Unit) {
    defineElementImpl(tag, onConnected)
}
