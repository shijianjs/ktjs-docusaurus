package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.createElement
import web.components.CustomElementReference
import web.dom.TagName
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * `<ktjs-features section-title="…" tinted>` with `<ktjs-feature>` children.
 *
 * Renders into a shadow root: a heading and a grid whose default slot holds the child cards. Each
 * `<ktjs-feature>` renders its own card, so this element only owns the layout.
 */
class KtjsFeatures : CustomElementAdapter() {
    private val heading: HTMLHeadingElement
    private val grid: HTMLDivElement

    init {
        val shadow = attachHomeShadow()
        heading = HtmlTagName.h2.createElement {
            classList.add("ktjs-section-title")
        }
        grid = HtmlTagName.div.createElement {
            classList.add("ktjs-feature-grid")
            appendChild(HtmlTagName.slot.createElement())
        }
        val inner = HtmlTagName.div.createElement {
            classList.add("ktjs-section-inner")
            appendChild(heading)
            appendChild(grid)
        }
        val section = HtmlTagName.section.createElement<HTMLElement> {
            classList.add("ktjs-section")
            if (hasAttribute("tinted")) classList.add("ktjs-section-tint")
            appendChild(inner)
        }
        shadow.appendChild(section)
    }

    override fun connectedCallback() {
        heading.textContent = getAttribute("section-title").orEmpty()
    }

    companion object : CustomElementReference<KtjsFeatures>(KtjsFeatures::class.js) {
        val NAME: TagName<KtjsFeatures> = TagName(HomeMarkup.FEATURES)
    }
}
