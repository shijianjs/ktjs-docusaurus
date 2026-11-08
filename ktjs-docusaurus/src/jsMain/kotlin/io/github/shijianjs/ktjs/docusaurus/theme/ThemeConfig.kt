package io.github.shijianjs.ktjs.docusaurus.theme

import js.objects.Record
import kotlin.js.JsAny
import js.reflect.unsafeCast

/**
 * Upstream `ThemeConfig`: the union of the classic theme, `theme-common` and the search/Mermaid theme
 * contributions.
 *
 * Docusaurus itself declares `ThemeConfig` as `{[key: string]: unknown}` and lets each theme read
 * what it needs; this declaration keeps the fields the bundled themes understand, so a misspelt key
 * is a compile error. A third-party theme's own fields are added to this interface when that theme
 * is wrapped.
 *
 * @see <a href="https://docusaurus.io/docs/api/themes/configuration">Theme configuration</a>
 */
external interface ThemeConfig {
    /** Docs-specific theme options. */
    var docs: DocsThemeConfig?

    /** Blog-specific theme options. */
    var blog: BlogThemeConfig?

    /** Navbar configuration. */
    var navbar: Navbar?

    /** Colour mode switch behaviour. */
    var colorMode: ColorModeConfig?

    /** Announcement bar above the navbar. */
    var announcementBar: AnnouncementBarConfig?

    /** Code block syntax highlighting. */
    var prism: PrismConfig?

    /** Footer configuration. */
    var footer: Footer?

    /** Default social card image, used as `og:image` when a page has none. */
    var image: String?

    /** Extra `<meta>` tags for every page, as name/value records. */
    var metadata: Array<Record<JsAny, String>>?

    /** Table of contents heading levels. */
    var tableOfContents: TableOfContents?

    /** Algolia DocSearch options, enabled when present. */
    var algolia: AlgoliaConfig?

    /** Mermaid diagram theme, used by `@docusaurus/theme-mermaid`. */
    var mermaid: MermaidThemeConfig?
}

/** Upstream `ThemeConfig['docs']`. */
external interface DocsThemeConfig {
    /** Where the selected docs version is remembered: `'localStorage'` or `'none'`. */
    var versionPersistence: DocsVersionPersistence?

    /** Sidebar behaviour. */
    var sidebar: DocsSidebarThemeConfig?
}

/** Upstream `'localStorage' | 'none'`. */
sealed external interface DocsVersionPersistence

/** Wraps a value coming from JavaScript. Prefer [DocsVersionPersistences]. */
fun DocsVersionPersistence(value: String): DocsVersionPersistence = unsafeCast<DocsVersionPersistence>(value)

/** The literals of [DocsVersionPersistence]. */
object DocsVersionPersistences {
    /** Remember the chosen version across visits. */
    inline val localStorage: DocsVersionPersistence get() = DocsVersionPersistence("localStorage")

    /** Do not remember it. */
    inline val none: DocsVersionPersistence get() = DocsVersionPersistence("none")
}

/** Upstream `ThemeConfig['docs']['sidebar']`. */
external interface DocsSidebarThemeConfig {
    /** Let the reader hide the sidebar. */
    var hideable: Boolean?

    /** Collapse other categories when one is expanded. */
    var autoCollapseCategories: Boolean?
}

/** Upstream `ThemeConfig['blog']`. */
external interface BlogThemeConfig {
    /** Blog sidebar behaviour. */
    var sidebar: BlogSidebarThemeConfig?
}

/** Upstream `ThemeConfig['blog']['sidebar']`. */
external interface BlogSidebarThemeConfig {
    /** Group posts by year in the sidebar. */
    var groupByYear: Boolean?
}

/** Upstream `ColorModeConfig`. */
external interface ColorModeConfig {
    /** Mode used on the first visit. */
    var defaultMode: ColorMode?

    /** Hide the light/dark switch. */
    var disableSwitch: Boolean?

    /** Follow the operating system setting instead of [defaultMode]. */
    var respectPrefersColorScheme: Boolean?
}

/** Upstream `ColorMode`. */
sealed external interface ColorMode

/** Wraps a value coming from JavaScript. Prefer [ColorModes]. */
fun ColorMode(value: String): ColorMode = unsafeCast<ColorMode>(value)

/** The literals of [ColorMode]. */
object ColorModes {
    /** Light theme. */
    inline val light: ColorMode get() = ColorMode("light")

    /** Dark theme. */
    inline val dark: ColorMode get() = ColorMode("dark")
}

/** Upstream `AnnouncementBarConfig`. */
external interface AnnouncementBarConfig {
    /** Id used to remember that the reader dismissed the bar. */
    var id: String?

    /** Markup shown in the bar. */
    var content: String?

    /** Background colour, any CSS colour. */
    var backgroundColor: String?

    /** Text colour, any CSS colour. */
    var textColor: String?

    /** Let the reader close the bar. */
    var isCloseable: Boolean?
}

/** Upstream `PrismConfig`. */
external interface PrismConfig {
    /** Highlighting theme for light mode. */
    var theme: PrismTheme?

    /** Highlighting theme for dark mode. */
    var darkTheme: PrismTheme?

    /** Language assumed when a fence has none. */
    var defaultLanguage: String?

    /** Extra languages to load on top of the common set, e.g. `"rust"`, `"sql"`. */
    var additionalLanguages: Array<String>?

    /** Comment directives that highlight or hide lines. */
    var magicComments: Array<MagicCommentConfig>?
}

/**
 * Upstream `PrismTheme` from `prism-react-renderer`.
 *
 * Use [PrismThemes] for the bundled themes instead of writing one by hand.
 */
external interface PrismTheme

/** Upstream `MagicCommentConfig`. */
external interface MagicCommentConfig {
    /** Class name applied to the commented lines. */
    var className: String?

    /** The comment text that marks a single line. */
    var line: String?

    /** Markers for a block of lines. */
    var block: MagicCommentBlock?
}

/** Upstream `MagicCommentConfig['block']`. */
external interface MagicCommentBlock {
    /** Start marker. */
    var start: String

    /** End marker. */
    var end: String
}

/** Upstream `TableOfContents`. */
external interface TableOfContents {
    /** Smallest heading level listed. */
    var minHeadingLevel: Double?

    /** Largest heading level listed. */
    var maxHeadingLevel: Double?
}

/** Upstream `ThemeConfigAlgolia` from `@docusaurus/theme-search-algolia`. */
external interface AlgoliaConfig {
    /** Algolia application id. */
    var appId: String

    /** Search-only API key. */
    var apiKey: String

    /** Index name. */
    var indexName: String

    /** Input placeholder. */
    var placeholder: String?

    /** Search only inside the current docs version and language. */
    var contextualSearch: Boolean?

    /** Path of the search results page; `false` / `null` disables it. */
    var searchPagePath: StringOrFalseOrNull?

    /** Regexp marking links that leave the site. */
    var externalUrlRegex: String?

    /** Rewrite the path of a search result before navigating. */
    var replaceSearchResultPathname: ReplaceSearchResultPathname?

    /** Query typed into the search box on load. */
    var initialQuery: String?

    /** Ask AI configuration; `string` is the short form. */
    var askAi: StringOrAskAiConfig?
}

/** Upstream `string | AskAiConfig`. */
external interface StringOrAskAiConfig

/** Wraps the short string form. */
fun StringOrAskAiConfig(value: String): StringOrAskAiConfig = unsafeCast<StringOrAskAiConfig>(value)

/** Wraps the object form. */
fun StringOrAskAiConfig(value: AskAiConfig): StringOrAskAiConfig = unsafeCast<StringOrAskAiConfig>(value)

/** Upstream `AskAiConfig`. */
external interface AskAiConfig {
    /** Index name. */
    var indexName: String?

    /** Search-only API key. */
    var apiKey: String?

    /** Algolia application id. */
    var appId: String?

    /** Assistant id. */
    var assistantId: String?

    /** Suggested questions offered in the UI. */
    var suggestedQuestions: Boolean?
}

/** Upstream `{from: string; to: string}`. */
external interface ReplaceSearchResultPathname {
    /** Prefix to replace. */
    var from: String

    /** Replacement prefix. */
    var to: String
}

/** Upstream `string | false | null`. */
external interface StringOrFalseOrNull

/** Wraps the string branch. */
fun StringOrFalseOrNull(value: String): StringOrFalseOrNull = unsafeCast<StringOrFalseOrNull>(value)

/** Wraps the `false` branch. */
fun StringOrFalseOrNull(value: Boolean): StringOrFalseOrNull = unsafeCast<StringOrFalseOrNull>(value)

/** Upstream `ThemeConfig['mermaid']` from `@docusaurus/theme-mermaid`. */
external interface MermaidThemeConfig {
    /** Diagram theme per colour mode. */
    var theme: MermaidThemeByColorMode?

    /** Mermaid configuration, passed straight to `mermaid.initialize`. Upstream type is mermaid's `Config`. */
    var options: JsAny?
}

/** Upstream `{light: mermaidAPI.Theme; dark: mermaidAPI.Theme}`. */
external interface MermaidThemeByColorMode {
    /** Theme used in light mode. */
    var light: MermaidTheme?

    /** Theme used in dark mode. */
    var dark: MermaidTheme?
}

/** Upstream `mermaidAPI.Theme`. */
sealed external interface MermaidTheme

/** Wraps a value coming from JavaScript. Prefer [MermaidThemes]. */
fun MermaidTheme(value: String): MermaidTheme = unsafeCast<MermaidTheme>(value)

/** The literals of [MermaidTheme]. */
object MermaidThemes {
    /** The default mermaid look. */
    inline val default: MermaidTheme get() = MermaidTheme("default")

    /** Neutral greys. */
    inline val neutral: MermaidTheme get() = MermaidTheme("neutral")

    /** Dark background. */
    inline val dark: MermaidTheme get() = MermaidTheme("dark")

    /** Bright colours. */
    inline val forest: MermaidTheme get() = MermaidTheme("forest")

    /** Base colours. */
    inline val base: MermaidTheme get() = MermaidTheme("base")
}
