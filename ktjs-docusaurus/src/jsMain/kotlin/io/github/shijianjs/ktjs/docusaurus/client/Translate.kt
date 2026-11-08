@file:JsModule("@docusaurus/Translate")

package io.github.shijianjs.ktjs.docusaurus.client

/**
 * Upstream `translate()` options.
 *
 * `id` is the key in `i18n/<locale>/code.json`, `message` is the English source string and
 * `description` is the translator's hint — Docusaurus extracts all three with
 * `docusaurus write-translations`, and the same `id` links a translation back to this call site.
 */
external interface TranslateOptions {
    /** Translation key, unique per site. */
    var id: String?

    /** Source string, used when no translation exists. */
    var message: String?

    /** Hint shown to translators next to the message. */
    var description: String?
}

/**
 * Resolves a message for the current locale without rendering a component.
 *
 * A plain DOM section cannot render React's `<Translate>`, so it receives the resolved string from
 * its caller instead; this function is that caller's tool. It must be called during render (it is
 * a hook internally), not from an event handler.
 */
external fun translate(options: TranslateOptions): String
