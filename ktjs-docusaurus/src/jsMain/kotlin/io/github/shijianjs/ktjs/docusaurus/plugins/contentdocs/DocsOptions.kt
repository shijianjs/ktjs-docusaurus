package io.github.shijianjs.ktjs.docusaurus.plugins.contentdocs

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import io.github.shijianjs.ktjs.docusaurus.config.StringOrFalse
import js.objects.Record
import js.reflect.unsafeCast
import kotlin.js.JsAny

/**
 * Options of `@docusaurus/plugin-content-docs`, also the `docs` entry of the classic preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-content-docs">Docs plugin</a>
 */
external interface DocsOptions : PluginOptions {
    /** Directory holding the docs, relative to the site directory. Defaults to `docs`. */
    var path: String?

    /** URL prefix of the docs section, without a trailing slash. Defaults to `docs`. */
    var routeBasePath: String?

    /** Sidebar file, relative to [path]; `false` disables sidebars, `null` autogenerates them. */
    var sidebarPath: StringOrFalse?

    /** Base URL of the "edit this page" link, or a callback computing it per file. */
    var editUrl: StringOrDocsEditUrlFunction?

    /** Point edit links at the current version instead of the versioned copy. */
    var editCurrentVersion: Boolean?

    /** Point edit links at the translated file. */
    var editLocalizedFiles: Boolean?

    /** Show the last update time of each doc. */
    var showLastUpdateTime: Boolean?

    /** Show the last author of each doc. */
    var showLastUpdateAuthor: Boolean?

    /** Custom parser for the `01-` style number prefixes: a callback, `true` for the default, `false` to disable. */
    var numberPrefixParser: NumberPrefixParserOrBoolean?

    /** Render breadcrumbs on doc pages. */
    var breadcrumbs: Boolean?

    /** Glob patterns of files to include, relative to [path]. */
    var include: Array<String>?

    /** Glob patterns of files to exclude. */
    var exclude: Array<String>?

    /** URL segment of the tags pages, appended to [routeBasePath]. Defaults to `tags`. */
    var tagsBasePath: String?

    /** Version navigated to by default. */
    var lastVersion: String?

    /** Restrict the build to these versions. */
    var onlyIncludeVersions: Array<String>?

    /** Build only the current version even when versioned docs exist. */
    var disableVersioning: Boolean?

    /** Include the current version in the build. */
    var includeCurrentVersion: Boolean?

    /** Per-version overrides. */
    var versions: Record<JsAny, DocsVersionOptions>?

    /** Categories in the sidebar can be collapsed. */
    var sidebarCollapsible: Boolean?

    /** Categories start collapsed. */
    var sidebarCollapsed: Boolean?

    /** `boolean | Partial<AdmonitionOptions>`: `:::note` support, on by default. */
    var admonitions: BooleanOrDocsAdmonitionOptions?

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

    /** Parent component of every docs page. */
    var docsRootComponent: String?

    /** Parent component of one version's docs pages. */
    var docVersionRootComponent: String?

    /** Parent component of the docs pages that have a sidebar. */
    var docRootComponent: String?

    /** The doc page container. */
    var docItemComponent: String?

    /** Root component of a tag's doc list page. */
    var docTagDocListComponent: String?

    /** Root component of the tags list page. */
    var docTagsListComponent: String?

    /** Root component of a generated category index page. */
    var docCategoryGeneratedIndexComponent: String?

    /** Callback generating the sidebar from the files. Upstream type is a function. */
    var sidebarItemsGenerator: JsAny?
}

/** Upstream `Partial<AdmonitionOptions>`. */
external interface DocsAdmonitionOptions {
    /** Keywords that open an admonition. */
    var keywords: Array<String>?
}

/** Upstream `boolean | NumberPrefixParser`. */
external interface NumberPrefixParserOrBoolean

/** Wraps the [Boolean] branch. */
inline fun NumberPrefixParserOrBoolean(value: Boolean): NumberPrefixParserOrBoolean =
    unsafeCast<NumberPrefixParserOrBoolean>(value)

/** Wraps the callback branch. */
inline fun NumberPrefixParserOrBoolean(
    noinline value: (String) -> DocsNumberPrefix,
): NumberPrefixParserOrBoolean = unsafeCast<NumberPrefixParserOrBoolean>(value)

/** Upstream `boolean | Partial<AdmonitionOptions>`. */
external interface BooleanOrDocsAdmonitionOptions

/** Wraps the [Boolean] branch. */
inline fun BooleanOrDocsAdmonitionOptions(value: Boolean): BooleanOrDocsAdmonitionOptions =
    unsafeCast<BooleanOrDocsAdmonitionOptions>(value)

/** Wraps the options branch. */
inline fun BooleanOrDocsAdmonitionOptions(
    value: DocsAdmonitionOptions,
): BooleanOrDocsAdmonitionOptions = unsafeCast<BooleanOrDocsAdmonitionOptions>(value)

/** Upstream `string | EditUrlFunction`. */
external interface StringOrDocsEditUrlFunction

/** Wraps the [String] branch. */
inline fun StringOrDocsEditUrlFunction(value: String): StringOrDocsEditUrlFunction =
    unsafeCast<StringOrDocsEditUrlFunction>(value)

/** Wraps the callback branch. */
inline fun StringOrDocsEditUrlFunction(
    noinline value: (DocsEditUrlParams) -> String?,
): StringOrDocsEditUrlFunction = unsafeCast<StringOrDocsEditUrlFunction>(value)

/** The argument object of `EditUrlFunction`. */
external interface DocsEditUrlParams {
    /** Version name. */
    var version: String

    /** Root of the version's docs, relative to the site directory. */
    var versionDocsDirPath: String

    /** Doc path, relative to [versionDocsDirPath]. */
    var docPath: String

    /** Final URL of the doc. */
    var permalink: String

    /** Locale name. */
    var locale: String
}

/** The result of a `NumberPrefixParser`: the file name without its prefix, plus the parsed number. */
external interface DocsNumberPrefix {
    /** File name with the number prefix removed. */
    var filename: String

    /** The parsed prefix, when the name had one. */
    var numberPrefix: Double?
}

/** Upstream `VersionOptions`. */
external interface DocsVersionOptions {
    /** URL segment of this version. */
    var path: String?

    /** Label shown in badges and dropdowns. */
    var label: String?

    /** Banner shown at the top of the version's docs: `'none'`, `'unreleased'` or `'unmaintained'`. */
    var banner: String?

    /** Show a badge with the version label. */
    var badge: Boolean?

    /** Keep this version out of search engines. */
    var noIndex: Boolean?

    /** Extra class name for the `<html>` element. */
    var className: String?
}
