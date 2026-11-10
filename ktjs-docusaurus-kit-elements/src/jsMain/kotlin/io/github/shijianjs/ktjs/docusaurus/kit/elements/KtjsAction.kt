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
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.None
import web.cssom.Padding
import web.cssom.integer
import web.cssom.number
import web.cssom.px
import web.cssom.rem
import web.dom.TagName
import web.html.HTMLAnchorElement
import web.html.HTMLSpanElement
import web.html.HtmlTagName
import web.window.WindowTarget
import web.window._blank

/**
 * `<ktjs-action kind="primary|secondary" href="…" icon="…">Label</ktjs-action>`
 *
 * A call-to-action pill. `secondary` actions open in a new tab and carry an Iconify glyph; the glyph
 * is dropped when `icon` is empty.
 *
 * The pill's box is inline on the anchor; its colours live in [ACTION_CSS] because `:hover` has to
 * change them, and an inline value would win over the hover rule.
 */
class KtjsAction : CustomElementAdapter() {
    private val anchor: HTMLAnchorElement
    private val label: HTMLSpanElement
    private val icon: IconifyIconElement

    init {
        val shadow = attachStyledShadow(HOME_RESET)
        shadow.appendStyle(ACTION_CSS)

        label = HtmlTagName.span.createElement()
        icon = IconifyIconTagName.iconifyIcon.createElement {
            inlineStyle.fontSize = 1.125.rem
            setAttribute("aria-hidden", "true")
        }
        anchor = HtmlTagName.a.createElement {
            inlineStyle.apply {
                display = Display.inlineFlex
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
                columnGap = 0.5.rem
                padding = Padding(0.7.rem, 1.6.rem)
                borderRadius = 999.px
                fontSize = 1.rem
                fontWeight = integer(600)
                lineHeight = number(1.4)
                textDecorationLine = None.none
            }
            appendChild(icon)
            appendChild(label)
        }
        shadow.appendChild(anchor)
    }

    override fun connectedCallback() {
        val secondary = getAttribute("kind") == "secondary"
        anchor.classList.remove(KIND_PRIMARY, KIND_SECONDARY)
        anchor.classList.add(if (secondary) KIND_SECONDARY else KIND_PRIMARY)
        anchor.href = getAttribute("href").orEmpty()
        if (secondary) {
            anchor.target = WindowTarget._blank
            anchor.rel = "noopener noreferrer"
        }
        label.textContent = textContent?.trim().orEmpty()
        val iconName = getAttribute("icon")
        if (iconName.isNullOrBlank()) {
            icon.remove()
        } else {
            icon.setAttribute("icon", iconName)
        }
    }

    companion object : CustomElementReference<KtjsAction>(KtjsAction::class.js) {
        val NAME: TagName<KtjsAction> = TagName(HomeMarkup.ACTION)
    }
}

/** `kind` values, also the class names [ACTION_CSS] keys its states on. */
private const val KIND_PRIMARY: String = "ktjs-button-primary"
private const val KIND_SECONDARY: String = "ktjs-button-secondary"

/**
 * What the pill cannot express inline: the border (hovered through `border-color`), the transition
 * that animates it, and the two colour schemes with their `:hover` / `:focus-visible` states.
 */
private const val ACTION_CSS: String = """
:host > a {
  border: 1px solid transparent;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.ktjs-button-primary {
  background-color: var(--ktjs-accent, var(--ifm-color-primary));
  border-color: var(--ktjs-accent, var(--ifm-color-primary));
  color: #fff;
}

.ktjs-button-primary:hover,
.ktjs-button-primary:focus-visible {
  background-color: var(--ktjs-accent-strong, #1a5fd0);
  border-color: var(--ktjs-accent-strong, #1a5fd0);
  color: #fff;
  box-shadow: var(--ktjs-shadow, 0 10px 24px rgba(31, 111, 235, 0.24));
}

.ktjs-button-secondary {
  background-color: transparent;
  border-color: var(--ktjs-outline-border, rgba(127, 127, 127, 0.35));
  color: var(--ktjs-accent, var(--ifm-color-primary));
}

.ktjs-button-secondary:hover,
.ktjs-button-secondary:focus-visible {
  background-color: var(--ktjs-accent-soft, rgba(31, 111, 235, 0.10));
  border-color: var(--ktjs-accent, var(--ifm-color-primary));
  color: var(--ktjs-accent, var(--ifm-color-primary));
}
"""
