package io.github.shijianjs.ktjs.docusaurus.demo.client

import web.html.HTMLElement

/**
 * Defines a custom element whose `connectedCallback` runs [onConnected].
 *
 * The class syntax has to come from JavaScript. Kotlin/JS compiles `class X : HTMLElement()` to
 * `function X() { HTMLElement.call(this); }`, and a DOM constructor may only be invoked with `new`:
 * the browser throws `Please use the 'new' operator`. Everything else — reading attributes, building
 * the section, deciding what to drop — stays in Kotlin; this is the only JavaScript in the module.
 *
 * Re-registering a tag is a no-op, which keeps a hot reload from throwing `NotSupportedError`.
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
