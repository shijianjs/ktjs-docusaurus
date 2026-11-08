package io.github.shijianjs.ktjs.docusaurus.theme

import io.github.shijianjs.ktjs.docusaurus.config.StringOrDouble
import kotlin.js.JsAny
import js.reflect.unsafeCast

/**
 * Upstream `MultiColumnFooter | SimpleFooter`.
 *
 * A footer with `links: FooterColumnItem[]` renders columns; one with `links: FooterLinkItem[]`
 * renders a single row. Build it with [Footer]:
 *
 * ```kotlin
 * footer = Footer(unsafeJso<MultiColumnFooter> {
 *     style = FooterStyles.dark
 *     links = arrayOf(/* columns */)
 * })
 * ```
 */
external interface Footer

/** Wraps the multi-column branch. */
inline fun Footer(value: MultiColumnFooter): Footer = unsafeCast<Footer>(value)

/** Wraps the single-row branch. */
inline fun Footer(value: SimpleFooter): Footer = unsafeCast<Footer>(value)

/** Upstream `'light' | 'dark'`. */
sealed external interface FooterStyle

/** Wraps a value coming from JavaScript. Prefer [FooterStyles]. */
inline fun FooterStyle(value: String): FooterStyle = unsafeCast<FooterStyle>(value)

/** The literals of [FooterStyle]. */
object FooterStyles {
    /** Light background. */
    inline val light: FooterStyle get() = FooterStyle("light")

    /** Dark background. */
    inline val dark: FooterStyle get() = FooterStyle("dark")
}

/** The fields both footer shapes share. */
external interface FooterBase {
    /** `'light'` or `'dark'`. */
    var style: FooterStyle?

    /** Logo shown above the links. */
    var logo: FooterLogo?

    /** Copyright line, rendered as HTML. Defaults to the site title. */
    var copyright: String?
}

/** A footer made of titled columns. */
external interface MultiColumnFooter : FooterBase {
    /** One entry per column. */
    var links: Array<FooterColumnItem>
}

/** A footer made of a single row of links. */
external interface SimpleFooter : FooterBase {
    /** The row's links. */
    var links: Array<FooterLinkItem>
}

/** Upstream `FooterColumnItem`. */
external interface FooterColumnItem {
    /** Column heading; `null` renders the column without one. */
    var title: String?

    /** Extra class name for the column. */
    var className: String?

    /** Links in the column. */
    var items: Array<FooterLinkItem>
}

/** Upstream `FooterLinkItem`. */
external interface FooterLinkItem {
    /** Visible text. Required unless [html] is set. */
    var label: String?

    /** Extra class name. */
    var className: String?

    /** Internal path. One of [to] and [href] is required. */
    var to: String?

    /** External URL. */
    var href: String?

    /** Raw markup instead of a label. */
    var html: String?

    /** `string`: prefix [href] with the site's `baseUrl`. Upstream declares it as `string`. */
    var prependBaseUrlToHref: String?
}

/** Upstream `FooterLogo`. */
external interface FooterLogo {
    /** Alt text, read by screen readers. */
    var alt: String?

    /** Image URL, resolved against a static directory. */
    var src: String

    /** Image URL used in dark mode. */
    var srcDark: String?

    /** Where the logo links to. */
    var href: String?

    /** Rendered width. `string | number`. */
    var width: StringOrDouble?

    /** Rendered height. `string | number`. */
    var height: StringOrDouble?

    /** Link target, e.g. `_self`. */
    var target: String?

    /** Inline style object. Upstream type is `object`. */
    var style: js.objects.Record<JsAny, String>?

    /** Extra class name. */
    var className: String?
}
