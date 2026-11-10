package io.github.shijianjs.ktjs.docusaurus.plugins.contentblog

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import io.github.shijianjs.ktjs.docusaurus.config.ReportingSeverity
import io.github.shijianjs.ktjs.docusaurus.config.StringOrFalse
import js.reflect.unsafeCast
import kotlin.js.JsAny
import kotlin.js.RegExp

/**
 * Options of `@docusaurus/plugin-content-blog`, also the `blog` entry of the classic preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-content-blog">Blog plugin</a>
 */
external interface BlogOptions : PluginOptions {
    /** Directory holding the posts, relative to the site directory. Defaults to `blog`. */
    var path: String?

    /** URL prefix of the blog, without a trailing slash. Defaults to `blog`. */
    var routeBasePath: String?

    /** URL segment of the tags pages. Defaults to `tags`. */
    var tagsBasePath: String?

    /** URL segment of the archive page; `null` disables it. */
    var archiveBasePath: String?

    /** URL segment of the per-page routes, appended to [routeBasePath]. Defaults to `page`. */
    var pageBasePath: String?

    /** Glob patterns of files to include, relative to [path]. */
    var include: Array<String>?

    /** Glob patterns of files to exclude. */
    var exclude: Array<String>?

    /** Posts per listing page, or `'ALL'`. */
    var postsPerPage: DoubleOrAll?

    /** Root component of the listing page. */
    var blogListComponent: String?

    /** Root component of a post page. */
    var blogPostComponent: String?

    /** Root component of the tags list page. */
    var blogTagsListComponent: String?

    /** Root component of a tag's post list page. */
    var blogTagsPostsComponent: String?

    /** Root component of the authors list page. */
    var blogAuthorsListComponent: String?

    /** Root component of an author's post list page. */
    var blogAuthorsPostsComponent: String?

    /** Root component of the archive page. */
    var blogArchiveComponent: String?

    /** Page title, for SEO. */
    var blogTitle: String?

    /** Meta description, for SEO. */
    var blogDescription: String?

    /** Posts in the sidebar: a count or `'ALL'`. `0` disables the sidebar. */
    var blogSidebarCount: DoubleOrAll?

    /** Sidebar title. */
    var blogSidebarTitle: String?

    /** Marker that ends the summary shown in the listing. Upstream type is `RegExp`. */
    var truncateMarker: RegExp?

    /** Show the estimated reading time. */
    var showReadingTime: Boolean?

    /** Feed generation options; `null` disables the feeds. */
    var feedOptions: BlogFeedOptions?

    /** Base URL of the "edit this page" link, or a callback computing it per post. */
    var editUrl: StringOrBlogEditUrlFunction?

    /** Point edit links at the translated file. */
    var editLocalizedFiles: Boolean?

    /** Authors map file, relative to [path]. */
    var authorsMapPath: String?

    /** Override the reading time computation. */
    var readingTime: DoubleOrReadingTimeFunction?

    /** Post ordering. */
    var sortPosts: BlogSortOrder?

    /** Show the last update time of each post. */
    var showLastUpdateTime: Boolean?

    /** Show the last author of each post. */
    var showLastUpdateAuthor: Boolean?

    /** Transform, filter or drop posts before they are rendered. Upstream type is a function. */
    var processBlogPosts: JsAny?

    /** URL prefix of the authors pages. */
    var authorsBasePath: String?

    /** Path of the tags file, relative to [path]; `false` disables it. */
    var tags: StringOrFalse?

    /** What to do about authors declared inline in front matter. */
    var onInlineAuthors: ReportingSeverity?

    /** What to do about tags declared inline in front matter instead of in the tags file. */
    var onInlineTags: ReportingSeverity?

    /** What to do about posts without a truncate marker. */
    var onUntruncatedBlogPosts: ReportingSeverity?

    /** `boolean | Partial<AdmonitionOptions>`: `:::note` support, on by default. */
    var admonitions: BooleanOrBlogAdmonitionOptions?

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

/** Upstream `number | 'ALL'`. */
external interface DoubleOrAll

/** Wraps the number branch. */
inline fun DoubleOrAll(value: Double): DoubleOrAll = unsafeCast<DoubleOrAll>(value)

/** Wraps the `'ALL'` branch. */
inline fun DoubleOrAll(value: String): DoubleOrAll = unsafeCast<DoubleOrAll>(value)

/** Upstream `number | ReadingTimeFunction`. */
external interface DoubleOrReadingTimeFunction

/** Wraps the number branch. */
inline fun DoubleOrReadingTimeFunction(value: Double): DoubleOrReadingTimeFunction =
    unsafeCast<DoubleOrReadingTimeFunction>(value)

/** Wraps the callback branch. */
inline fun DoubleOrReadingTimeFunction(
    noinline value: (BlogReadingTimeParams) -> Double,
): DoubleOrReadingTimeFunction = unsafeCast<DoubleOrReadingTimeFunction>(value)

/** The argument object of `ReadingTimeFunction`. */
external interface BlogReadingTimeParams {
    /** Raw post content. */
    var content: String

    /** The post's front matter. Upstream type is `{[key: string]: unknown}`. */
    var frontMatter: JsAny
}

/** Upstream `'ascending' | 'descending'`. */
sealed external interface BlogSortOrder

/** Wraps a value coming from JavaScript. Prefer [BlogSortOrders]. */
inline fun BlogSortOrder(value: String): BlogSortOrder = unsafeCast<BlogSortOrder>(value)

/** The literals of [BlogSortOrder]. */
object BlogSortOrders {
    /** Oldest post first. */
    inline val ascending: BlogSortOrder get() = BlogSortOrder("ascending")

    /** Newest post first; the default. */
    inline val descending: BlogSortOrder get() = BlogSortOrder("descending")
}

/** Upstream `Partial<AdmonitionOptions>`. */
external interface BlogAdmonitionOptions {
    /** Keywords that open an admonition. */
    var keywords: Array<String>?
}

/** Upstream `boolean | Partial<AdmonitionOptions>`. */
external interface BooleanOrBlogAdmonitionOptions

/** Wraps the [Boolean] branch. */
inline fun BooleanOrBlogAdmonitionOptions(value: Boolean): BooleanOrBlogAdmonitionOptions =
    unsafeCast<BooleanOrBlogAdmonitionOptions>(value)

/** Wraps the options branch. */
inline fun BooleanOrBlogAdmonitionOptions(
    value: BlogAdmonitionOptions,
): BooleanOrBlogAdmonitionOptions = unsafeCast<BooleanOrBlogAdmonitionOptions>(value)

/** Upstream `string | EditUrlFunction`. */
external interface StringOrBlogEditUrlFunction

/** Wraps the [String] branch. */
inline fun StringOrBlogEditUrlFunction(value: String): StringOrBlogEditUrlFunction =
    unsafeCast<StringOrBlogEditUrlFunction>(value)

/** Wraps the callback branch. */
inline fun StringOrBlogEditUrlFunction(
    noinline value: (BlogEditUrlParams) -> String?,
): StringOrBlogEditUrlFunction = unsafeCast<StringOrBlogEditUrlFunction>(value)

/** The argument object of the blog `EditUrlFunction`. */
external interface BlogEditUrlParams {
    /** Locale name. */
    var locale: String

    /** Root of the blog content, relative to the site directory. */
    var blogDirPath: String

    /** Post path, relative to [blogDirPath]. */
    var blogPath: String

    /** Final URL of the post. */
    var permalink: String
}

/** Upstream `UserFeedOptions`: the blog feed configuration. */
external interface BlogFeedOptions {
    /** Feed kinds: `'rss'`, `'atom'`, `'json'`, `'all'`, or a list of them; `null` disables feeds. */
    var type: StringOrStringArray?

    /** Feed title. Defaults to the blog title. */
    var title: String?

    /** Feed description. Defaults to the blog description. */
    var description: String?

    /** Copyright line. */
    var copyright: String?

    /** Feed language, e.g. `en`. */
    var language: String?

    /** Time to live, in minutes. */
    var ttl: Double?

    /** Maximum number of items in a feed. */
    var limit: Double?

    /** Glob patterns of posts kept out of the feeds. */
    var ignorePatterns: Array<String>?

    /** Build the feed items yourself. Upstream type is a function. */
    var createFeedItems: JsAny?

    /** Per-format XSLT stylesheet of the feeds: `true` for the bundled one, a path for your own. */
    var xslt: BooleanOrBlogXsltOptions?
}

/** Upstream `{rss?: string | boolean | null; atom?: string | boolean | null}`. */
external interface BlogXsltOptions {
    /** Stylesheet of the RSS feed. */
    var rss: StringOrBoolean?

    /** Stylesheet of the Atom feed. */
    var atom: StringOrBoolean?
}

/** Upstream `boolean | {rss?: …; atom?: …}`. */
external interface BooleanOrBlogXsltOptions

/** Wraps the [Boolean] branch: `true` uses the bundled stylesheet, `false` disables it. */
inline fun BooleanOrBlogXsltOptions(value: Boolean): BooleanOrBlogXsltOptions =
    unsafeCast<BooleanOrBlogXsltOptions>(value)

/** Wraps the per-format branch. */
inline fun BooleanOrBlogXsltOptions(value: BlogXsltOptions): BooleanOrBlogXsltOptions =
    unsafeCast<BooleanOrBlogXsltOptions>(value)

/** Upstream `string | boolean`. */
external interface StringOrBoolean

/** Wraps the [String] branch: a URL or path of a stylesheet. */
inline fun StringOrBoolean(value: String): StringOrBoolean = unsafeCast<StringOrBoolean>(value)

/** Wraps the [Boolean] branch. */
inline fun StringOrBoolean(value: Boolean): StringOrBoolean = unsafeCast<StringOrBoolean>(value)

/** Upstream `string | string[]`. */
external interface StringOrStringArray

/** Wraps the single-value branch. */
inline fun StringOrStringArray(value: String): StringOrStringArray = unsafeCast<StringOrStringArray>(value)

/** Wraps the list branch. */
inline fun StringOrStringArray(value: Array<String>): StringOrStringArray = unsafeCast<StringOrStringArray>(value)
