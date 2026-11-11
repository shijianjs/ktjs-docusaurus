package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.attachStyledShadow
import io.github.shijianjs.ktjs.dom.attr
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.inlineStyle
import io.github.shijianjs.ktjs.iconify.IconifyIconElement
import io.github.shijianjs.ktjs.iconify.IconifyIconTagName
import kotlin.js.JsName
import web.components.CustomElementReference
import web.cssom.AlignItems
import web.cssom.CSSStyleSheet
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.None
import web.cssom.Padding
import web.cssom.integer
import web.cssom.number
import web.cssom.pct
import web.cssom.px
import web.cssom.rem
import web.dom.TagName
import web.html.HTMLAnchorElement
import web.html.HTMLSpanElement
import web.html.HtmlTagName

/**
 * `<ktjs-step href="…" title="…">details</ktjs-step>` — one link card in a [`KtjsNextSteps`] grid.
 *
 * The whole card is the anchor, so it renders itself into a shadow root and the parent only lays it
 * out. Border and background are in [CSS] because `:hover` / `:focus-visible` change them.
 *
 * `href` and `title` are `attr` delegates; the card body is the element's own text.
 */
class KtjsStep : CustomElementAdapter() {
    private val anchor: HTMLAnchorElement
    private val headingElement: HTMLSpanElement
    private val detailsElement: HTMLSpanElement
    private val arrow: IconifyIconElement

    /** `href`: where the card links to; blank leaves the anchor without `href`. */
    @JsName("href")
    var href: String by attr { change ->
        if (change.newValue.isBlank()) {
            anchor.removeAttribute("href")
        } else {
            anchor.href = change.newValue
        }
    }

    /** `title`: the card's heading; named `titleText` because `HTMLElement.title` is the tooltip. */
    @JsName("titleText")
    var titleText: String by attr("title") { change -> headingElement.textContent = change.newValue }

    init {
        val shadow = attachStyledShadow(HOME_RESET, SHEET)

        headingElement = HtmlTagName.span.createElement {
            inlineStyle.apply {
                fontSize = 1.rem
                fontWeight = integer(600)
                color = accent
            }
        }
        detailsElement = HtmlTagName.span.createElement {
            inlineStyle.apply {
                fontSize = 0.9.rem
                lineHeight = number(1.6)
                color = mutedText
            }
        }
        arrow = IconifyIconTagName.iconifyIcon.createElement {
            inlineStyle.apply {
                flexShrink = number(0.0)
                fontSize = 1.25.rem
                color = subtleText
            }
            setAttribute("icon", "lucide:arrow-right")
            setAttribute("aria-hidden", "true")
        }
        val body = HtmlTagName.span.createElement {
            inlineStyle.apply {
                display = Display.flex
                flexGrow = number(1.0)
                flexDirection = FlexDirection.column
                rowGap = 0.25.rem
                minWidth = 0.px
            }
            appendChild(headingElement)
            appendChild(detailsElement)
        }
        anchor = HtmlTagName.a.createElement {
            // Hover / focus state only, hence the hook.
            classList.add("ktjs-next-card")
            inlineStyle.apply {
                display = Display.flex
                alignItems = AlignItems.center
                columnGap = 1.rem
                height = 100.pct
                padding = Padding(1.25.rem, 1.375.rem)
                borderRadius = 0.875.rem
                color = Color("inherit")
                textDecorationLine = None.none
            }
            appendChild(body)
            appendChild(arrow)
        }
        shadow.appendChild(anchor)
    }

    override fun connectedCallback() {
        detailsElement.textContent = textContent?.trim().orEmpty()
        applyInitialAttributes()
    }

    companion object : CustomElementReference<KtjsStep>(KtjsStep::class.js) {
        val NAME: TagName<KtjsStep> = TagName(HomeMarkup.STEP)

        /** The card's surface and its `:hover` / `:focus-visible` lift. */
        private val CSS: String = """
            .ktjs-next-card {
              border: 1px solid var(--ktjs-card-border, rgba(127, 127, 127, 0.24));
              background-color: var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff));
              transition: border-color 0.2s ease, box-shadow 0.2s ease;
            }

            .ktjs-next-card:hover,
            .ktjs-next-card:focus-visible {
              border-color: var(--ktjs-accent, var(--ifm-color-primary));
              box-shadow: var(--ktjs-shadow, 0 10px 24px rgba(31, 111, 235, 0.18));
            }
        """.trimIndent()

        /** [CSS] parsed once; every `<ktjs-step>` adopts this one sheet. */
        private val SHEET: CSSStyleSheet = CSSStyleSheet().apply { replaceSync(CSS) }
    }
}
