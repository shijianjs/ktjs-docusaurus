package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.attachStyledShadow
import io.github.shijianjs.ktjs.dom.createElement
import web.components.CustomElementReference
import web.dom.TagName
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * `<ktjs-next-steps section-title="…">` with `<ktjs-step>` children.
 *
 * Same shape as [`KtjsFeatures`]: a heading and a grid whose default slot holds the child cards.
 */
class KtjsNextSteps : CustomElementAdapter() {
    private val heading: HTMLHeadingElement
    private val grid: HTMLDivElement

    init {
        val shadow = attachStyledShadow(HOME_STYLE_SHEET)
        heading = HtmlTagName.h2.createElement {
            classList.add("ktjs-section-title")
        }
        grid = HtmlTagName.div.createElement {
            classList.add("ktjs-next-grid")
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

    companion object : CustomElementReference<KtjsNextSteps>(KtjsNextSteps::class.js) {
        val NAME: TagName<KtjsNextSteps> = TagName(HomeMarkup.NEXT_STEPS)
    }
}
