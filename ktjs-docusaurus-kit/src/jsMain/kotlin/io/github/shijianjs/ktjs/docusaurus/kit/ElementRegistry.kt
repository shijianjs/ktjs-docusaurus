package io.github.shijianjs.ktjs.docusaurus.kit

import web.html.HTMLElement

/**
 * Defines a custom element whose `connectedCallback` runs [onConnected].
 *
 * The class syntax comes from JavaScript on purpose. A Kotlin class that extends a DOM type is not
 * usable for an element:
 *
 * - it emits `class X extends HTMLElement` at module scope, and a site's bundle is loaded by Node (for
 *   `docusaurus.config.mjs`) where `HTMLElement` does not exist;
 * - even in a browser-only bundle, Kotlin re-emits the inherited DOM members as self-recursive
 *   forwarders (`querySelector(s) { return this.querySelector(s) }`), so the first DOM call from
 *   `connectedCallback` overflows the stack.
 *
 * Everything else stays in Kotlin: this function only supplies the class and forwards
 * `connectedCallback`. The kit's [HomeElementBinding] implementations own the attribute parsing, and
 * [bindHomeSection] decides which instance a host owns.
 *
 * The bridge is a function expression, not module-level code: loading this module never evaluates
 * `HTMLElement`, so the kit stays usable from the server bundle.
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
fun defineElement(tag: String, onConnected: (HTMLElement) -> Unit) {
    defineElementImpl(tag, onConnected)
}
