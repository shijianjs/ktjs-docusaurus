package io.github.shijianjs.ktjs.docusaurus.kit.browser

import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.attribute.attr
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import kotlin.js.JsName
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

    /** `section-title`: the heading above the grid. */
    @JsName("sectionTitle")
    var sectionTitle: String by attr("section-title") { change -> section.heading.textContent = change.newValue }

    /** `tinted`: paints the band with the section tint; a boolean attribute, so presence means `true`. */
    @JsName("tinted")
    var tinted: Boolean by attr { change ->
        section.root.inlineStyle.backgroundColor = if (change.newValue) sectionTint else null
    }

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
        section = homeSection()
        section.content.appendChild(grid)
        shadow.appendChild(section.root)
    }

    override fun connectedCallback() {
        applyInitialAttributes()
    }

    companion object : CustomElementReference<KtjsFeatures>(KtjsFeatures::class.js) {
        val NAME: TagName<KtjsFeatures> = TagName(HomeMarkup.FEATURES)
    }
}
