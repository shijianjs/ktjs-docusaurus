package io.github.shijianjs.ktjs.docusaurus.plugins.contentpages

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import kotlin.js.JsAny
import js.reflect.unsafeCast

/**
 * Options of `@docusaurus/plugin-content-pages`, also the `pages` entry of the classic preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-content-pages">Pages plugin</a>
 */
external interface PagesOptions : PluginOptions {
    /** Directory holding the pages, relative to the site directory. Defaults to `src/pages`. */
    var path: String?

    /** URL prefix of the pages, without a trailing slash. Defaults to `/`. */
    var routeBasePath: String?

    /** Glob patterns of files to include, relative to [path]. */
    var include: Array<String>?

    /** Glob patterns of files to exclude. */
    var exclude: Array<String>?

    /** Component wrapping every page. */
    var mdxPageComponent: String?

    /** Show the last update time of each page. */
    var showLastUpdateTime: Boolean?

    /** Show the last author of each page. */
    var showLastUpdateAuthor: Boolean?

    /** Base URL of the "edit this page" link, or a callback computing it per file. */
    var editUrl: StringOrPagesEditUrlFunction?

    /** Point edit links at the translated file. */
    var editLocalizedFiles: Boolean?

    /** `boolean | Partial<AdmonitionOptions>`: `:::note` support, on by default. */
    var admonitions: BooleanOrPagesAdmonitionOptions?

    /** Remark plugins, applied after Docusaurus' own. Upstream type is `any[]`. */
    var remarkPlugins: Array<JsAny>?

    /** Rehype plugins, applied after Docusaurus' own. Upstream type is `any[]`. */
    var rehypePlugins: Array<JsAny>?

    /** Recma plugins, applied after Docusaurus' own. Upstream type is `any[]`. */
    var recmaPlugins: Array<JsAny>?

    /** Remark plugins applied before Docusaurus' own. Upstream type is `any[]`. */
    var beforeDefaultRemarkPlugins: Array<JsAny>?

    /** Rehype plugins applied before Docusaurus' own. Upstream type is `any[]`. */
    var beforeDefaultRehypePlugins: Array<JsAny>?
}

/** Upstream `Partial<AdmonitionOptions>`. */
external interface PagesAdmonitionOptions {
    /** Keywords that open an admonition. */
    var keywords: Array<String>?
}

/** Upstream `boolean | Partial<AdmonitionOptions>`. */
external interface BooleanOrPagesAdmonitionOptions

/** Wraps the [Boolean] branch. */
inline fun BooleanOrPagesAdmonitionOptions(value: Boolean): BooleanOrPagesAdmonitionOptions =
    unsafeCast<BooleanOrPagesAdmonitionOptions>(value)

/** Wraps the options branch. */
inline fun BooleanOrPagesAdmonitionOptions(
    value: PagesAdmonitionOptions,
): BooleanOrPagesAdmonitionOptions = unsafeCast<BooleanOrPagesAdmonitionOptions>(value)

/** Upstream `string | EditUrlFunction`. */
external interface StringOrPagesEditUrlFunction

/** Wraps the [String] branch. */
inline fun StringOrPagesEditUrlFunction(value: String): StringOrPagesEditUrlFunction =
    unsafeCast<StringOrPagesEditUrlFunction>(value)

/** Wraps the callback branch; `null` from the callback disables the link for that page. */
inline fun StringOrPagesEditUrlFunction(
    noinline value: (PagesEditUrlParams) -> String?,
): StringOrPagesEditUrlFunction = unsafeCast<StringOrPagesEditUrlFunction>(value)

/** The argument object of the pages `EditUrlFunction`. */
external interface PagesEditUrlParams {
    /** Root content directory of the page, relative to the site directory. */
    var pagesDirPath: String

    /** Page path, relative to [pagesDirPath]. */
    var pagesPath: String

    /** Final URL of the page. */
    var permalink: String

    /** Locale name. */
    var locale: String
}
