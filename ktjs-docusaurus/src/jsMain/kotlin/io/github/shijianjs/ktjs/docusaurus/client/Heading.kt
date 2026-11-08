@file:JsModule("@theme/Heading")

package io.github.shijianjs.ktjs.docusaurus.client

import react.FC
import react.PropsWithChildren
import kotlin.js.JsName

/**
 * Renders a heading at the level the page needs, with the theme's anchor behaviour.
 *
 * Upstream source: `@docusaurus/theme-classic` (`theme/Heading`).
 */
external interface HeadingProps : PropsWithChildren {
    /** `h1` … `h6`. */
    var `as`: String?

    var id: String?

    var className: String?
}

/** The heading component itself. */
@JsName("default")
external val Heading: FC<HeadingProps>
