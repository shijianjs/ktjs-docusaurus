package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.createElement
import web.components.CustomElementReference
import web.dom.TagName
import web.html.HTMLAnchorElement
import web.html.HTMLImageElement
import web.html.HtmlTagName
import web.window.WindowTarget
import web.window._blank

/**
 * `<ktjs-badge href="…" src="…" alt="…">` — a shields.io-style image inside an external link.
 */
class KtjsBadge : CustomElementAdapter() {
    private val anchor: HTMLAnchorElement
    private val image: HTMLImageElement

    init {
        val shadow = attachHomeShadow()
        image = HtmlTagName.img.createElement {
            classList.add("ktjs-badge-image")
        }
        anchor = HtmlTagName.a.createElement {
            classList.add("ktjs-badge")
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
