package io.github.shijianjs.ktjs.docusaurus.kit.browser

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.attachStyledShadow
import io.github.shijianjs.ktjs.dom.attr
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.css.Pseudos
import io.github.shijianjs.ktjs.dom.css.invoke
import io.github.shijianjs.ktjs.dom.css.pseudos
import io.github.shijianjs.ktjs.dom.cssSheet
import io.github.shijianjs.ktjs.dom.inlineStyle
import kotlin.js.JsName
import web.components.CustomElementReference
import web.cssom.AlignItems
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.CSSStyleSheet
import web.cssom.Color
import web.cssom.Content
import web.cssom.Cursor
import web.cssom.CustomPropertyName
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.Length
import web.cssom.LineStyle
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Padding
import web.cssom.Position
import web.cssom.PropertyName
import web.cssom.Transition
import web.cssom.TransitionTimingFunction
import web.cssom.deg
import web.cssom.integer
import web.cssom.minus
import web.cssom.number
import web.cssom.plus
import web.cssom.px
import web.cssom.rem
import web.cssom.rotate
import web.cssom.rgb
import web.cssom.s
import web.cssom.`var`
import web.cssom.vh
import web.dom.ElementId
import web.dom.TagName
import web.events.EventHandler
import web.html.ButtonType
import web.html.HTMLButtonElement
import web.html.HTMLElement
import web.html.HtmlTagName
import web.html.button

/**
 * `<ktjs-toc-toggle hide-label="…" show-label="…" storage-key="…">`
 *
 * A collapse/expand control for the desktop table of contents. The kit inserts this tag into the TOC
 * column, before the list; everything the control does is here, in the browser.
 *
 * Docusaurus only exposes `themeConfig.docs.sidebar.hideable` for the left sidebar; the right-hand
 * TOC has no such option (`themeConfig.tableOfContents` only takes heading levels). The tag is
 * therefore injected by the kit, and this element owns the button, the stored preference and the
 * layout switch.
 *
 * The element knows no Docusaurus selector: its own position is the description of the layout it
 * changes. The parent is the TOC column, the next sibling is the list it controls, and the column's
 * previous sibling is the article column that takes the freed width.
 *
 * Collapsing shrinks the column to a narrow strip instead of hiding it — the button lives in the
 * column, so hiding it would hide the only way back. Only the list is hidden inside the strip, and
 * the article is widened to the full row: hiding the list alone would leave the column's 25% empty,
 * because Infima sizes the article with `.col--9` rather than with a flex ratio. All of it is inline
 * style on the nodes involved, so the element needs no page-wide stylesheet.
 *
 * `collapsed` is the state: seeded from the stored preference on connect, toggled by the button, and
 * read again when the site rebuilds the layout and re-creates the element.
 */
class KtjsTocToggle : CustomElementAdapter() {
    private val button: HTMLButtonElement = HtmlTagName.button.createElement()

    /** `hide-label`: the button's label while the list is expanded. */
    @JsName("hideLabel")
    var hideLabel: String by attr("hide-label", default = DEFAULT_HIDE_LABEL) { updateLabel() }

    /** `show-label`: the button's label while the list is collapsed. */
    @JsName("showLabel")
    var showLabel: String by attr("show-label", default = DEFAULT_SHOW_LABEL) { updateLabel() }

    /** `storage-key`: the `localStorage` key the choice is remembered under. */
    @JsName("storageKey")
    var storageKey: String by attr("storage-key", default = DEFAULT_STORAGE_KEY)

    /** `collapsed`: whether the list is hidden; also the state the layout is derived from. */
    @JsName("collapsed")
    var collapsed: Boolean by attr(default = false) { change ->
        applyLayout(change.newValue)
        updateLabel()
    }

    /** The nodes whose layout the toggle changes, held while connected. */
    private var column: HTMLElement? = null
    private var list: HTMLElement? = null
    private var article: HTMLElement? = null

    init {
        val shadow = attachStyledShadow(SHEET)
        button.type = ButtonType.button
        button.onclick = EventHandler {
            collapsed = !collapsed
            storePreference(collapsed)
        }
        shadow.appendChild(button)
    }

    override fun connectedCallback() {
        column = parentElement
        list = nextElementSibling as? HTMLElement
        article = column?.previousElementSibling as? HTMLElement

        applyInitialAttributes()
        pinList()
        collapsed = readPreference()
        applyLayout(collapsed)
        updateLabel()
    }

    /**
     * Restores the layout it changed. The column is rebuilt on the next route, but the nodes it
     * touched can outlive this element when the TOC is dropped (a resize, a page without headings),
     * and a column left at the strip width would stay collapsed with no control to bring it back.
     */
    override fun disconnectedCallback() {
        applyLayout(false)
        unpinList()
        column = null
        list = null
        article = null
    }

    /**
     * Pushes the list down by the button's own height, which is what keeps the two apart: both are
     * sticky under the navbar, and the list's own offset comes from a hashed CSS module.
     */
    private fun pinList() {
        val toc = list ?: return
        toc.inlineStyle.top = navbarHeight + 3.rem
        toc.inlineStyle.maxHeight = 100.vh - navbarHeight - 4.rem
    }

    /** Takes the offsets back, so a list that outlives this element keeps the theme's own values. */
    private fun unpinList() {
        val toc = list ?: return
        toc.style.removeProperty("top")
        toc.style.removeProperty("max-height")
    }

    private fun applyLayout(value: Boolean) {
        // Without a list there is nothing to collapse: the column and the article are only touched
        // to give a hidden list's width away. Keeping this guard also makes a standalone toggle
        // (connected somewhere without siblings) harmless.
        val toc = list ?: return

        column?.let { element ->
            if (value) {
                element.style.setProperty(COLUMN_WIDTH_NAME, COLLAPSED_WIDTH)
                element.inlineStyle.padding = Padding(0.px, 0.px)
            } else {
                element.style.removeProperty(COLUMN_WIDTH_NAME)
                element.style.removeProperty("padding")
            }
        }

        if (value) toc.inlineStyle.display = None.none else toc.style.removeProperty("display")

        article?.let { element ->
            // Docusaurus pins the article to 75% with `!important` in a hashed CSS module, so the
            // override needs the same priority.
            if (value) {
                element.style.setProperty("max-width", "100%", "important")
            } else {
                element.style.removeProperty("max-width")
            }
        }
    }

    private fun updateLabel() {
        val label = if (collapsed) showLabel else hideLabel
        button.ariaLabel = label
        button.title = label
        // `ariaExpanded` is a plain string in the published kotlin-browser (2026.11.0); the union
        // type in the sources tree is newer than the release we compile against.
        button.ariaExpanded = if (collapsed) "false" else "true"

        val toc = list ?: return
        val id = toc.getAttribute("id").takeUnless { it.isNullOrEmpty() } ?: TOC_ID.also {
            toc.id = ElementId(it)
        }
        button.setAttribute("aria-controls", id)
    }

    private fun readPreference(): Boolean = try {
        localStorageOrNull()?.getItem(storageKey) == "true"
    } catch (_: Throwable) {
        // Storage may be unavailable (private mode, blocked cookies). The toggle still works for the
        // current page; the choice is just not remembered.
        false
    }

    private fun storePreference(value: Boolean) {
        try {
            localStorageOrNull()?.setItem(storageKey, value.toString())
        } catch (_: Throwable) {
            // Same as above: losing the preference is harmless.
        }
    }

    companion object : CustomElementReference<KtjsTocToggle>(KtjsTocToggle::class.js) {
        val NAME: TagName<KtjsTocToggle> = TagName(HomeMarkup.TOC_TOGGLE)

        /** Fallbacks used when the kit does not pass `hide-label` / `show-label`. */
        const val DEFAULT_HIDE_LABEL: String = "Collapse table of contents"
        const val DEFAULT_SHOW_LABEL: String = "Expand table of contents"
        const val DEFAULT_STORAGE_KEY: String = "ktjs:toc-collapsed"

        private const val TOC_ID = "ktjs-doc-toc"
        private const val COLUMN_WIDTH_NAME = "--ifm-col-width"
        private const val COLLAPSED_WIDTH = "2.5rem"

        private val navbarHeight: Length =
            `var`(CustomPropertyName<Length>("--ifm-navbar-height"), 0.px)

        private val surfaceHook = CustomPropertyName<Color>("--ifm-background-surface-color")
        private val textHook = CustomPropertyName<Color>("--ifm-font-color-base")

        /**
         * What the button cannot express inline, and what the state has to say about it: the pin
         * (sticky, under the navbar), the pill with its hover, the arrow, and the arrow's rotation
         * when collapsed.
         */
        private val SHEET: CSSStyleSheet = cssSheet {
            ":host" {
                position = Position.sticky
                top = navbarHeight + 0.5.rem
                zIndex = integer(1)
                display = Display.flex
                justifyContent = JustifyContent.end
                marginBlockEnd = 0.5.rem
            }

            ":host([collapsed])" {
                justifyContent = JustifyContent.center
            }

            ":host > button" {
                display = Display.flex
                alignItems = AlignItems.center
                justifyContent = JustifyContent.center
                width = 2.rem
                height = 2.rem
                padding = Padding(0.px, 0.px)
                border = Border(0.px, None.none, Color.transparent)
                borderRadius = 999.px
                background = `var`(surfaceHook, NamedColor.white)
                boxShadow = BoxShadow(0.px, 2.px, 8.px, rgb(0, 0, 0, 0.18))
                opacity = number(0.85)
                cursor = Cursor.pointer
                color = `var`(textHook, NamedColor.black)

                Pseudos.hover {
                    opacity = number(1.0)
                }

                Pseudos.before {
                    content = Content()
                    width = 0.45.rem
                    height = 0.45.rem
                    borderInlineStart = Border(2.px, LineStyle.solid, `var`(textHook, NamedColor.black))
                    borderBlockStart = Border(2.px, LineStyle.solid, `var`(textHook, NamedColor.black))
                    // A start/top corner rotated by 45° is the usual upward chevron; 135° turns the
                    // same corner into `›`, which is the direction the TOC collapses towards.
                    transform = rotate(135.deg)
                    transition = Transition(
                        PropertyName.transform,
                        0.15.s,
                        TransitionTimingFunction.ease,
                    )
                }
            }

            ":host([collapsed]) > button" {
                Pseudos.before {
                    // Expanded is `›`, so the way back has to be `‹`: a reader who collapsed the
                    // column expects the control to point where the list will reappear.
                    transform = rotate(315.deg)
                }
            }
        }
    }
}

/** The Web Storage surface the toggle uses. */
private external interface WebStorage {
    fun getItem(key: String): String?

    fun setItem(key: String, value: String)
}

/** `localStorage`, or `null` where the environment has no Web Storage. */
@Suppress("UNCHECKED_CAST")
private fun localStorageOrNull(): WebStorage? =
    js("typeof localStorage === 'undefined' ? null : localStorage") as WebStorage?
