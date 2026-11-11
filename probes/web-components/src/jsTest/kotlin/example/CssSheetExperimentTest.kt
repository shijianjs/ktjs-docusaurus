package example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the forms in [CssSheetExperiment]: the csstype DSL through `@emotion/serialize` + stylis
 * produces the same declarations as the hand-written text, and the CSSOM route loses the
 * `fallback(…)` ladder.
 */
class CssSheetExperimentTest {

    private val experiment = CssSheetExperiment()

    @Test
    fun textKeepsTheFallbackLadder() {
        assertTrue(
            experiment.text.contains("display: grid;\n  display: flex;"),
            experiment.text,
        )
    }

    @Test
    fun serializedDslMatchesTheTextForm() {
        val css = experiment.dslAsText

        assertTrue(css.startsWith(".ktjs-feature-card{border:1px solid var(--ktjs-card-border,"), css)
        assertTrue(css.contains("background-color:var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff));"), css)
        assertTrue(css.contains("transition:border-color 0.2s ease,box-shadow 0.2s ease;"), css)
        assertTrue(css.contains("padding:1.5rem;"), css)
        // `fallback(Display.grid, Display.flex)` reaches the text as two declarations, in order.
        assertTrue(css.contains("display:grid;display:flex;"), css)
        // `&:hover` resolves to the parent selector instead of nesting a descendant rule.
        assertTrue(
            css.contains(".ktjs-feature-card:hover{border-color:var(--ktjs-accent, var(--ifm-color-primary));"),
            css,
        )
        assertTrue(css.contains("box-shadow:var(--ktjs-shadow, 0 10px 24px rgba(31, 111, 235, 0.18));"), css)
        // stylis runs with `stringify` only: no autoprefixer, same as emotion 11's cache.
        assertFalse(css.contains("-webkit-"), css)
    }

    @Test
    fun cssomCollapsesTheFallbackLadder() {
        val sheet = experiment.dslAsStyleSheet()
        assertEquals(2, sheet.cssRules.length)

        val card = sheet.cssRules.item(0)!!.cssText
        assertTrue(card.startsWith(".ktjs-feature-card {"), card)
        assertTrue(card.contains("display: flex"), card)
        assertFalse(card.contains("grid"), "CSSOM kept only the last declaration: $card")

        val hover = sheet.cssRules.item(1)!!.cssText
        assertTrue(hover.startsWith(".ktjs-feature-card:hover {"), hover)
        assertTrue(hover.contains("border-color: var(--ktjs-accent, var(--ifm-color-primary))"), hover)
    }

    @Test
    fun theDslLeavesOnlyItsStringLiteralsToSpell() {
        val report = experiment.rawCssReport
        assertTrue(report.contains("text form: ${experiment.text.length} characters"), report)
        assertTrue(report.contains("DSL form:  256 characters (5 string literals)"), report)
    }
}
