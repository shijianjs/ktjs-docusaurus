package io.github.shijianjs.ktjs.docusaurus.plugins.themeclassic

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import js.reflect.unsafeCast

/**
 * Options of `@docusaurus/theme-classic`, also the `theme` entry of the classic preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/themes/@docusaurus/theme-classic">Classic theme</a>
 */
external interface ThemeClassicOptions : PluginOptions {
    /** Extra stylesheets, relative to the site directory, applied after the theme's own. */
    var customCss: StringOrStringArray?
}

/** Upstream `string | string[]`. */
external interface StringOrStringArray

/** Wraps the single-value branch. */
fun StringOrStringArray(value: String): StringOrStringArray = unsafeCast<StringOrStringArray>(value)

/** Wraps the list branch. */
fun StringOrStringArray(value: Array<String>): StringOrStringArray = unsafeCast<StringOrStringArray>(value)
