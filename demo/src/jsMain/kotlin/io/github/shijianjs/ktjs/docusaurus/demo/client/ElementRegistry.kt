package io.github.shijianjs.ktjs.docusaurus.demo.client

import web.html.HTMLElement

/**
 * Defines a custom element whose `connectedCallback` runs [onConnected].
 *
 * A Kotlin class cannot extend `HTMLElement` here. Two independent reasons, both verified on Kotlin
 * 2.4.21:
 *
 * 1. Kotlin emits `function X() { HTMLElement.call(this) }` unless the js target compiles at ES2015,
 *    and a DOM constructor may only be invoked with `new`.
 * 2. At ES2015 the class is real, but Kotlin also re-emits every inherited external member as a
 *    forwarding method that calls itself:
 *
 *    ```js
 *    querySelector(selectors) { return this.querySelector(selectors); }
 *    ```
 *
 *    so the first DOM call from `connectedCallback` overflows the stack (`Maximum call stack size
 *    exceeded`) and the element never renders.
 *
 * The class syntax therefore comes from JavaScript and `connectedCallback` calls back into Kotlin,
 * where all the logic lives: attributes, child tags, section building and cleanup.
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
