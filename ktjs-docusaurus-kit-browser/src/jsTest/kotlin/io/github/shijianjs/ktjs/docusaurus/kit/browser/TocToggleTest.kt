package io.github.shijianjs.ktjs.docusaurus.kit.browser

import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.element.defineIfMissing
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import web.dom.document
import web.html.HTMLButtonElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * `<ktjs-toc-toggle>` checked in a real browser: attributes in, button and inline layout out.
 *
 * The element derives everything it needs from its position, so a test is a stand-in for the
 * Docusaurus column: an article, a column holding the toggle and the list, and the toggle's state in
 * between them. No stylesheet is involved — every change the test asserts is inline style on exactly
 * those nodes.
 */
class TocToggleTest {

    @BeforeTest
    fun defineElement() {
        defineIfMissing(KtjsTocToggle.NAME, KtjsTocToggle.Companion)
        clearStoredPreference()
    }

    @AfterTest
    fun forgetPreference() {
        clearStoredPreference()
    }

    @Test
    fun buttonReflectsLabelsAndControlTarget() {
        val row = TocRow(hideLabel = "Hide contents", showLabel = "Show contents")

        val button = row.button
        assertEquals("Hide contents", button.ariaLabel)
        assertEquals("Hide contents", button.title)
        assertEquals("true", button.getAttribute("aria-expanded"))

        // The list has no id of its own in the theme, so the toggle names it.
        val id = assertNotNull(row.list.getAttribute("id"))
        assertEquals(id, button.getAttribute("aria-controls"))

        row.button.click()
        assertEquals("Show contents", button.ariaLabel)
        assertEquals("Show contents", button.title)
        assertEquals("false", button.getAttribute("aria-expanded"))

        row.dispose()
    }

    @Test
    fun clickingCollapsesTheListAndWidensTheArticle() {
        val row = TocRow()

        assertEquals("", row.column.style.getPropertyValue("--ifm-col-width"))
        assertEquals("", row.list.style.display)
        assertEquals("", row.article.style.getPropertyValue("max-width"))

        row.button.click()

        assertTrue(row.toggle.hasAttribute("collapsed"))
        assertEquals("2.5rem", row.column.style.getPropertyValue("--ifm-col-width"))
        assertEquals("none", row.list.style.display)
        assertEquals("100%", row.article.style.getPropertyValue("max-width"))
        // Docusaurus pins the article with `!important` in a hashed CSS module.
        assertEquals("important", row.article.style.getPropertyPriority("max-width"))

        row.button.click()

        assertFalse(row.toggle.hasAttribute("collapsed"))
        assertEquals("", row.column.style.getPropertyValue("--ifm-col-width"))
        assertEquals("", row.list.style.display)
        assertEquals("", row.article.style.getPropertyValue("max-width"))
        assertEquals("", row.article.style.getPropertyPriority("max-width"))

        row.dispose()
    }

    @Test
    fun listIsPinnedBelowTheButton() {
        val row = TocRow()

        // The list is sticky as well; the toggle owns the offset that keeps the two apart.
        assertTrue(
            row.list.style.top.startsWith("calc("),
            "the list's sticky offset was not set: '${row.list.style.top}'",
        )
        assertTrue(row.list.style.maxHeight.startsWith("calc("))

        row.dispose()
    }

    @Test
    fun storedPreferenceIsRestoredOnConnect() {
        val first = TocRow(storageKey = "ktjs:test-toc")
        first.button.click()
        assertEquals("true", stored("ktjs:test-toc"))
        first.dispose()

        val second = TocRow(storageKey = "ktjs:test-toc")
        assertTrue(second.toggle.collapsed)
        assertEquals("none", second.list.style.display)

        second.button.click()
        assertEquals("false", stored("ktjs:test-toc"))
        assertEquals("", second.list.style.display)

        second.dispose()
    }

    @Test
    fun disconnectingRestoresTheLayoutItChanged() {
        val row = TocRow()
        row.button.click()
        assertEquals("none", row.list.style.display)

        // The TOC can outlive the control (a resize drops the element and re-adds it): a column left
        // collapsed would have no way back.
        row.dispose()

        assertEquals("", row.column.style.getPropertyValue("--ifm-col-width"))
        assertEquals("", row.list.style.display)
        assertEquals("", row.list.style.getPropertyValue("top"))
        assertEquals("", row.list.style.getPropertyValue("max-height"))
        assertEquals("", row.article.style.getPropertyValue("max-width"))
    }

    @Test
    fun standaloneToggleDoesNotTouchItsHost() {
        val toggle = KtjsTocToggle.NAME.createElement()
        toggle.setAttribute("hide-label", "Hide")
        document.body.appendChild(toggle)

        // Without a list there is nothing to collapse, so the parent (here `<body>`) is left alone.
        assertNotNull(toggle.shadowRoot?.querySelector(HtmlTagName.button)).click()
        assertTrue(toggle.hasAttribute("collapsed"))
        assertEquals("", document.body.style.getPropertyValue("--ifm-col-width"))
        assertEquals("", document.body.style.getPropertyValue("padding"))
        assertEquals("", document.body.style.getPropertyValue("max-width"))

        document.body.removeChild(toggle)
    }

    private class TocRow(
        hideLabel: String = KtjsTocToggle.DEFAULT_HIDE_LABEL,
        showLabel: String = KtjsTocToggle.DEFAULT_SHOW_LABEL,
        storageKey: String = KtjsTocToggle.DEFAULT_STORAGE_KEY,
    ) {
        val article: HTMLElement = HtmlTagName.article.createElement()
        val column: HTMLElement = HtmlTagName.div.createElement()
        val list: HTMLElement = HtmlTagName.ul.createElement()
        val toggle: KtjsTocToggle = KtjsTocToggle.NAME.createElement()

        val button: HTMLButtonElement
            get() = assertNotNull(toggle.shadowRoot?.querySelector(HtmlTagName.button))

        init {
            toggle.setAttribute("hide-label", hideLabel)
            toggle.setAttribute("show-label", showLabel)
            toggle.setAttribute("storage-key", storageKey)

            // The article comes first so it is the column's previous sibling, which is the layout
            // the element reads.
            document.body.appendChild(article)
            column.appendChild(toggle)
            column.appendChild(list)
            document.body.appendChild(column)
        }

        fun dispose() {
            column.remove()
            article.remove()
        }
    }
}

/** The stored preference, read through the same untyped Web Storage the element uses. */
private fun stored(key: String): String? = js("localStorage.getItem(key)") as String?

private fun clearStoredPreference() {
    js("localStorage.removeItem('ktjs:toc-collapsed')")
    js("localStorage.removeItem('ktjs:test-toc')")
}
