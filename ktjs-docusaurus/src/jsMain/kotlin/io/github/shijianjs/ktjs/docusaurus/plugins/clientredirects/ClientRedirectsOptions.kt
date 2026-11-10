package io.github.shijianjs.ktjs.docusaurus.plugins.clientredirects

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import js.reflect.unsafeCast

/**
 * Options of `@docusaurus/plugin-client-redirects`: it writes extra HTML pages that bounce the
 * browser to an existing route.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-client-redirects">Client redirects plugin</a>
 */
external interface ClientRedirectsOptions : PluginOptions {
    /** Extensions removed from the created route, e.g. `[".html"]` for `docs/foo.html` → `docs/foo`. */
    var fromExtensions: Array<String>?

    /** Extensions appended to the created route, e.g. `[".html"]` for `docs/foo` → `docs/foo.html`. */
    var toExtensions: Array<String>?

    /** The redirect rules: each one points one or more paths at a single existing page. */
    var redirects: Array<ClientRedirectRule>?

    /**
     * Called for every route Docusaurus creates; return the paths that should redirect to it, or
     * `null` for none.
     */
    var createRedirects: ((String) -> StringOrStringArray?)?
}

/** Upstream `RedirectOption`: the new path(s) in [from], the existing page in [to]. */
external interface ClientRedirectRule {
    /** Pathname of an existing Docusaurus page. */
    var to: String

    /** Pathname (or paths) of the page(s) to create, redirecting to [to]. */
    var from: StringOrStringArray
}

/** Upstream `string | string[]`. */
external interface StringOrStringArray

/** Wraps the single-value branch. */
inline fun StringOrStringArray(value: String): StringOrStringArray = unsafeCast<StringOrStringArray>(value)

/** Wraps the list branch. */
inline fun StringOrStringArray(value: Array<String>): StringOrStringArray = unsafeCast<StringOrStringArray>(value)
