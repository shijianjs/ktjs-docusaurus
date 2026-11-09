package io.github.shijianjs.ktjs.docusaurus.demo.client

import web.html.HTMLElement

/**
 * Defines a custom element whose `connectedCallback` runs [onConnected].
 *
 * The class syntax comes from JavaScript on purpose. A Kotlin class that extends a DOM type is not
 * usable here:
 *
 * - it emits `class X extends HTMLElement` at module scope, and this bundle is loaded by Node (for
 *   `docusaurus.config.mjs`) where `HTMLElement` does not exist;
 * - even in a browser-only bundle, Kotlin re-emits the inherited DOM members as self-recursive
 *   forwarders (`querySelector(s) { return this.querySelector(s) }`), so the first DOM call overflows
 *   the stack.
 *
 * Everything else stays in Kotlin: `defineElement` only supplies the class and forwards
 * `connectedCallback`; the kit owns the attribute parsing (`bindHomeHero` and friends).
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

/** Registers [tag]; [onConnected] runs every time an instance is connected. */
internal fun defineElement(tag: String, onConnected: (HTMLElement) -> Unit) {
    defineElementImpl(tag, onConnected)
}
