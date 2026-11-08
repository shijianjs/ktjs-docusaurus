package io.github.shijianjs.ktjs.docusaurus.kit

import web.cssom.ClassName
import web.cssom.MediaQuery
import web.cssom.matchMedia
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
     * viewports; the button may also have been discarded by a re-render, which is why it is rebuilt
     * when it is no longer connected.
     */
    fun refresh() {
        val toc = document.querySelector(TOC_SELECTOR)
        if (button != null && button?.isConnected == false) {
            button = null
        }

        if (toc == null) {
            // No desktop TOC on this page: drop the button and the layout class, so the article
            // always uses the full width where there is nothing to collapse.
            button?.remove()
            button = null
            setStateClass(false)
            return
        }

        toc.parentElement?.classList?.add(ClassName(COLUMN_CLASS))

        if (button == null) {
            val created = createButton()
            button = created
            toc.parentElement?.insertBefore(created, toc)
        }

        apply()
    }

    private fun createButton(): HTMLButtonElement {
        val element = document.createElement("button").unsafeCastElement<HTMLButtonElement>()
        element.type = ButtonType.button
        element.className = ClassName("clean-btn $BUTTON_CLASS")
        element.setAttribute("aria-controls", TOC_ID)
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
        const val TOC_ID = "ktjs-doc-toc"
        const val TOC_SELECTOR = ".theme-doc-toc-desktop"
        const val DESKTOP_QUERY = "(min-width: 997px)"

        /**
         * The layout half of the toggle: the TOC sits in a column of its own, and collapsing only
         * hides that column, so the article keeps the space it gains.
         */
        val TOC_CSS = """
            .ktjs-toc-column {
              position: relative;
            }

            .ktjs-toc-toggle {
              position: absolute;
              inset-inline-end: 0;
              inset-block-start: -2rem;
              display: none;
              align-items: center;
              justify-content: center;
              width: 1.6rem;
              height: 1.6rem;
              padding: 0;
              border-radius: 6px;
              opacity: 0.7;
              cursor: pointer;
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

            @media (min-width: 997px) {
              .ktjs-toc-toggle {
                display: inline-flex;
              }
            }

            body.ktjs-toc-collapsed .ktjs-toc-column {
              display: none;
            }

            body.ktjs-toc-collapsed .ktjs-toc-toggle::before {
              transform: rotate(-135deg);
            }

            body.ktjs-toc-collapsed .ktjs-toc-toggle {
              position: fixed;
              inset-inline-end: 1rem;
              inset-block-start: auto;
              inset-block-end: 1.5rem;
              z-index: 100;
              background: var(--ifm-background-surface-color, #fff);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
            }
        """.trimIndent()
    }
}
