package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.createElement
import web.components.CustomElementReference
import web.components.HTMLSlotElement
import web.cssom.ClassName
import web.dom.TagName
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLElement

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
        heading = TagName<HTMLHeadingElement>("h2").createElement {
            className = ClassName("ktjs-section-title")
        }
        grid = TagName<HTMLDivElement>("div").createElement {
            className = ClassName("ktjs-feature-grid")
            appendChild(TagName<HTMLSlotElement>("slot").createElement())
        }
        val inner = TagName<HTMLDivElement>("div").createElement {
            className = ClassName("ktjs-section-inner")
            appendChild(heading)
            appendChild(grid)
        }
        val section = TagName<HTMLElement>("section").createElement {
            className = ClassName(
                if (hasAttribute("tinted")) "ktjs-section ktjs-section-tint" else "ktjs-section",
            )
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
