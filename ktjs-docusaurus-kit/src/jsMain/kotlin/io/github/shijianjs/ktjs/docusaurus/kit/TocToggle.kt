package io.github.shijianjs.ktjs.docusaurus.kit

import web.cssom.MediaQuery
import web.cssom.matchMedia
import web.dom.TagName
import web.dom.document
import web.events.EventHandler
import web.html.HTMLElement

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
 * Places `<ktjs-toc-toggle>` in the desktop table of contents.
 *
 * This class is the Node-safe half: it knows where the toggle belongs (Docusaurus's TOC column) and
 * what it should say, and it inserts a plain custom-element tag. The tag itself — the button, the
 * stored preference, the layout switch — belongs to `ktjs-docusaurus-kit-browser`, which is loaded
 * by the browser alone. Nothing here touches an element the kit's own module cannot safely evaluate.
 *
 * `install()` is idempotent and wires the viewport listener; `refresh()` reconciles the tag with the
 * page. The TOC is rendered by React, so it exists only on pages with headings and only on wide
 * viewports; the tag is inserted in front of the list and removed again otherwise, and re-inserted
 * when React has rebuilt the column underneath it.
 *
 * ```kotlin
 * private val toggle = TocToggle()
 *
 * @JsExport
 * fun onRouteDidUpdate() {
 *     toggle.install()
 *     toggle.refresh()
 * }
 * ```
 */
class TocToggle(private val options: TocToggleOptions = TocToggleOptions()) {
    private var installed = false

    /**
     * Wires up the viewport listener once.
     *
     * Must be called from the browser only (a Docusaurus client module is also evaluated during the
     * Node prerender pass, where there is no DOM).
     */
    fun install() {
        if (installed) return
        installed = true

        // React drops the desktop TOC when the viewport shrinks; re-check once it has re-rendered.
        matchMedia(MediaQuery(DESKTOP_QUERY)).onchange = EventHandler { refresh() }
    }

    /** Reconciles the tag with the page; safe to call after every route change. */
    fun refresh() {
        val toc = document.querySelector(TOC_SELECTOR)
        val column = toc?.parentElement
        val existing = document.querySelector(TOGGLE_SELECTOR)

        if (toc == null || column == null) {
            // No desktop TOC on this page: drop the tag. Its own `disconnectedCallback` restores the
            // layout it had changed.
            existing?.remove()
            return
        }

        // React rebuilds the column on every route, which takes the tag with it; re-insert when it is
        // not where it belongs instead of assuming the earlier insert survived.
        if (existing?.parentElement !== column) {
            existing?.remove()
            column.insertBefore(createToggle(), toc)
        }
    }

    private fun createToggle(): HTMLElement = document.createElement(TOGGLE_TAG).also { toggle ->
        val labels = currentLabels()
        toggle.setAttribute(HIDE_LABEL, labels.hide)
        toggle.setAttribute(SHOW_LABEL, labels.show)
        toggle.setAttribute(STORAGE_KEY, options.storageKey)
    }

    private fun currentLabels(): TocToggleLabels {
        val language = document.documentElement.getAttribute("lang")?.lowercase() ?: "en"
        return options.labels[language] ?: options.labels["en"] ?: DEFAULT_LABELS.getValue("en")
    }

    private companion object {
        /**
         * The tag name is also a constant in `ktjs-docusaurus-kit-browser` (which owns the element);
         * this module cannot import that one, because loading it evaluates `HTMLElement` and the
         * client module is loaded by the Node prerender pass too. The end-to-end test is what keeps
         * the two spellings in sync.
         */
        val TOGGLE_TAG: TagName<HTMLElement> = TagName("ktjs-toc-toggle")
        const val TOGGLE_SELECTOR = "ktjs-toc-toggle"

        const val HIDE_LABEL = "hide-label"
        const val SHOW_LABEL = "show-label"
        const val STORAGE_KEY = "storage-key"

        /** The desktop TOC list whose column receives the tag. */
        const val TOC_SELECTOR = ".theme-doc-toc-desktop"

        /** The breakpoint Docusaurus uses for the desktop TOC. */
        const val DESKTOP_QUERY = "(min-width: 997px)"
    }
}
