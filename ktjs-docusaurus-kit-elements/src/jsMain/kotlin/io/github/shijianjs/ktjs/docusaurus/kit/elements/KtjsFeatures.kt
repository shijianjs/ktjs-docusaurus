package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.attachStyledShadow
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.inlineStyle
import web.components.CustomElementReference
import web.cssom.Display
import web.cssom.rem
import web.dom.TagName
import web.html.HTMLDivElement
import web.html.HtmlTagName

/**
 * `<ktjs-features section-title="…" tinted>` with `<ktjs-feature>` children.
 *
 * Renders into a shadow root: the shared section band ([homeSection]) plus a grid whose default slot
 * holds the child cards. Each `<ktjs-feature>` renders its own card, so this element only owns the
 * layout of the grid.
 */
class KtjsFeatures : CustomElementAdapter() {
    private val section: HomeSection
    private val grid: HTMLDivElement

    init {
        val shadow = attachStyledShadow(HOME_RESET)
        grid = HtmlTagName.div.createElement {
            inlineStyle.apply {
                display = Display.grid
                gridTemplateColumns = autoFitGrid(17.rem)
                rowGap = 1.25.rem
                columnGap = 1.25.rem
                marginTop = 2.75.rem
            }
            appendChild(HtmlTagName.slot.createElement())
        }
        section = homeSection(tinted = hasAttribute("tinted"))
        section.content.appendChild(grid)
        shadow.appendChild(section.root)
    }

    override fun connectedCallback() {
        section.heading.textContent = getAttribute("section-title").orEmpty()
    }

    companion object : CustomElementReference<KtjsFeatures>(KtjsFeatures::class.js) {
        val NAME: TagName<KtjsFeatures> = TagName(HomeMarkup.FEATURES)
    }
}
