package docs.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junitpioneer.jupiter.RetryingTest

/**
 * The TOC toggle, checked against a page the browser has really rendered.
 *
 * The table of contents is client-only — Docusaurus mounts `theme-doc-toc-desktop` on a desktop
 * viewport, after hydration — so neither the control nor the layout it switches can be checked
 * against the built HTML. The button used to be fixed to the viewport's bottom-right corner, where
 * it landed on the theme's own "back to top" button; the first test is what pins the placement down.
 */
@UsePlaywright(DocsOptions::class)
class TocToggleE2eTest {

    @RetryingTest(maxAttempts = 2)
    fun `the toggle sits above the TOC and clear of the back-to-top button`(page: Page) {
        openDocWithToc(page)

        // It belongs to the TOC column, before the list it controls — not to `<body>`, and not in
        // the corner of the viewport.
        val placement = page.evaluate(
            """
            () => {
                const toc = document.querySelector('.theme-doc-toc-desktop');
                const button = document.querySelector('.ktjs-toc-toggle');
                const column = toc.parentElement;
                return {
                    inColumn: button.parentElement === column,
                    beforeToc: (button.compareDocumentPosition(toc) & Node.DOCUMENT_POSITION_FOLLOWING) !== 0,
                    columnTagged: column.classList.contains('ktjs-toc-column'),
                };
            }
            """.trimIndent(),
        ).asRecord()
        assertEquals(true, placement["inColumn"], "the toggle is not a child of the TOC column")
        assertEquals(true, placement["beforeToc"], "the toggle does not come before the TOC")
        assertEquals(true, placement["columnTagged"], "the TOC column is not tagged for the collapse rules")

        // Scrolled to the bottom, where the theme's own button sits: the two must not overlap. That
        // button is fixed to the bottom-right corner, so its rect is there whether or not the theme
        // has faded it in yet — the check does not have to wait for it.
        page.mouse().move(640.0, 400.0)
        page.mouse().wheel(0.0, 5_000.0)
        // Chromium applies the wheel's scroll to the offset asynchronously, so wait for it rather
        // than reading `scrollY` in the same turn.
        page.waitForFunction("() => window.scrollY > 100")

        val boxes = page.evaluate(
            """
            () => {
                const box = (selector) => document.querySelector(selector).getBoundingClientRect();
                const toggle = box('.ktjs-toc-toggle');
                const toc = box('.theme-doc-toc-desktop');
                const backToTop = box('.theme-back-to-top-button');
                return {
                    overlaps: toggle.left < backToTop.right && backToTop.left < toggle.right
                        && toggle.top < backToTop.bottom && backToTop.top < toggle.bottom,
                    toggleTop: toggle.top,
                    toggleBottom: toggle.bottom,
                    tocTop: toc.top,
                    backToTopTop: backToTop.top,
                };
            }
            """.trimIndent(),
        ).asRecord()
        assertEquals(false, boxes["overlaps"], "the toggle overlaps the back-to-top button: $boxes")
        assertTrue(
            boxes.number("toggleTop") < 400.0,
            "the toggle is not pinned near the top of the viewport: $boxes",
        )
        assertTrue(
            boxes.number("toggleBottom") < boxes.number("backToTopTop"),
            "the toggle is not clear of the back-to-top button: $boxes",
        )
        // The list is sticky too, so the button's own height is what keeps the two apart.
        assertTrue(
            boxes.number("toggleBottom") < boxes.number("tocTop"),
            "the toggle overlaps the TOC list: $boxes",
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `collapsing hides the list, keeps the toggle reachable and widens the article`(page: Page) {
        openDocWithToc(page)

        val toggle = page.locator(".ktjs-toc-toggle")
        val toc = page.locator(".theme-doc-toc-desktop")
        val article = page.locator(".ktjs-doc-article")
        val expandedWidth = article.boundingBox()!!.width

        toggle.click()

        // The list is gone; the control that brings it back is not — it lives in the column, which
        // only shrinks. Hiding the column instead would strand a reader who collapsed it.
        assertThat(toc).isHidden()
        assertThat(toggle).isVisible()
        assertEquals(true, page.isCollapsed(), "the state class is not on <body>")
        assertTrue(
            page.locator(".ktjs-toc-column").boundingBox()!!.width < 60.0,
            "the collapsed column did not shrink to a strip",
        )
        assertTrue(
            article.boundingBox()!!.width > expandedWidth,
            "the article did not take the width the list gave up",
        )

        // The choice is stored, so a reload comes back collapsed — and still expandable.
        page.reload()
        assertThat(toc).isHidden()
        assertThat(toggle).isVisible()

        toggle.click()
        assertThat(toc).isVisible()
        assertEquals(false, page.isCollapsed(), "the state class was not cleared")
    }

    /** A long page with headings: the TOC exists there, and the back-to-top button can appear. */
    private fun openDocWithToc(page: Page) {
        page.navigate(
            DocsServer.homeUrl + "docs/dom-kit/css-sheet/",
            Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD),
        )
        // The client module creates the button on its first route update; the assertion retries.
        assertThat(page.locator(".ktjs-toc-toggle")).isVisible()
    }

    private fun Page.isCollapsed(): Boolean =
        evaluate("() => document.body.classList.contains('ktjs-toc-collapsed')") as Boolean

    /** `page.evaluate` hands back a plain JS object as a map; the numbers may be any boxed type. */
    @Suppress("UNCHECKED_CAST")
    private fun Any?.asRecord(): Map<String, Any?> = this as Map<String, Any?>

    private fun Map<String, Any?>.number(key: String): Double = (this[key] as Number).toDouble()
}
