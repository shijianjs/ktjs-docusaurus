package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.attachStyledShadow
import io.github.shijianjs.ktjs.dom.attr
import io.github.shijianjs.ktjs.dom.cssSheet
import io.github.shijianjs.ktjs.dom.css.RadialGradientShape
import io.github.shijianjs.ktjs.dom.css.colorStop
import io.github.shijianjs.ktjs.dom.css.radialGradient
import io.github.shijianjs.ktjs.dom.css.pointerEventsNone
import io.github.shijianjs.ktjs.dom.css.or
import io.github.shijianjs.ktjs.dom.css.supports
import io.github.shijianjs.ktjs.dom.css.supportsDeclaration
import io.github.shijianjs.ktjs.dom.css.webkitBackgroundClipName
import io.github.shijianjs.ktjs.dom.css.webkitBackgroundClip
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.inlineStyle
import kotlin.js.JsName
import web.components.CustomElementReference
import web.components.HTMLSlotElement
import web.cssom.AlignItems
import web.cssom.BackgroundClip
import web.cssom.CSSStyleSheet
import web.cssom.Auto
import web.cssom.Color
import web.cssom.ClassName
import web.cssom.Content
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.FlexWrap
import web.cssom.Isolation
import web.cssom.JustifyContent
import web.cssom.Margin
import web.cssom.Overflow
import web.cssom.Padding
import web.cssom.Position
import web.cssom.PropertyName
import web.cssom.TextAlign
import web.cssom.atrule.MediaType
import web.cssom.atrule.maxWidth
import web.cssom.clamp
import web.cssom.deg
import web.cssom.dropShadow
import web.cssom.em
import web.cssom.integer
import web.cssom.linearGradient
import web.cssom.minus
import web.cssom.number
import web.cssom.pct
import web.cssom.px
import web.cssom.rem
import web.cssom.rgb
import web.cssom.stop
import web.cssom.translatex
import web.cssom.`var`
import web.cssom.and
import web.cssom.vw
import web.dom.TagName
import web.html.HTMLDivElement
import web.html.HTMLHeadingElement
import web.html.HTMLImageElement
import web.html.HTMLParagraphElement
import web.html.HTMLSpanElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * `<ktjs-hero logo-src="…" title="…" tagline="…">` with `<ktjs-action>` / `<ktjs-badge>` children.
 *
 * Renders into a shadow root. `logo-src` / `title` / `tagline` are `attr` delegates, so the shadow
 * tree follows them whenever they change — not only when the element is connected. The child tags are
 * routed into their own slots (`action`, `badge`) by tag name, so MDX does not need a `slot`
 * attribute. Each child is a real custom element that renders itself.
 */
class KtjsHero : CustomElementAdapter() {
    private val logoElement: HTMLImageElement
    private val headingElement: HTMLHeadingElement
    private val taglineElement: HTMLParagraphElement
    private val actions: HTMLDivElement
    private val badges: HTMLDivElement

    /** `logo-src`: the logo image; blank drops the `src` instead of pointing the image at the page. */
    @JsName("logoSrc")
    var logoSrc: String by attr("logo-src") { change ->
        if (change.newValue.isBlank()) {
            logoElement.removeAttribute("src")
        } else {
            logoElement.src = change.newValue
        }
    }

    /**
     * `title`: the heading text.
     *
     * Named `titleText` because `HTMLElement` already has a `title` (the tooltip attribute), and the
     * spec spelling of the attribute is the only thing the markup cares about.
     */
    @JsName("titleText")
    var titleText: String by attr("title") { change -> headingElement.textContent = change.newValue }

    /** `tagline`: the sentence under the heading. */
    @JsName("tagline")
    var tagline: String by attr { change -> taglineElement.textContent = change.newValue }

    init {
        val shadow = attachStyledShadow(HOME_RESET, SHEET)

        logoElement = HtmlTagName.img.createElement {
            inlineStyle.apply {
                display = Display.block
                width = clamp(7.rem, 16.vw, 10.rem)
                height = Auto.auto
                margin = Margin(0.px, Auto.auto)
                filter = dropShadow(0.px, 14.px, 28.px, glow)
            }
            alt = ""
        }
        headingElement = HtmlTagName.h1.createElement {
            // The gradient title is a `@supports` block, so it needs this hook (see CSS).
            classList.add(TITLE)
            inlineStyle.apply {
                margin = Margin(1.5.rem, 0.px, 0.px)
                fontSize = clamp(2.25.rem, 5.vw, 3.25.rem)
                fontWeight = integer(700)
                letterSpacing = (-0.02).em
                lineHeight = number(1.1)
            }
        }
        taglineElement = HtmlTagName.p.createElement {
            inlineStyle.apply {
                maxWidth = 34.rem
                margin = Margin(1.25.rem, Auto.auto, 0.px)
                fontSize = clamp(1.05.rem, 2.2.vw, 1.25.rem)
                lineHeight = number(1.65)
                color = mutedText
            }
        }
        val stage = HtmlTagName.span.createElement {
            inlineStyle.apply {
                display = Display.inlineBlock
                lineHeight = number(0.0)
            }
            appendChild(logoElement)
        }
        actions = HtmlTagName.div.createElement {
            // Only the small-screen stacking comes from CSS, so the row needs its hook.
            classList.add(ACTIONS)
            inlineStyle.apply {
                display = Display.flex
                flexWrap = FlexWrap.wrap
                justifyContent = JustifyContent.center
                rowGap = 0.75.rem
                columnGap = 1.rem
                marginTop = 2.25.rem
            }
            appendChild(HtmlTagName.slot.createElement<HTMLSlotElement> { name = "action" })
        }
        badges = HtmlTagName.div.createElement {
            inlineStyle.apply {
                display = Display.flex
                flexWrap = FlexWrap.wrap
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
                rowGap = 0.5.rem
                columnGap = 0.5.rem
                marginTop = 2.rem
            }
            appendChild(HtmlTagName.slot.createElement<HTMLSlotElement> { name = "badge" })
        }
        val inner = HtmlTagName.div.createElement {
            inlineStyle.maxWidth = 46.rem
            inlineStyle.margin = Margin(0.px, Auto.auto)
            appendChild(stage)
            appendChild(headingElement)
            appendChild(taglineElement)
            appendChild(actions)
            appendChild(badges)
        }

        val section = HtmlTagName.section.createElement<HTMLElement> {
            // The two glow blobs are `::before` / `::after` (see SHEET).
            classList.add(HERO)
            inlineStyle.apply {
                position = Position.relative
                isolation = Isolation.isolate
                width = 100.vw
                marginInline = 50.pct - 50.vw
                overflowX = Overflow.clip
                padding = Padding(clamp(3.rem, 5.vw, 4.5.rem), 1.5.rem, 4.rem)
                textAlign = TextAlign.center
            }
            appendChild(inner)
        }
        shadow.appendChild(section)
    }

    override fun connectedCallback() {
        // Markup attributes never reach a property setter, so this is where the three delegates first
        // hear about them. Later changes arrive on their own.
        applyInitialAttributes()

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

        /** The host, the gradient headline and the action row, as typed class names. */
        private val HERO: ClassName = ClassName("ktjs-hero")
        private val TITLE: ClassName = ClassName("ktjs-title")
        private val ACTIONS: ClassName = ClassName("ktjs-actions")

        /**
         * Everything in the hero that inline styles cannot say: two decorative pseudo-elements, the
         * `@supports`-guarded gradient text (inline colour would beat a descendant rule), and the
         * small-screen stacking of the action row. The host's own `display` lives in `HOME_RESET`.
         *
         * The two `-webkit-` needs of this sheet come from `ktjs-dom-kit`'s `css` package:
         * `radialGradient` and `webkitBackgroundClip`, since cssom models neither.
         */
        private val SHEET: CSSStyleSheet = cssSheet {
            HERO {
                "&::before, &::after" {
                    content = Content()
                    position = Position.absolute
                    zIndex = integer(-1)
                    borderRadius = 50.pct
                    pointerEvents = pointerEventsNone
                }

                "&::before" {
                    top = (-18).rem
                    left = 50.pct
                    width = 52.rem
                    height = 52.rem
                    transform = translatex((-50).pct)
                    background = radialGradient(
                        RadialGradientShape.Circle,
                        colorStop(glow),
                        stop(Color.transparent, 70.pct),
                    )
                }

                "&::after" {
                    right = (-10).rem
                    bottom = (-14).rem
                    width = 34.rem
                    height = 34.rem
                    background = radialGradient(
                        RadialGradientShape.Circle,
                        colorStop(glowWarm),
                        stop(Color.transparent, 70.pct),
                    )
                }
            }

            TITLE {
                color = `var`(accentHook, `var`(primaryColorHook))
            }

            supports(
                supportsDeclaration(webkitBackgroundClipName, BackgroundClip.text) or
                    supportsDeclaration(PropertyName.backgroundClip, BackgroundClip.text),
            ) {
                TITLE {
                    backgroundImage = linearGradient(
                        120.deg,
                        colorStop(accentDeep),
                        colorStop(accentBright),
                    )
                    webkitBackgroundClip(BackgroundClip.text)
                    backgroundClip = BackgroundClip.text
                    color = Color.transparent
                    // `background-clip: text` only paints inside the padding box, so the tight line box
                    // would leave the tail of "g" transparent; 0.06em was not enough at the clamped size.
                    paddingBottom = 0.14.em
                }
            }

            `@media`(MediaType.screen and maxWidth(576.px)) {
                ACTIONS {
                    flexDirection = FlexDirection.column
                    alignItems = AlignItems.stretch
                }
            }
        }
    }
}
