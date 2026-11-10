package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.attachStyledShadow
import io.github.shijianjs.ktjs.dom.createElement
import web.components.CustomElementReference
import web.components.HTMLSlotElement
import web.dom.TagName
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLImageElement
import web.html.HTMLParagraphElement
import web.html.HTMLSpanElement
import web.html.HTMLStyleElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * `<ktjs-hero logo-src="…" title="…" tagline="…">` with `<ktjs-action>` / `<ktjs-badge>` children.
 *
 * Renders into a shadow root. `logo-src` / `title` / `tagline` are read on connect; the child tags are
 * routed into their own slots (`action`, `badge`) by tag name, so MDX does not need a `slot`
 * attribute. Each child is a real custom element that renders itself.
 */
class KtjsHero : CustomElementAdapter() {
    private val logo: HTMLImageElement
    private val heading: HTMLHeadingElement
    private val tagline: HTMLParagraphElement
    private val actions: HTMLDivElement
    private val badges: HTMLDivElement

    init {
        val shadow = attachStyledShadow(HOME_STYLE_SHEET)

        logo = HtmlTagName.img.createElement {
            classList.add("ktjs-logo")
            alt = ""
        }
        heading = HtmlTagName.h1.createElement {
            classList.add("ktjs-title")
        }
        tagline = HtmlTagName.p.createElement {
            classList.add("ktjs-tagline")
        }
        val stage = HtmlTagName.span.createElement {
            classList.add("ktjs-logo-stage")
            appendChild(logo)
        }
        actions = HtmlTagName.div.createElement {
            classList.add("ktjs-actions")
            appendChild(HtmlTagName.slot.createElement<HTMLSlotElement> { name = "action" })
        }
        badges = HtmlTagName.div.createElement {
            classList.add("ktjs-badges")
            appendChild(HtmlTagName.slot.createElement<HTMLSlotElement> { name = "badge" })
        }
        val inner = HtmlTagName.div.createElement {
            classList.add("ktjs-hero-inner")
            appendChild(stage)
            appendChild(heading)
            appendChild(tagline)
            appendChild(actions)
            appendChild(badges)
        }

        val section = HtmlTagName.section.createElement<HTMLElement> {
            classList.add("ktjs-hero")
            appendChild(inner)
        }
        shadow.appendChild(section)
    }

    override fun connectedCallback() {
        getAttribute("logo-src")?.let { logo.src = it }
        heading.textContent = getAttribute("title").orEmpty()
        tagline.textContent = getAttribute("tagline").orEmpty()

        // Route the declarative children into their slots. Reading `children` keeps React's nodes in
        // place; the browser does the actual distribution through `slot`.
        for (index in 0 until children.length) {
            val child = children.item(index) ?: continue
            when (child.tagName.lowercase()) {
                HomeMarkup.ACTION -> child.setAttribute("slot", "action")
                HomeMarkup.BADGE -> child.setAttribute("slot", "badge")
            }
        }
    }

    companion object : CustomElementReference<KtjsHero>(KtjsHero::class.js) {
        val NAME: TagName<KtjsHero> = TagName(HomeMarkup.HERO)
    }
}
