package io.github.shijianjs.ktjs.docusaurus.plugins.gtag

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import js.reflect.unsafeCast

/**
 * Options of `@docusaurus/plugin-google-gtag`, also the `gtag` entry of the classic preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-google-gtag">gtag plugin</a>
 */
external interface GtagOptions : PluginOptions {
    /**
     * Google Analytics measurement id, e.g. `G-XXXXXXXXXX`, or several ids to load more than one
     * tracker (upstream type is the non-empty tuple `[string, ...string[]]`).
     */
    var trackingID: StringOrStringArray

    /** Ask Google to anonymize the stored IP address. */
    var anonymizeIP: Boolean?
}

/** Upstream `string | [string, ...string[]]`. */
external interface StringOrStringArray

/** Wraps the single-tracker branch. */
inline fun StringOrStringArray(value: String): StringOrStringArray = unsafeCast<StringOrStringArray>(value)

/** Wraps the multi-tracker branch; upstream requires at least one id. */
inline fun StringOrStringArray(value: Array<String>): StringOrStringArray = unsafeCast<StringOrStringArray>(value)
