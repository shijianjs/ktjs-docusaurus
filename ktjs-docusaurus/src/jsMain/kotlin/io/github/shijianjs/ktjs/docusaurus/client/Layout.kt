@file:JsModule("@theme/Layout")

package io.github.shijianjs.ktjs.docusaurus.client

import react.FC
import react.PropsWithChildren
import kotlin.js.JsName

/**
 * The site shell: navbar, page body, footer.
 *
 * Every page that is not a docs page wraps its content in this component. It renders no `<main>` of
 * its own, so the page supplies its own.
 *
 * Upstream source: `@docusaurus/theme-classic` (`theme/Layout`).
 */
external interface LayoutProps : PropsWithChildren {
    /** `<title>` of the page; Docusaurus appends the site title. */
    var title: String?

    /** `<meta name="description">` of the page. */
    var description: String?

    /** Drops the footer for this page. */
    var noFooter: Boolean?

    /** Extra class name for the page wrapper. */
    var wrapperClassName: String?
}

/** The layout component itself. */
@JsName("default")
external val Layout: FC<LayoutProps>
