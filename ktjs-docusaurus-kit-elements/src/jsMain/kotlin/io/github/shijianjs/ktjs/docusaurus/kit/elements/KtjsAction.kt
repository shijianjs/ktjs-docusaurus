package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.AttributeCodec
import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.attachStyledShadow
import io.github.shijianjs.ktjs.dom.attr
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.enumAttributeCodec
import io.github.shijianjs.ktjs.dom.inlineStyle
import io.github.shijianjs.ktjs.iconify.IconifyIconElement
import io.github.shijianjs.ktjs.iconify.IconifyIconTagName
import kotlin.js.JsName
import web.components.CustomElementReference
import web.cssom.AlignItems
import web.cssom.CSSStyleSheet
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
 * `kind` of a [`KtjsAction`]: `primary` is the filled pill, `secondary` the outlined one.
 *
 * The lower-cased constant name is what `kind="…"` matches; an unknown value reads as [Primary], the
 * same way any other unparsable attribute falls back to the delegate's default.
 */
enum class ActionKind(internal val className: String) {
    Primary("ktjs-button-primary"),
    Secondary("ktjs-button-secondary"),
}

/**
 * `<ktjs-action kind="primary|secondary" href="…" icon="…">Label</ktjs-action>`
 *
 * A call-to-action pill. `secondary` actions open in a new tab and carry an Iconify glyph; the glyph
 * is dropped when `icon` is empty. All three attributes are `attr` delegates, so the pill follows
 * them: the shadow tree is re-rendered from the new value, wherever the change came from.
 *
 * The pill's box is inline on the anchor; its colours live in [ACTION_CSS] because `:hover` has to
 * change them, and an inline value would win over the hover rule.
 */
class KtjsAction : CustomElementAdapter() {
    private val anchor: HTMLAnchorElement
    private val labelElement: HTMLSpanElement
    private val iconElement: IconifyIconElement

    /** `kind`: which pill to paint; `secondary` also opens in a new tab. */
    @JsName("kind")
    var kind: ActionKind by attr(codec = kindCodec, default = ActionKind.Primary) { change ->
        applyKind(change.newValue)
    }

    /** `href`: the link target; blank leaves the anchor without `href` instead of linking to the page. */
    @JsName("href")
    var href: String by attr { change ->
        if (change.newValue.isBlank()) {
            anchor.removeAttribute("href")
        } else {
            anchor.href = change.newValue
        }
    }

    /** `icon`: an Iconify name; blank removes the glyph from the pill. */
    @JsName("iconName")
    var iconName: String by attr("icon") { change ->
        applyIcon(change.newValue)
    }

    init {
        val shadow = attachStyledShadow(HOME_RESET, ACTION_SHEET)

        labelElement = HtmlTagName.span.createElement()
        iconElement = IconifyIconTagName.iconifyIcon.createElement {
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
            appendChild(iconElement)
            appendChild(labelElement)
        }
        shadow.appendChild(anchor)
    }

    override fun connectedCallback() {
        // The label is the element's own text, not an attribute; the rest comes from the delegates.
        labelElement.textContent = textContent?.trim().orEmpty()
        applyInitialAttributes()
    }

    private fun applyKind(kind: ActionKind) {
        anchor.classList.remove(ActionKind.Primary.className, ActionKind.Secondary.className)
        anchor.classList.add(kind.className)
        if (kind == ActionKind.Secondary) {
            anchor.target = WindowTarget._blank
            anchor.rel = "noopener noreferrer"
        } else {
            anchor.removeAttribute("target")
            anchor.removeAttribute("rel")
        }
    }

    private fun applyIcon(name: String) {
        if (name.isBlank()) {
            iconElement.remove()
            return
        }
        iconElement.setAttribute("icon", name)
        // The glyph is dropped from the pill while `icon` is empty; put it back in front of the label.
        if (iconElement.parentNode == null) anchor.insertBefore(iconElement, labelElement)
    }

    companion object : CustomElementReference<KtjsAction>(KtjsAction::class.js) {
        val NAME: TagName<KtjsAction> = TagName(HomeMarkup.ACTION)

        /** `ActionKind` cannot be enumerated through reflection in Kotlin/JS, hence the explicit list. */
        private val kindCodec: AttributeCodec<ActionKind> = enumAttributeCodec(ActionKind.entries)
    }
}

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

/** [ACTION_CSS] parsed once at module load; every `<ktjs-action>` adopts this one sheet. */
private val ACTION_SHEET: CSSStyleSheet = CSSStyleSheet().apply { replaceSync(ACTION_CSS) }
