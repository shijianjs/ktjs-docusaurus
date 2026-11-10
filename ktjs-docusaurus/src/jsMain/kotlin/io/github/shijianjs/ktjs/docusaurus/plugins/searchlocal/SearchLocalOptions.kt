package io.github.shijianjs.ktjs.docusaurus.plugins.searchlocal

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import js.objects.Record
import js.reflect.unsafeCast
import kotlin.js.JsAny
import kotlin.js.RegExp

/**
 * Options of `@easyops-cn/docusaurus-search-local`, a local (build-time) search theme.
 *
 * It is a **theme**, not a plugin: register it in `Config.themes` so its `@theme/SearchBar` replaces
 * the classic theme's placeholder, and Docusaurus renders the search box in the navbar by itself.
 *
 * ```kotlin
 * themes = arrayOf<PluginConfig?>(
 *     PluginConfig(
 *         "@easyops-cn/docusaurus-search-local",
 *         unsafeJso<SearchLocalOptions> {
 *             language = StringOrStringArray(arrayOf("en", "zh"))
 *             indexBlog = false
 *         },
 *     ),
 * )
 * ```
 *
 * @see <a href="https://github.com/easyops-cn/docusaurus-search-local#theme-options">Theme options</a>
 */
external interface SearchLocalOptions : PluginOptions {
    /** Index the docs. Defaults to `true`. */
    var indexDocs: Boolean?

    /** Index the blog. Defaults to `true`. */
    var indexBlog: Boolean?

    /** Index standalone pages. Defaults to `false`. */
    var indexPages: Boolean?

    /**
     * Route base path(s) of the docs to index; the leading slash is optional. Defaults to `/docs`.
     *
     * For docs-only mode it has to match the preset's `routeBasePath`, e.g. `"/"`.
     */
    var docsRouteBasePath: StringOrStringArray?

    /** Route base path(s) of the blog to index. Defaults to `/blog`. */
    var blogRouteBasePath: StringOrStringArray?

    /**
     * Languages of the index, from `lunr-languages` plus `zh` (jieba). Defaults to `["en"]`.
     *
     * Every language that appears on the site has to be listed, otherwise its pages are indexed
     * with the wrong tokenizer.
     */
    var language: StringOrStringArray?

    /**
     * Add the content hash of the indexed Markdown to the index URL, so a redeploy invalidates
     * browser caches. `"filename"` puts the hash in the file name instead of the query string.
     * Defaults to `false`.
     */
    var hashed: BooleanOrHashed?

    /** Directories of the docs used to compute [hashed], relative to the site directory. Defaults to `docs`. */
    var docsDir: StringOrStringArray?

    /** Directories of the blog used to compute [hashed]. Defaults to `blog`. */
    var blogDir: StringOrStringArray?

    /**
     * Docs plugin id whose preferred version becomes the indexed one, when the site runs more than
     * one docs instance.
     */
    var docsPluginIdForPreferredVersion: String?

    /**
     * Keep the default stop-word filter. `true` is the same as `["en"]`, which is what a programming
     * documentation site usually wants.
     */
    var removeDefaultStopWordFilter: BooleanOrStringArray?

    /**
     * Keep the default stemmer, making every partial word searchable at the cost of index size and
     * query speed. Defaults to `false`.
     */
    var removeDefaultStemmer: Boolean?

    /** Highlight the matched terms on the target page. Defaults to `false`. */
    var highlightSearchTermsOnTargetPage: Boolean?

    /** Maximum number of suggestions. Defaults to `8`. */
    var searchResultLimits: Double?

    /** Maximum number of characters shown around a match. Defaults to `50`. */
    var searchResultContextMaxLength: Double?

    /** Show the heading path of a suggestion. Defaults to `false`. */
    var explicitSearchResultPath: Boolean?

    /**
     * Routes left out of the index: a string is an exact match, a `RegExp` a partial one. Base URL
     * is not part of the path. Defaults to an empty list.
     */
    var ignoreFiles: IgnoreFiles?

    /** CSS selectors whose content is skipped while indexing. Defaults to an empty list. */
    var ignoreCssSelectors: StringOrStringArray?

    /** Focus the search box with a keyboard shortcut. Defaults to `true`. */
    var searchBarShortcut: Boolean?

    /** Show the shortcut hint inside the search box. Defaults to `true`. */
    var searchBarShortcutHint: Boolean?

    /**
     * Shortcut that focuses the search box: `"s"`, `"ctrl+k"`, `"cmd+k"`, `"mod+k"` or
     * `"ctrl+shift+k"`. Defaults to `"mod+k"`.
     */
    var searchBarShortcutKeymap: String?

    /**
     * Side of the navbar the search box appears on. `auto` follows the same rule the classic theme
     * uses for `themeConfig.algolia`. Defaults to [SearchBarPositions.auto].
     */
    var searchBarPosition: SearchBarPosition?

    /** Extra jieba dictionary for `zh`, as file content. */
    var zhUserDict: String?

    /** Extra jieba dictionary for `zh`, as a file path. */
    var zhUserDictPath: String?

    /**
     * Split the index into one index per sub-path, e.g. `["docs", "community"]`, so a search only
     * covers the section the reader is in.
     */
    var searchContextByPaths: Array<StringOrSearchContextByPath>?

    /** Hide the search box on pages that match none of [searchContextByPaths]. Defaults to `false`. */
    var hideSearchBarWithNoSearchContext: Boolean?

    /**
     * Search all contexts from a page that matches none of [searchContextByPaths]. Defaults to
     * `false`.
     */
    var useAllContextsWithNoSearchContext: Boolean?

    /** Index pages marked `noIndex`, and unlisted articles. Defaults to `false`. */
    var forceIgnoreNoIndex: Boolean?

    /** Edit distance of the fuzzy matching pass; `0` disables it. Defaults to `1`. */
    var fuzzyMatchingDistance: Double?

    /**
     * Ask-AI widget configuration. Upstream type is `open-ask-ai`'s `AskAIWidgetProps`, which is an
     * open `{[key: string]: any}` record of a package this wrapper does not depend on.
     */
    var askAi: JsAny?
}

/** Upstream `string | string[]`. */
external interface StringOrStringArray

/** Wraps the single-value branch. */
inline fun StringOrStringArray(value: String): StringOrStringArray = unsafeCast<StringOrStringArray>(value)

/** Wraps the list branch. */
inline fun StringOrStringArray(value: Array<String>): StringOrStringArray =
    unsafeCast<StringOrStringArray>(value)

/** Upstream `boolean | string[]`. */
external interface BooleanOrStringArray

/** Wraps the [Boolean] branch. */
inline fun BooleanOrStringArray(value: Boolean): BooleanOrStringArray =
    unsafeCast<BooleanOrStringArray>(value)

/** Wraps the list branch. */
inline fun BooleanOrStringArray(value: Array<String>): BooleanOrStringArray =
    unsafeCast<BooleanOrStringArray>(value)

/** Upstream `boolean | "query" | "filename"`. */
external interface BooleanOrHashed

/** Wraps the [Boolean] branch. */
inline fun BooleanOrHashed(value: Boolean): BooleanOrHashed = unsafeCast<BooleanOrHashed>(value)

/** Wraps a [Hashed] branch. */
inline fun BooleanOrHashed(value: Hashed): BooleanOrHashed = unsafeCast<BooleanOrHashed>(value)

/** Upstream `'query' | 'filename'`. */
sealed external interface Hashed

/** Wraps a value coming from JavaScript. Prefer [Hasheds]. */
inline fun Hashed(value: String): Hashed = unsafeCast<Hashed>(value)

/** The literals of [Hashed]. */
object Hasheds {
    /** Cache-bust through a `?_=<hash>` query string. */
    inline val query: Hashed get() = Hashed("query")

    /** Cache-bust by writing the hash into the index file name. */
    inline val filename: Hashed get() = Hashed("filename")
}

/** Upstream `string | RegExp`. */
external interface StringOrRegExp

/** Wraps the string branch. */
inline fun StringOrRegExp(value: String): StringOrRegExp = unsafeCast<StringOrRegExp>(value)

/** Wraps the pattern branch. */
inline fun StringOrRegExp(value: RegExp): StringOrRegExp = unsafeCast<StringOrRegExp>(value)

/** Upstream `string | RegExp | (string | RegExp)[]`. */
external interface IgnoreFiles

/** Wraps the single-string branch. */
inline fun IgnoreFiles(value: String): IgnoreFiles = unsafeCast<IgnoreFiles>(value)

/** Wraps the single-pattern branch. */
inline fun IgnoreFiles(value: RegExp): IgnoreFiles = unsafeCast<IgnoreFiles>(value)

/** Wraps the list branch. */
inline fun IgnoreFiles(value: Array<StringOrRegExp>): IgnoreFiles = unsafeCast<IgnoreFiles>(value)

/** Upstream `string | Record<string, string>`, as the label of a [SearchContextByPath]. */
external interface StringOrStringRecord

/** Wraps the single-label branch. */
inline fun StringOrStringRecord(value: String): StringOrStringRecord =
    unsafeCast<StringOrStringRecord>(value)

/** Wraps the per-locale branch, keyed by locale id. */
inline fun StringOrStringRecord(value: Record<JsAny, String>): StringOrStringRecord =
    unsafeCast<StringOrStringRecord>(value)

/** Upstream `{label: string | Record<string, string>; path: string}`. */
external interface SearchContextByPath {
    /** Label shown in the context selector; a record holds one label per locale. */
    var label: StringOrStringRecord?

    /** Sub-path of the site this context covers. */
    var path: String
}

/** Upstream `string | {label: string | Record<string, string>; path: string}`. */
external interface StringOrSearchContextByPath

/** Wraps the bare-path branch. */
inline fun StringOrSearchContextByPath(value: String): StringOrSearchContextByPath =
    unsafeCast<StringOrSearchContextByPath>(value)

/** Wraps the labelled branch. */
inline fun StringOrSearchContextByPath(value: SearchContextByPath): StringOrSearchContextByPath =
    unsafeCast<StringOrSearchContextByPath>(value)

/** Upstream `'auto' | 'left' | 'right'`. */
sealed external interface SearchBarPosition

/** Wraps a value coming from JavaScript. Prefer [SearchBarPositions]. */
inline fun SearchBarPosition(value: String): SearchBarPosition = unsafeCast<SearchBarPosition>(value)

/** The literals of [SearchBarPosition]. */
object SearchBarPositions {
    /** Detect the side the way the classic theme does for `themeConfig.algolia`. */
    inline val auto: SearchBarPosition get() = SearchBarPosition("auto")

    /** Force the search box to the left side of the navbar. */
    inline val left: SearchBarPosition get() = SearchBarPosition("left")

    /** Force the search box to the right side of the navbar. */
    inline val right: SearchBarPosition get() = SearchBarPosition("right")
}
