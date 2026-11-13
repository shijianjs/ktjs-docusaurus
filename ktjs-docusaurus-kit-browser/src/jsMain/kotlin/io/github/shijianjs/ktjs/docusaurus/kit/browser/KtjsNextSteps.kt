package io.github.shijianjs.ktjs.docusaurus.kit.browser

import io.github.shijianjs.ktjs.dom.element.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.style.attachStyledShadow
import io.github.shijianjs.ktjs.dom.attribute.attr
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.style.inlineStyle
import kotlin.js.JsName
import web.components.CustomElementReference
import web.cssom.Auto
import web.cssom.Display
import web.cssom.Margin
import web.cssom.px
import web.cssom.rem
import web.dom.TagName
import web.html.HTMLDivElement
import web.html.HtmlTagName

/**
 * `<ktjs-next-steps section-title="…">` with `<ktjs-step>` children.
 *
 * Same shape as [`KtjsFeatures`]: the shared section band plus a grid whose default slot holds the
 * child cards.
 */
class KtjsNextSteps : CustomElementAdapter() {
    private val section: HomeSection
    private val grid: HTMLDivElement

    /** `section-title`: the heading above the grid. */
    @JsName("sectionTitle")
    var sectionTitle: String by attr("section-title") { change -> section.heading.textContent = change.newValue }

    init {
        val shadow = attachStyledShadow(HOME_RESET)
        grid = HtmlTagName.div.createElement {
            inlineStyle.apply {
                display = Display.grid
                gridTemplateColumns = autoFitGrid(20.rem)
                rowGap = 1.rem
                columnGap = 1.rem
                maxWidth = 54.rem
                margin = Margin(2.75.rem, Auto.auto, 0.px)
            }
            appendChild(HtmlTagName.slot.createElement())
        }
        section = homeSection()
        section.content.appendChild(grid)
        shadow.appendChild(section.root)
    }

    override fun connectedCallback() {
        applyInitialAttributes()
    }

    companion object : CustomElementReference<KtjsNextSteps>(KtjsNextSteps::class.js) {
        val NAME: TagName<KtjsNextSteps> = TagName(HomeMarkup.NEXT_STEPS)
    }
}
