package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.createElement
import web.components.CustomElementReference
import web.cssom.ClassName
import web.dom.TagName
import web.html.HTMLAnchorElement
import web.html.HTMLImageElement
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
        image = TagName<HTMLImageElement>("img").createElement {
            className = ClassName("ktjs-badge-image")
        }
        anchor = TagName<HTMLAnchorElement>("a").createElement {
            className = ClassName("ktjs-badge")
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
