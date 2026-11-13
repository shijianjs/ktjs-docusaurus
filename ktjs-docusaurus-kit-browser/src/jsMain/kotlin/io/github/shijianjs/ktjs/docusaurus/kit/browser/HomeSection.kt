package io.github.shijianjs.ktjs.docusaurus.kit.browser

import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import js.reflect.unsafeCast
import web.cssom.Auto
import web.cssom.GridTemplateTracks
import web.cssom.Length
import web.cssom.Margin
import web.cssom.minus
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.clamp
import web.cssom.em
import web.cssom.pct
import web.cssom.px
import web.cssom.rem
import web.cssom.vw
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * The band a card section sits in: a full-bleed surface, a centred content column and its title.
 *
 * `ktjs-features` and `ktjs-next-steps` are the same band with different cards inside, and the band
 * is one look that has to stay identical in both — that is real sharing, so it is built once here.
 * The cards themselves belong to the elements that render them.
 *
 * The `tinted` background is not set here: the section's `attr` delegate owns it, so a later change
 * to the attribute repaints the band.
 */
internal class HomeSection(
    val root: HTMLElement,
    val content: HTMLDivElement,
    val heading: HTMLHeadingElement,
)

internal fun homeSection(): HomeSection {
    val heading = HtmlTagName.h2.createElement {
        inlineStyle.apply {
            margin = 0.px
            textAlign = TextAlign.center
            fontSize = clamp(1.5.rem, 3.vw, 2.rem)
            letterSpacing = (-0.01).em
        }
    }
    val content = HtmlTagName.div.createElement {
        inlineStyle.apply {
            maxWidth = 72.rem
            margin = Margin(0.px, Auto.auto)
            paddingInline = clamp(1.rem, 4.vw, 1.5.rem)
        }
        appendChild(heading)
    }
    val root = HtmlTagName.section.createElement<HTMLElement> {
        inlineStyle.apply {
            // Reach the window edges by measuring against the viewport: the MDX page caps its
            // container at `--ifm-container-width-xl` on wide screens, so `100%` stops short.
            // `100vw` includes the desktop scrollbar's gutter, which is why the page clips
            // horizontal overflow (see the site's `custom.css`) instead of growing a scrollbar.
            width = 100.vw
            marginInline = 50.pct - 50.vw
            padding = Padding(clamp(3.5.rem, 6.vw, 4.5.rem), 0.px)
        }
        appendChild(content)
    }
    return HomeSection(root, content, heading)
}

/**
 * `repeat(auto-fit, minmax(min(<minColumn>, 100%), 1fr))` — as many columns as fit, never narrower
 * than [minColumn], never overflowing a phone.
 *
 * Written as text because cssom's `minmax` takes `GridAutoTracks` on both sides and a `min()` of two
 * lengths is not one; this is the escape hatch `unsafeCast` exists for.
 */
internal fun autoFitGrid(minColumn: Length): GridTemplateTracks =
    unsafeCast("repeat(auto-fit, minmax(min($minColumn, 100%), 1fr))")
