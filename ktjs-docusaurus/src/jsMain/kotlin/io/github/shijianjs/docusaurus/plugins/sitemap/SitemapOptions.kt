package io.github.shijianjs.docusaurus.plugins.sitemap

import io.github.shijianjs.docusaurus.config.PluginOptions
import kotlin.js.JsAny
import js.reflect.unsafeCast

/**
 * Options of `@docusaurus/plugin-sitemap`, also the `sitemap` entry of the classic preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-sitemap">Sitemap plugin</a>
 */
external interface SitemapOptions : PluginOptions {
    /** How often crawlers should revisit a page. Defaults to `weekly`. */
    var changefreq: SitemapChangefreq?

    /** Priority of a page relative to the rest of the site, from `0.0` to `1.0`. */
    var priority: Double?

    /** Last modification date written into the sitemap; defaults to the build date. */
    var lastmod: String?

    /** Glob patterns of routes left out of the sitemap. */
    var ignorePatterns: Array<String>?

    /** File name of the sitemap, relative to the site root. Defaults to `sitemap.xml`. */
    var filename: String?

    /** Build the sitemap entries yourself. Upstream type is a function returning a promise. */
    var createSitemapItems: JsAny?
}

/** Upstream `'always' | 'hourly' | 'daily' | 'weekly' | 'monthly' | 'yearly' | 'never'`. */
sealed external interface SitemapChangefreq

/** Wraps a value coming from JavaScript. Prefer [SitemapChangefreqs]. */
fun SitemapChangefreq(value: String): SitemapChangefreq = unsafeCast<SitemapChangefreq>(value)

/** The literals of [SitemapChangefreq]. */
object SitemapChangefreqs {
    /** Changes on every visit. */
    inline val always: SitemapChangefreq get() = SitemapChangefreq("always")

    /** Changes every hour. */
    inline val hourly: SitemapChangefreq get() = SitemapChangefreq("hourly")

    /** Changes every day. */
    inline val daily: SitemapChangefreq get() = SitemapChangefreq("daily")

    /** Changes every week; the Docusaurus default. */
    inline val weekly: SitemapChangefreq get() = SitemapChangefreq("weekly")

    /** Changes every month. */
    inline val monthly: SitemapChangefreq get() = SitemapChangefreq("monthly")

    /** Changes every year. */
    inline val yearly: SitemapChangefreq get() = SitemapChangefreq("yearly")

    /** Archived, never changes. */
    inline val never: SitemapChangefreq get() = SitemapChangefreq("never")
}
