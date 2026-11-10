package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.appendStyle
import io.github.shijianjs.ktjs.dom.attachStyledShadow
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.inlineStyle
import io.github.shijianjs.ktjs.iconify.IconifyIconElement
import io.github.shijianjs.ktjs.iconify.IconifyIconTagName
import web.components.CustomElementReference
import web.cssom.AlignItems
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
 * out. Border and background are in [STEP_CSS] because `:hover` / `:focus-visible` change them.
 */
class KtjsStep : CustomElementAdapter() {
    private val anchor: HTMLAnchorElement
    private val heading: HTMLSpanElement
    private val details: HTMLSpanElement
    private val arrow: IconifyIconElement

    init {
        val shadow = attachStyledShadow(HOME_RESET)
        shadow.appendStyle(STEP_CSS)

        heading = HtmlTagName.span.createElement {
            inlineStyle.apply {
                fontSize = 1.rem
                fontWeight = integer(600)
                color = accent
            }
        }
        details = HtmlTagName.span.createElement {
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
            appendChild(heading)
            appendChild(details)
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
        anchor.href = getAttribute("href").orEmpty()
        heading.textContent = getAttribute("title").orEmpty()
        details.textContent = textContent?.trim().orEmpty()
    }

    companion object : CustomElementReference<KtjsStep>(KtjsStep::class.js) {
        val NAME: TagName<KtjsStep> = TagName(HomeMarkup.STEP)
    }
}

/** The card's surface and its `:hover` / `:focus-visible` lift. */
private const val STEP_CSS: String = """
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
"""
