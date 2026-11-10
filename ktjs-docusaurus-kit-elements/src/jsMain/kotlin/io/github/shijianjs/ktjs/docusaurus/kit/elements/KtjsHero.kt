package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.createElement
import web.components.CustomElementReference
import web.components.HTMLSlotElement
import web.cssom.ClassName
import web.dom.TagName
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLImageElement
import web.html.HTMLParagraphElement
import web.html.HTMLSpanElement
import web.html.HTMLElement

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
        val shadow = attachHomeShadow()

        logo = TagName<HTMLImageElement>("img").createElement {
            className = ClassName("ktjs-logo")
            alt = ""
        }
        heading = TagName<HTMLHeadingElement>("h1").createElement {
            className = ClassName("ktjs-title")
        }
        tagline = TagName<HTMLParagraphElement>("p").createElement {
            className = ClassName("ktjs-tagline")
        }
        val stage = TagName<HTMLSpanElement>("span").createElement {
            className = ClassName("ktjs-logo-stage")
            appendChild(logo)
        }
        actions = TagName<HTMLDivElement>("div").createElement {
            className = ClassName("ktjs-actions")
            appendChild(TagName<HTMLSlotElement>("slot").createElement { name = "action" })
        }
        badges = TagName<HTMLDivElement>("div").createElement {
            className = ClassName("ktjs-badges")
            appendChild(TagName<HTMLSlotElement>("slot").createElement { name = "badge" })
        }
        val inner = TagName<HTMLDivElement>("div").createElement {
            className = ClassName("ktjs-hero-inner")
            appendChild(stage)
            appendChild(heading)
            appendChild(tagline)
            appendChild(actions)
            appendChild(badges)
        }

        val section = TagName<HTMLElement>("section").createElement {
            className = ClassName("ktjs-hero")
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
