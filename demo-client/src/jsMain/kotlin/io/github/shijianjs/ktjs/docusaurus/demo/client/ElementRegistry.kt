package io.github.shijianjs.ktjs.docusaurus.demo.client

import web.html.HTMLElement

/**
 * Defines a custom element whose `connectedCallback` runs [onConnected].
 *
 * The `CustomElement` / `CustomElementReference` API from kotlin-browser is the declarative way to
 * write this, and it compiles — but at runtime it goes through `ConstructorFunction(clazz)`, i.e. the
 * Kotlin class itself, and Kotlin 2.4 still compiles `class X : HTMLElement()` to
 * `function X() { HTMLElement.call(this); }`. A DOM constructor may only be invoked with `new`, so the
 * browser throws `Please use the 'new' operator` as soon as the element is created.
 *
 * Until the compiler emits a real `class X extends HTMLElement`, the class syntax comes from here: six
 * lines of JavaScript that define the element and call back into Kotlin. Attributes, child tags,
 * section building and cleanup all stay on the Kotlin side.
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
