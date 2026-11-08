package io.github.shijianjs.ktjs.docusaurus.presets

import io.github.shijianjs.ktjs.docusaurus.config.PresetConfig
import io.github.shijianjs.ktjs.docusaurus.plugins.contentblog.BlogOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.contentdocs.DocsOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.contentpages.PagesOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.googleanalytics.GoogleAnalyticsOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.gtag.GtagOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.gtm.GtmOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.sitemap.SitemapOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.svgr.SvgrOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.themeclassic.ThemeClassicOptions
import js.reflect.unsafeCast

/**
 * Options of `@docusaurus/preset-classic`, the preset almost every Docusaurus site uses.
 *
 * @see <a href="https://docusaurus.io/docs/api/presets">Preset configuration</a>
 */
external interface ClassicOptions {
    /** Enable the debug plugin. `true` in production as well. */
    var debug: Boolean?

    /** Docs plugin; `false` disables it. */
    var docs: DocsOptionsOrFalse?

    /** Blog plugin; `false` disables it. */
    var blog: BlogOptionsOrFalse?

    /** Pages plugin; `false` disables it. */
    var pages: PagesOptionsOrFalse?

    /** Sitemap plugin; `false` disables it. */
    var sitemap: SitemapOptionsOrFalse?

    /** SVGR plugin (importing `.svg` files as components); `false` disables it. */
    var svgr: SvgrOptionsOrFalse?

    /** Classic theme options. */
    var theme: ThemeClassicOptions?

    /** Google Analytics plugin; enabled when this key is present. */
    var googleAnalytics: GoogleAnalyticsOptions?

    /** Google Analytics 4 plugin; enabled when this key is present. */
    var gtag: GtagOptions?

    /** Google Tag Manager plugin; enabled when this key is present. */
    var googleTagManager: GtmOptions?
}

/**
 * Registers the classic preset with [options].
 *
 * ```kotlin
 * presets = arrayOf(classicPreset(unsafeJso {
 *     docs = DocsOptionsOrFalse(unsafeJso<DocsOptions> {
 *         sidebarPath = "sidebars.ts"
 *         routeBasePath = "docs"
 *     })
 *     blog = BlogOptionsOrFalse(false)
 *     theme = unsafeJso<ThemeClassicOptions> { customCss = "src/css/custom.css" }
 * }))
 * ```
 */
fun classicPreset(options: ClassicOptions): PresetConfig = PresetConfig("classic", options)

/** Upstream `false | DocsOptions`. */
external interface DocsOptionsOrFalse

/** Wraps the [DocsOptions] branch. */
fun DocsOptionsOrFalse(value: DocsOptions): DocsOptionsOrFalse = unsafeCast<DocsOptionsOrFalse>(value)

/** Wraps the `false` branch. */
fun DocsOptionsOrFalse(value: Boolean): DocsOptionsOrFalse = unsafeCast<DocsOptionsOrFalse>(value)

/** Upstream `false | BlogOptions`. */
external interface BlogOptionsOrFalse

/** Wraps the [BlogOptions] branch. */
fun BlogOptionsOrFalse(value: BlogOptions): BlogOptionsOrFalse = unsafeCast<BlogOptionsOrFalse>(value)

/** Wraps the `false` branch. */
fun BlogOptionsOrFalse(value: Boolean): BlogOptionsOrFalse = unsafeCast<BlogOptionsOrFalse>(value)

/** Upstream `false | PagesOptions`. */
external interface PagesOptionsOrFalse

/** Wraps the [PagesOptions] branch. */
fun PagesOptionsOrFalse(value: PagesOptions): PagesOptionsOrFalse = unsafeCast<PagesOptionsOrFalse>(value)

/** Wraps the `false` branch. */
fun PagesOptionsOrFalse(value: Boolean): PagesOptionsOrFalse = unsafeCast<PagesOptionsOrFalse>(value)

/** Upstream `false | SitemapOptions`. */
external interface SitemapOptionsOrFalse

/** Wraps the [SitemapOptions] branch. */
fun SitemapOptionsOrFalse(value: SitemapOptions): SitemapOptionsOrFalse = unsafeCast<SitemapOptionsOrFalse>(value)

/** Wraps the `false` branch. */
fun SitemapOptionsOrFalse(value: Boolean): SitemapOptionsOrFalse = unsafeCast<SitemapOptionsOrFalse>(value)

/** Upstream `false | SvgrOptions`. */
external interface SvgrOptionsOrFalse

/** Wraps the [SvgrOptions] branch. */
fun SvgrOptionsOrFalse(value: SvgrOptions): SvgrOptionsOrFalse = unsafeCast<SvgrOptionsOrFalse>(value)

/** Wraps the `false` branch. */
fun SvgrOptionsOrFalse(value: Boolean): SvgrOptionsOrFalse = unsafeCast<SvgrOptionsOrFalse>(value)
