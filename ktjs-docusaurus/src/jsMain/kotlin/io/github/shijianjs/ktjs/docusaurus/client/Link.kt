@file:JsModule("@docusaurus/Link")

package io.github.shijianjs.ktjs.docusaurus.client

import react.FC
import react.PropsWithChildren
import kotlin.js.JsName

/**
 * The router-aware link: an internal `to` navigates client-side, an external `href` opens a normal
 * anchor.
 *
 * Use it instead of `<a>` for anything pointing inside the site; the configured `baseUrl` and the
 * trailing-slash policy are applied by the component.
 *
 * Upstream source: `@docusaurus/core` (`Link`).
 */
external interface LinkProps : PropsWithChildren {
    /** Internal target, resolved against `baseUrl`. */
    var to: String?

    /** External target; takes priority over [to]. */
    var href: String?

    /** `target` of the rendered anchor. */
    var target: String?

    /** `rel` of the rendered anchor. */
    var rel: String?

    var className: String?

    var activeClassName: String?

    /** Renders the active link with the matching class name. */
    var isNavLink: Boolean?
}

/** The link component itself. */
@JsName("default")
external val Link: FC<LinkProps>
