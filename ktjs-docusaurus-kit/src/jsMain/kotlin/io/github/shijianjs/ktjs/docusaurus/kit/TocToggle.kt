package io.github.shijianjs.ktjs.docusaurus.kit

import web.cssom.ClassName
import web.cssom.MediaQuery
import web.cssom.matchMedia
import web.dom.Element
import web.dom.ElementId
import web.dom.document
import web.events.EventHandler
import web.html.ButtonType
import web.html.HTMLButtonElement
import web.html.button

/**
 * Labels for the toggle, keyed by a lower-cased `html-lang` prefix.
 *
 * A site that publishes other languages adds its own entry; anything without one falls back to
 * [DEFAULT_LABELS]'s `en`.
 */
data class TocToggleLabels(
    /** Shown while the TOC is expanded: clicking collapses it. */
    val hide: String,
    /** Shown while the TOC is collapsed: clicking expands it. */
    val show: String,
)

/** Options of [TocToggle]. */
data class TocToggleOptions(
    /** Keyed by a lower-cased HTML language tag; falls back to `en`. */
    val labels: Map<String, TocToggleLabels> = DEFAULT_LABELS,
    /** `localStorage` key the choice is remembered under. */
    val storageKey: String = "ktjs:toc-collapsed",
)

private val DEFAULT_LABELS: Map<String, TocToggleLabels> = mapOf(
    "en" to TocToggleLabels(
        hide = "Collapse table of contents",
        show = "Expand table of contents",
    ),
    "zh-hans" to TocToggleLabels(hide = "收起目录", show = "展开目录"),
)

/**
 * Adds a collapse/expand control to the desktop table of contents.
 *
 * Docusaurus only exposes `themeConfig.docs.sidebar.hideable` for the left sidebar; the right-hand
 * TOC has no such option (`themeConfig.tableOfContents` only takes heading levels). The button is
 * therefore injected here, and the layout switches through the `ktjs-toc-collapsed` class on
 * `<body>` — the class goes on the body, not on `<html>`, because Docusaurus rewrites the whole
 * `class` attribute of `<html>` on every route.
 *
 * The button is inserted into the TOC column, above the list it controls: the column is the one place
 * that is both next to the TOC and outside the article, and a button parked in the viewport's corner
 * would collide with the theme's own "back to top" button.
 *
 * It is pinned the way the list is — `position: sticky` under the navbar — so it stays reachable
 * while scrolling and after collapsing, when the list is gone. The button's own height is what pushes
 * the TOC's sticky offset down, so the two never overlap.
 *
 * Collapsing marks the TOC column (`.ktjs-toc-column`) and the article column (`.ktjs-doc-article`)
 * with classes: the list goes away, the column shrinks to a strip that keeps the button, and the
 * article takes over the freed width instead of leaving a gap.
 *
 * The matching CSS is [TOC_CSS]; [install] injects it once.
 *
 * ```kotlin
 * private val toggle = TocToggle()
 *
 * @JsExport
 * fun onRouteDidUpdate() {
 *     toggle.refresh()
 * }
 * ```
 *
 * `install()` must be called from the browser only (a Docusaurus client module is also evaluated
 * during the Node prerender pass, where there is no DOM).
 */
class TocToggle(private val options: TocToggleOptions = TocToggleOptions()) {
    private var collapsed = false
    private var button: HTMLButtonElement? = null
    private var installed = false

    /**
     * Reads the stored preference, applies it before React renders (so a collapsed TOC never
     * flashes open) and wires up the viewport listener.
     */
    fun install() {
        if (installed) return
        installed = true

        appendStyle(document.head, TOC_CSS)
        collapsed = readPreference()
        setStateClass(collapsed)

        // React drops the desktop TOC when the viewport shrinks; re-check once it has re-rendered.
        matchMedia(MediaQuery(DESKTOP_QUERY)).onchange = EventHandler {
            setTimeout({ refresh() }, 0)
        }
    }

    /**
     * Reconciles the button with the current page.
     *
     * The TOC is rendered by React, so it exists only on pages with headings and only on wide
     * viewports. The button outlives both: it is created once on `<body>`, and a page without a TOC
     * only removes it—which is why it is rebuilt when it is no longer connected.
     */
    fun refresh() {
        val toc = document.querySelector(TOC_SELECTOR)
        if (button != null && button?.isConnected == false) {
            button = null
        }

        val column = toc?.parentElement
        if (toc == null || column == null) {
            // No desktop TOC on this page: drop the button and the layout class, so the article
            // always uses the full width where there is nothing to collapse.
            button?.remove()
            button = null
            setStateClass(false)
            return
        }

        markLayout(column)

        val current = button ?: createButton(toc)
        button = current
        // React rebuilds the column on every route, which takes the button with it; re-insert when it
        // is not where it belongs instead of assuming the earlier insert survived.
        if (current.parentElement !== column) {
            column.insertBefore(current, toc)
        }

        apply()
    }

    /**
     * Tags the two columns the collapse rule needs: the TOC column and the article column beside it.
     *
     * The article is the previous sibling of the TOC column inside the layout row; its width is set
     * by Infima's `.col--*` class, so the CSS needs its own class to widen it when the list is gone.
     */
    private fun markLayout(column: Element) {
        column.classList?.add(ClassName(COLUMN_CLASS))
        column.previousElementSibling?.classList?.add(ClassName(ARTICLE_CLASS))
    }

    private fun createButton(toc: Element): HTMLButtonElement {
        val element = document.createElement("button").unsafeCastElement<HTMLButtonElement>()
        element.type = ButtonType.button
        element.className = ClassName("clean-btn $BUTTON_CLASS")
        // `aria-controls` has to name an element that exists; the theme renders the TOC without an id.
        val tocId = toc.getAttribute("id").takeUnless { it.isNullOrEmpty() } ?: TOC_ID.also {
            toc.id = ElementId(it)
        }
        element.setAttribute("aria-controls", tocId)
        element.onclick = EventHandler {
            collapsed = !collapsed
            storePreference(collapsed)
            apply()
        }
        return element
    }

    private fun apply() {
        setStateClass(collapsed)
        updateLabel()
    }

    private fun updateLabel() {
        val target = button ?: return
        val labels = currentLabels()
        val label = if (collapsed) labels.show else labels.hide
        target.setAttribute("aria-label", label)
        target.setAttribute("title", label)
        target.setAttribute("aria-expanded", if (collapsed) "false" else "true")
    }

    /** The state class goes on `<body>`; see the class KDoc for why it is not `<html>`. */
    private fun setStateClass(value: Boolean) {
        document.body?.classList?.toggle(ClassName(STATE_CLASS), value)
    }

    private fun currentLabels(): TocToggleLabels {
        val language = document.documentElement.getAttribute("lang")?.lowercase() ?: "en"
        return options.labels[language] ?: options.labels["en"] ?: DEFAULT_LABELS.getValue("en")
    }

    private fun readPreference(): Boolean = try {
        localStorageOrNull()?.getItem(options.storageKey) == "true"
    } catch (_: Throwable) {
        // Storage may be unavailable (private mode, blocked cookies). The toggle still works for
        // the current page; the choice is just not remembered.
        false
    }

    private fun storePreference(value: Boolean) {
        try {
            localStorageOrNull()?.setItem(options.storageKey, value.toString())
        } catch (_: Throwable) {
            // Same as above: losing the preference is harmless.
        }
    }

    private companion object {
        const val STATE_CLASS = "ktjs-toc-collapsed"
        const val BUTTON_CLASS = "ktjs-toc-toggle"
        const val COLUMN_CLASS = "ktjs-toc-column"
        const val ARTICLE_CLASS = "ktjs-doc-article"
        const val TOC_ID = "ktjs-doc-toc"
        const val TOC_SELECTOR = ".theme-doc-toc-desktop"
        const val DESKTOP_QUERY = "(min-width: 997px)"

        /**
         * The layout half of the toggle.
         *
         * The button is pinned exactly like the list it controls — sticky under the navbar — so it
         * stays clickable while scrolling and after collapsing, when the list is gone.
         *
         * Collapsing shrinks the TOC column to a strip instead of hiding it (the button lives in it,
         * and hiding the column would hide the only way back), then widens the article column to the
         * full row: hiding the list alone would leave the column's 25% empty, because Infima sizes
         * the article with `.col--9` rather than with a flex ratio.
         */
        val TOC_CSS = """
            .ktjs-toc-toggle {
              display: flex;
              align-items: center;
              justify-content: center;
              width: 2rem;
              height: 2rem;
              /* The right edge of the column, next to the list it controls. */
              margin-inline-start: auto;
              margin-block-end: 0.5rem;
              padding: 0;
              border-radius: 999px;
              background: var(--ifm-background-surface-color, #fff);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
              opacity: 0.85;
              cursor: pointer;
            }

            .ktjs-toc-toggle:hover {
              opacity: 1;
            }

            .ktjs-toc-toggle::before {
              content: '';
              width: 0.45rem;
              height: 0.45rem;
              border-inline-start: 2px solid currentColor;
              border-block-start: 2px solid currentColor;
              transform: rotate(45deg);
              transition: transform 0.15s ease;
            }

            body.ktjs-toc-collapsed .ktjs-toc-toggle::before {
              transform: rotate(-135deg);
            }

            /* Two classes rather than one: the TOC's own sticky rule comes from a hashed CSS module,
               and at equal specificity the winner would depend on stylesheet order. The TOC's offset
               is pushed down by the button plus its margin, which is what keeps the two from
               overlapping. */
            .ktjs-toc-column .ktjs-toc-toggle {
              position: sticky;
              top: calc(var(--ifm-navbar-height) + 0.5rem);
              z-index: 1;
            }

            .ktjs-toc-column .theme-doc-toc-desktop {
              top: calc(var(--ifm-navbar-height) + 3rem);
              max-height: calc(100vh - (var(--ifm-navbar-height) + 4rem));
            }

            /* Collapsed: the list goes away, the strip keeps the button reachable, and the article
               takes the freed width. */
            body.ktjs-toc-collapsed .ktjs-toc-column {
              --ifm-col-width: 2.5rem;
              padding: 0;
            }

            body.ktjs-toc-collapsed .ktjs-toc-column .theme-doc-toc-desktop {
              display: none;
            }

            /* The strip is narrower than the column was, so the button centres in it instead of
               hugging the right edge of a column it no longer fills. */
            body.ktjs-toc-collapsed .ktjs-toc-toggle {
              margin-inline: auto;
            }

            /* Docusaurus pins the article to 75% with `max-width: 75% !important` in a hashed CSS
               module, so this rule needs `!important` as well (the body class gives it the
               specificity, the hash would otherwise change on every upgrade). */
            body.ktjs-toc-collapsed .ktjs-doc-article {
              max-width: 100% !important;
            }
        """.trimIndent()
    }
}
