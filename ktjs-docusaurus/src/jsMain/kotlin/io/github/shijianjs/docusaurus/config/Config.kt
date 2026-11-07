package io.github.shijianjs.docusaurus.config

import io.github.shijianjs.docusaurus.theme.ThemeConfig
import js.objects.Record
import kotlin.js.JsAny
import kotlin.js.Promise
import js.reflect.unsafeCast

/**
 * Upstream `Config`: the object a `docusaurus.config.ts` exports.
 *
 * It is `DocusaurusConfig` with everything optional except [title], [url] and [baseUrl]. Docusaurus
 * validates and fills in the rest at load time.
 *
 * Build it with `unsafeJso<Config>`:
 *
 * ```kotlin
 * val config = unsafeJso<Config> {
 *     title = "ktjs-docusaurus"
 *     url = "https://shijianjs.github.io"
 *     baseUrl = "/ktjs-docusaurus/"
 *     presets = arrayOf(classicPreset(unsafeJso { /* … */ }))
 * }
 * ```
 *
 * @see <a href="https://docusaurus.io/docs/api/docusaurus-config">Docusaurus configuration</a>
 */
external interface Config {
    /** Website title, used in metadata and as the browser tab title. */
    var title: String

    /** Top-level hostname, e.g. `https://shijianjs.github.io`. */
    var url: String

    /**
     * Path after the host, with both a leading and a trailing slash, e.g. `/ktjs-docusaurus/`.
     *
     * Use `/` for a site served from the domain root.
     */
    var baseUrl: String

    /** Path to the favicon, relative to a static directory or an absolute URL. */
    var favicon: String?

    /**
     * Upstream `boolean | undefined`.
     *
     * `true` adds trailing slashes to URLs and emits `<path>/index.html`; `false` removes them and
     * emits `<path>.html`; `null` leaves URLs untouched and still emits `<path>/index.html`.
     */
    var trailingSlash: Boolean?

    /** Localization configuration. */
    var i18n: I18nConfig?

    /** Site-wide browser storage options. */
    var storage: StorageConfig?

    /** Future flags and experimental features. */
    var future: FutureConfig?

    /** Adds `<meta name="robots" content="noindex, nofollow">` to every page. */
    var noIndex: Boolean?

    /** Behaviour when a link points at a page that does not exist. */
    var onBrokenLinks: ReportingSeverity?

    /** Behaviour when a link points at an anchor that does not exist. */
    var onBrokenAnchors: ReportingSeverity?

    /** Behaviour when a Markdown link cannot be resolved to a file. */
    var onBrokenMarkdownLinks: ReportingSeverity?

    /** Behaviour when two routes resolve to the same path. */
    var onDuplicateRoutes: ReportingSeverity?

    /** Tagline shown on the home page by the classic theme. */
    var tagline: String?

    /** GitHub user or organization that owns the repository; used by `docusaurus deploy`. */
    var organizationName: String?

    /** GitHub repository name; used by `docusaurus deploy`. */
    var projectName: String?

    /** Branch the static files are pushed to by `docusaurus deploy`. */
    var deploymentBranch: String?

    /** Hostname of the deployment server, for GitHub Enterprise. */
    var githubHost: String?

    /** Port of the deployment server, for GitHub Enterprise. */
    var githubPort: String?

    /** Theme configuration; the classic theme's fields are declared in [ThemeConfig]. */
    var themeConfig: ThemeConfig?

    /** Plugins. `null` entries are allowed and mean "no plugin". */
    var plugins: Array<PluginConfig?>?

    /** Themes. A theme is a plugin that also provides theme components. */
    var themes: Array<PluginConfig?>?

    /** Presets. The classic preset is available through `classicPreset`. */
    var presets: Array<PresetConfig?>?

    /**
     * Custom fields exposed to client code through `useDocusaurusContext()`.
     *
     * Docusaurus rejects unknown top-level fields, so site-specific values belong here.
     */
    var customFields: Record<JsAny, JsAny>?

    /** Directories copied verbatim into the build output. Defaults to `["static"]`. */
    var staticDirectories: Array<String>?

    /** Tags inserted into the HTML `<head>` of every page. */
    var headTags: Array<HtmlTagObject>?

    /** Scripts loaded by every page. Render-blocking unless `async` / `defer` is set. */
    var scripts: Array<ScriptOrString>?

    /** Stylesheets loaded by every page. */
    var stylesheets: Array<StylesheetOrString>?

    /** Client modules (paths to `.js` / `.ts` files) loaded on every page. */
    var clientModules: Array<String>?

    /** Eta template used to render the HTML shell. */
    var ssrTemplate: String?

    /** Delimiter between the page title and the site title. Defaults to `|`. */
    var titleDelimiter: String?

    /** Shows a banner when the site's CSS or JS cannot be loaded, usually a wrong [baseUrl]. */
    var baseUrlIssueBanner: Boolean?

    /** Bundler-related options. */
    var webpack: WebpackConfig?

    /** Markdown-related options. */
    var markdown: MarkdownConfig?
}

/**
 * Upstream `I18nConfig`, as accepted in a site config (everything but [defaultLocale] is optional).
 */
external interface I18nConfig {
    /** Locale served at the root of the site. */
    var defaultLocale: String

    /** Every locale the site is built for. */
    var locales: Array<String>

    /** Directory holding the per-locale plugin content, relative to the site directory. */
    var path: String?

    /** Per-locale overrides. */
    var localeConfigs: Record<JsAny, I18nLocaleConfig>?
}

/** Upstream `I18nLocaleConfig`. */
external interface I18nLocaleConfig {
    /** Label shown in the locale dropdown. */
    var label: String?

    /** BCP 47 language tag for `<html lang>` and `hreflang`. */
    var htmlLang: String?

    /** Text direction. */
    var direction: Direction?

    /** Calendar used to compute date eras; `gregory` unless the locale says otherwise. */
    var calendar: String?

    /** Root folder of this locale's plugin content, relative to [I18nConfig.path]. */
    var path: String?

    /** Whether to translate this locale. Defaults to "only if the folder exists". */
    var translate: Boolean?

    /** Per-locale site URL, for deployments on distinct domains. */
    var url: String?

    /** Per-locale base URL override. */
    var baseUrl: String?
}

/** Upstream `StorageConfig`. */
external interface StorageConfig {
    /** Browser storage implementation. */
    var type: SiteStorageType?

    /** `boolean | string`: namespace the keys by site, or use this explicit prefix. */
    var namespace: BooleanOrString?
}

/**
 * Upstream `FutureConfig`.
 *
 * @see <a href="https://docusaurus.io/docs/api/docusaurus-config#future">Future flags</a>
 */
external interface FutureConfig {
    /** `boolean | Partial<FutureV4Config>`: turn the v4 behaviour on, or pick individual flags. */
    var v4: BooleanOrFutureV4Config?

    /** `boolean | Partial<FasterConfig>`: turn the faster pipeline on, or pick individual flags. */
    var faster: BooleanOrFasterConfig?

    /** Experimental router. */
    var experimental_router: RouterType?

    /** `VcsPreset | VcsConfig | boolean`: how file creation/last-update info is read. */
    var experimental_vcs: VcsPresetOrVcsConfigOrBoolean?
}

/** Upstream `FutureV4Config`: individual Docusaurus v4 behaviours. */
external interface FutureV4Config {
    /** Drop the legacy `head` attribute from post-build HTML. */
    var removeLegacyPostBuildHeadAttribute: Boolean?

    /** Wrap theme CSS in cascade layers. */
    var useCssCascadeLayers: Boolean?

    /** Namespace browser storage keys per site. */
    var siteStorageNamespacing: Boolean?

    /** Enable the faster pipeline by default. */
    var fasterByDefault: Boolean?

    /** Disable the MDX 1 compatibility flags by default. */
    var mdx1CompatDisabledByDefault: Boolean?
}

/** Upstream `FasterConfig`: the individual pieces of the faster pipeline. */
external interface FasterConfig {
    /** Use SWC for the JS loader. */
    var swcJsLoader: Boolean?

    /** Use SWC as the JS minimizer. */
    var swcJsMinimizer: Boolean?

    /** Use SWC as the HTML minimizer. */
    var swcHtmlMinimizer: Boolean?

    /** Use Lightning CSS as the CSS minimizer. */
    var lightningCssMinimizer: Boolean?

    /** Share the MDX compile cache across the client and server compilers. */
    var mdxCrossCompilerCache: Boolean?

    /** Use Rspack instead of webpack. */
    var rspackBundler: Boolean?

    /** Keep an Rspack persistent cache between builds. */
    var rspackPersistentCache: Boolean?

    /** Render static pages in worker threads. */
    var ssgWorkerThreads: Boolean?

    /** Use the eager git VCS preset. */
    var gitEagerVcs: Boolean?
}

/** Upstream `VcsChangeInfo`. */
external interface VcsChangeInfo {
    /** Unix timestamp in milliseconds. */
    var timestamp: Double

    /** Author name. */
    var author: String
}

/** Upstream `VscInitializeParams`. */
external interface VscInitializeParams {
    /** Absolute path of the site directory. */
    var siteDir: String
}

/** Upstream `VcsConfig`: custom file history lookup. */
external interface VcsConfig {
    /** Warm up the VCS; called once, synchronously, before the build reads any file. */
    var initialize: (VscInitializeParams) -> Unit

    /** Creation info of a file, or `null` when unknown. */
    var getFileCreationInfo: (String) -> Promise<VcsChangeInfo?>

    /** Last update info of a file, or `null` when unknown. */
    var getFileLastUpdateInfo: (String) -> Promise<VcsChangeInfo?>
}

/** Upstream `'git-ad-hoc' | 'git-eager' | 'hardcoded' | 'disabled' | 'default-v1' | 'default-v2'`. */
sealed external interface VcsPreset

/** Wraps a value coming from JavaScript. Prefer [VcsPresets]. */
fun VcsPreset(value: String): VcsPreset = unsafeCast<VcsPreset>(value)

/** The literals of [VcsPreset]. */
object VcsPresets {
    /** Read git lazily, one command per file. */
    inline val gitAdHoc: VcsPreset get() = VcsPreset("git-ad-hoc")

    /** Read the whole git history once, then answer from memory. */
    inline val gitEager: VcsPreset get() = VcsPreset("git-eager")

    /** Only use hardcoded front matter values. */
    inline val hardcoded: VcsPreset get() = VcsPreset("hardcoded")

    /** Never read file history. */
    inline val disabled: VcsPreset get() = VcsPreset("disabled")

    /** The Docusaurus v1 behaviour. */
    inline val defaultV1: VcsPreset get() = VcsPreset("default-v1")

    /** The Docusaurus v2 behaviour. */
    inline val defaultV2: VcsPreset get() = VcsPreset("default-v2")
}

/** Upstream `boolean | FutureV4Config`. */
external interface BooleanOrFutureV4Config

/** Wraps a [Boolean] branch. */
fun BooleanOrFutureV4Config(value: Boolean): BooleanOrFutureV4Config = unsafeCast<BooleanOrFutureV4Config>(value)

/** Wraps a [FutureV4Config] branch. */
fun BooleanOrFutureV4Config(value: FutureV4Config): BooleanOrFutureV4Config =
    unsafeCast<BooleanOrFutureV4Config>(value)

/** Upstream `boolean | FasterConfig`. */
external interface BooleanOrFasterConfig

/** Wraps a [Boolean] branch. */
fun BooleanOrFasterConfig(value: Boolean): BooleanOrFasterConfig = unsafeCast<BooleanOrFasterConfig>(value)

/** Wraps a [FasterConfig] branch. */
fun BooleanOrFasterConfig(value: FasterConfig): BooleanOrFasterConfig = unsafeCast<BooleanOrFasterConfig>(value)

/** Upstream `VcsPreset | VcsConfig | boolean`. */
external interface VcsPresetOrVcsConfigOrBoolean

/** Wraps a [VcsPreset] branch. */
fun VcsPresetOrVcsConfigOrBoolean(value: VcsPreset): VcsPresetOrVcsConfigOrBoolean =
    unsafeCast<VcsPresetOrVcsConfigOrBoolean>(value)

/** Wraps a [VcsConfig] branch. */
fun VcsPresetOrVcsConfigOrBoolean(value: VcsConfig): VcsPresetOrVcsConfigOrBoolean =
    unsafeCast<VcsPresetOrVcsConfigOrBoolean>(value)

/** Wraps a [Boolean] branch. */
fun VcsPresetOrVcsConfigOrBoolean(value: Boolean): VcsPresetOrVcsConfigOrBoolean =
    unsafeCast<VcsPresetOrVcsConfigOrBoolean>(value)

/** Upstream `WebpackConfig`. */
external interface WebpackConfig {
    /** `'babel'` for the built-in loader, or a custom rule factory. */
    var jsLoader: JsLoader?
}

/** Upstream `'babel' | ((isServer: boolean) => RuleSetRule)`. */
external interface JsLoader

/** Wraps the `'babel'` branch. */
fun JsLoader(value: String): JsLoader = unsafeCast<JsLoader>(value)

/** Wraps the custom-rule branch. The upstream return value is a webpack `RuleSetRule`. */
fun JsLoader(value: (isServer: Boolean) -> JsAny): JsLoader = unsafeCast<JsLoader>(value)

/**
 * Upstream `{attributes?: {[key: string]: string | boolean}; tagName: string; innerHTML?: string;
 * customElement?: boolean}`.
 */
external interface HtmlTagObject {
    /** Attributes of the tag, e.g. `{rel: "preconnect", href: "..."}`. */
    var attributes: Record<JsAny, BooleanOrString>?

    /** Tag name, e.g. `div`, `script`, `link`, `meta`. */
    var tagName: String

    /** Markup inserted inside the tag. */
    var innerHTML: String?

    /** Allow custom elements such as `<my-element>`. */
    var customElement: Boolean?
}

/** Upstream `string | {src: string; [key: string]: string | boolean | undefined}`. */
external interface ScriptOrString

/** Wraps the URL branch. */
fun ScriptOrString(value: String): ScriptOrString = unsafeCast<ScriptOrString>(value)

/** Wraps the object branch. */
fun ScriptOrString(value: ScriptConfig): ScriptOrString = unsafeCast<ScriptOrString>(value)

/** The object form of an entry in [Config.scripts]. */
external interface ScriptConfig {
    /** Script URL. */
    var src: String

    /** Load without blocking parsing. */
    var async: Boolean?

    /** Run after parsing. */
    var defer: Boolean?

    /** CORS mode. */
    var crossorigin: String?

    /** Subresource integrity hash. */
    var integrity: String?

    /** MIME type. */
    var type: String?

    /** Referrer policy. */
    var referrerpolicy: String?
}

/** Upstream `string | {href: string; [key: string]: string | boolean | undefined}`. */
external interface StylesheetOrString

/** Wraps the URL branch. */
fun StylesheetOrString(value: String): StylesheetOrString = unsafeCast<StylesheetOrString>(value)

/** Wraps the object branch. */
fun StylesheetOrString(value: StylesheetConfig): StylesheetOrString = unsafeCast<StylesheetOrString>(value)

/** The object form of an entry in [Config.stylesheets]. */
external interface StylesheetConfig {
    /** Stylesheet URL. */
    var href: String

    /** MIME type. */
    var type: String?

    /** Media query the sheet applies to. */
    var media: String?
}
