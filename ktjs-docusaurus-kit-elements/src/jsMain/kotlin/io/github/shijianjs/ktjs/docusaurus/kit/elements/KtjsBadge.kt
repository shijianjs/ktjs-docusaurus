package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.attachStyledShadow
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.inlineStyle
import web.components.CustomElementReference
import web.cssom.Auto
import web.cssom.Display
import web.cssom.px
import web.dom.TagName
import web.html.HTMLAnchorElement
import web.html.HTMLImageElement
import web.html.HtmlTagName
import web.window.WindowTarget
import web.window._blank

/**
 * `<ktjs-badge href="…" src="…" alt="…">` — a shields.io-style image inside an external link.
 *
 * No stylesheet: an image in a row is exactly what inline styles can say.
 */
class KtjsBadge : CustomElementAdapter() {
    private val anchor: HTMLAnchorElement
    private val image: HTMLImageElement

    init {
        val shadow = attachStyledShadow(HOME_RESET)
        image = HtmlTagName.img.createElement {
            inlineStyle.apply {
                display = Display.block
                width = Auto.auto
                height = 20.px
            }
        }
        anchor = HtmlTagName.a.createElement {
            inlineStyle.display = Display.inlineFlex
            target = WindowTarget._blank
            rel = "noopener noreferrer"
            appendChild(image)
        }
        shadow.appendChild(anchor)
    }

    override fun connectedCallback() {
        anchor.href = getAttribute("href").orEmpty()
        image.src = getAttribute("src").orEmpty()
        image.alt = getAttribute("alt").orEmpty()
    }

    companion object : CustomElementReference<KtjsBadge>(KtjsBadge::class.js) {
        val NAME: TagName<KtjsBadge> = TagName(HomeMarkup.BADGE)
    }
}
