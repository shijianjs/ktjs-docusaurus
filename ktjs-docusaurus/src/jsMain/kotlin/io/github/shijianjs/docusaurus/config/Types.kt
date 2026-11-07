package io.github.shijianjs.docusaurus.config

import js.reflect.unsafeCast

/**
 * Upstream `'ignore' | 'log' | 'warn' | 'throw'`.
 *
 * Used by every "what to do when something is broken" option, such as
 * [Config.onBrokenLinks]. Prefer the constants in [ReportingSeverities].
 */
sealed external interface ReportingSeverity

/** Wraps a value coming from JavaScript. Prefer [ReportingSeverities]. */
fun ReportingSeverity(value: String): ReportingSeverity = unsafeCast<ReportingSeverity>(value)

/** The literals of [ReportingSeverity]. */
object ReportingSeverities {
    /** Report nothing. */
    inline val ignore: ReportingSeverity get() = ReportingSeverity("ignore")

    /** Print the problem without failing the build. */
    inline val log: ReportingSeverity get() = ReportingSeverity("log")

    /** Print a warning; the build still succeeds. */
    inline val warn: ReportingSeverity get() = ReportingSeverity("warn")

    /** Fail the build. This is Docusaurus' default for broken links. */
    inline val `throw`: ReportingSeverity get() = ReportingSeverity("throw")
}

/** Upstream `'browser' | 'hash'`. */
sealed external interface RouterType

/** Wraps a value coming from JavaScript. Prefer [RouterTypes]. */
fun RouterType(value: String): RouterType = unsafeCast<RouterType>(value)

/** The literals of [RouterType]. */
object RouterTypes {
    /** Browser history routing; one static HTML file per page. */
    inline val browser: RouterType get() = RouterType("browser")

    /** Hash routing; a single page application with no static site generation. */
    inline val hash: RouterType get() = RouterType("hash")
}

/** Upstream `'ltr' | 'rtl'`. */
sealed external interface Direction

/** Wraps a value coming from JavaScript. Prefer [Directions]. */
fun Direction(value: String): Direction = unsafeCast<Direction>(value)

/** The literals of [Direction]. */
object Directions {
    /** Left to right. */
    inline val ltr: Direction get() = Direction("ltr")

    /** Right to left. */
    inline val rtl: Direction get() = Direction("rtl")
}

/** Upstream `'localStorage' | 'sessionStorage'`. */
sealed external interface SiteStorageType

/** Wraps a value coming from JavaScript. Prefer [SiteStorageTypes]. */
fun SiteStorageType(value: String): SiteStorageType = unsafeCast<SiteStorageType>(value)

/** The literals of [SiteStorageType]. */
object SiteStorageTypes {
    /** Persists across tabs and sessions; the Docusaurus default. */
    inline val localStorage: SiteStorageType get() = SiteStorageType("localStorage")

    /** Cleared when the tab closes. */
    inline val sessionStorage: SiteStorageType get() = SiteStorageType("sessionStorage")
}

/**
 * Upstream `'mdx' | 'md' | 'detect'`.
 *
 * @see MarkdownConfig.format
 */
sealed external interface MarkdownFormat

/** Wraps a value coming from JavaScript. Prefer [MarkdownFormats]. */
fun MarkdownFormat(value: String): MarkdownFormat = unsafeCast<MarkdownFormat>(value)

/** The literals of [MarkdownFormat]. */
object MarkdownFormats {
    /** MDX: Markdown with JSX support. */
    inline val mdx: MarkdownFormat get() = MarkdownFormat("mdx")

    /** CommonMark, no JSX. */
    inline val md: MarkdownFormat get() = MarkdownFormat("md")

    /** Pick per file from its extension. */
    inline val detect: MarkdownFormat get() = MarkdownFormat("detect")
}

/** Upstream `boolean | string`, as used by `storage.namespace`. */
external interface BooleanOrString

/** Wraps a [Boolean] branch. */
fun BooleanOrString(value: Boolean): BooleanOrString = unsafeCast<BooleanOrString>(value)

/** Wraps a [String] branch. */
fun BooleanOrString(value: String): BooleanOrString = unsafeCast<BooleanOrString>(value)

/** Upstream `string | false`, as used by `docs.sidebarPath`. */
external interface StringOrFalse

/** Wraps the [String] branch. */
fun StringOrFalse(value: String): StringOrFalse = unsafeCast<StringOrFalse>(value)

/** Wraps the `false` branch. */
fun StringOrFalse(value: Boolean): StringOrFalse = unsafeCast<StringOrFalse>(value)

/** Upstream `string | number`, as used by the logo `width` / `height`. */
external interface StringOrDouble

/** Wraps a [String] branch, e.g. `"100%"`. */
fun StringOrDouble(value: String): StringOrDouble = unsafeCast<StringOrDouble>(value)

/** Wraps a [Double] branch, e.g. `32.0`. */
fun StringOrDouble(value: Double): StringOrDouble = unsafeCast<StringOrDouble>(value)
