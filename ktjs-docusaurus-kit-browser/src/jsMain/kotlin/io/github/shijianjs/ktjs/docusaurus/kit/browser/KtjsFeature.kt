package io.github.shijianjs.ktjs.docusaurus.kit.browser

import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.attribute.attr
import io.github.shijianjs.ktjs.dom.css.dsl.Pseudos
import io.github.shijianjs.ktjs.dom.css.dsl.pseudos
import io.github.shijianjs.ktjs.dom.css.cssSheet
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.css.dsl.invoke
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import io.github.shijianjs.ktjs.iconify.IconifyIconElement
import io.github.shijianjs.ktjs.iconify.IconifyIconTagName
import kotlin.js.JsName
import web.components.CustomElementReference
import web.cssom.AlignItems
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.ClassName
import web.cssom.CSSStyleSheet
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.LineStyle
import web.cssom.NamedColor
import web.cssom.PropertyName
import web.cssom.JustifyContent
import web.cssom.Margin
import web.cssom.Transition
import web.cssom.TransitionProperty
import web.cssom.TransitionTimingFunction
import web.cssom.many
import web.cssom.integer
import web.cssom.number
import web.cssom.pct
import web.cssom.px
import web.cssom.rgb
import web.cssom.s
import web.cssom.`var`
import web.cssom.rem
import web.dom.TagName
import web.html.HTMLHeadingElement
import web.html.HTMLParagraphElement
import web.html.HTMLSpanElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * `<ktjs-feature icon="lucide:…" title="…">details</ktjs-feature>` — one card in a
 * [`KtjsFeatures`] grid.
 *
 * Renders itself into a shadow root, so the parent only lays the cards out. Border and background
 * are in [CSS] rather than inline because the card's `:hover` changes them.
 *
 * `icon` and `title` are `attr` delegates; the card body is the element's own text.
 */
class KtjsFeature : CustomElementAdapter() {
    private val iconElement: IconifyIconElement
    private val headingElement: HTMLHeadingElement
    private val detailsElement: HTMLParagraphElement

    /** `icon`: an Iconify name for the chip above the title. */
    @JsName("iconName")
    var iconName: String by attr("icon") { change -> iconElement.setAttribute("icon", change.newValue) }

    /** `title`: the card's heading; named `titleText` because `HTMLElement.title` is the tooltip. */
    @JsName("titleText")
    var titleText: String by attr("title") { change -> headingElement.textContent = change.newValue }

    init {
        val shadow = attachStyledShadow(HOME_RESET, SHEET)

        iconElement = IconifyIconTagName.iconifyIcon.createElement {
            inlineStyle.fontSize = 1.375.rem
            setAttribute("aria-hidden", "true")
        }
        headingElement = HtmlTagName.h3.createElement {
            inlineStyle.apply {
                margin = Margin(0.px, 0.px, 0.5.rem)
                fontSize = 1.0625.rem
                fontWeight = integer(600)
            }
        }
        detailsElement = HtmlTagName.p.createElement {
            inlineStyle.apply {
                margin = 0.px
                fontSize = 0.9375.rem
                lineHeight = number(1.7)
                color = mutedText
            }
        }
        val chip = HtmlTagName.span.createElement {
            inlineStyle.apply {
                display = Display.inlineFlex
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
                width = 2.5.rem
                height = 2.5.rem
                marginBottom = 1.rem
                borderRadius = 0.75.rem
                backgroundColor = accentSoft
                color = accent
            }
            appendChild(iconElement)
        }
        val card = HtmlTagName.article.createElement<HTMLElement> {
            // Hover state only, hence the hook.
            classList.add(CARD)
            inlineStyle.apply {
                display = Display.flex
                flexDirection = FlexDirection.column
                height = 100.pct
                padding = 1.5.rem
                borderRadius = 1.rem
            }
            appendChild(chip)
            appendChild(headingElement)
            appendChild(detailsElement)
        }
        shadow.appendChild(card)
    }

    override fun connectedCallback() {
        detailsElement.textContent = textContent?.trim().orEmpty()
        applyInitialAttributes()
    }

    companion object : CustomElementReference<KtjsFeature>(KtjsFeature::class.js) {
        val NAME: TagName<KtjsFeature> = TagName(HomeMarkup.FEATURE)

        /** The card, as a typed class name: the shadow markup and [SHEET] share it. */
        private val CARD: ClassName = ClassName("ktjs-feature-card")

        /** The card's surface and its `:hover` lift — what inline styles cannot say. */
        private val SHEET: CSSStyleSheet = cssSheet {
            CARD {
                border = Border(
                    1.px,
                    LineStyle.solid,
                    `var`(cardBorderHook, rgb(127, 127, 127, 0.24)),
                )
                backgroundColor = `var`(cardBackgroundHook, `var`(surfaceColorHook, NamedColor.white))
                transition = many(
                    Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease),
                    Transition(PropertyName.boxShadow, 0.2.s, TransitionTimingFunction.ease),
                )
                Pseudos.hover {
                    borderColor = `var`(accentHook, `var`(primaryColorHook))
                    boxShadow = `var`(
                        shadowHook,
                        BoxShadow(0.px, 10.px, 24.px, rgb(31, 111, 235, 0.18)),
                    )
                }
            }
        }
    }
}
