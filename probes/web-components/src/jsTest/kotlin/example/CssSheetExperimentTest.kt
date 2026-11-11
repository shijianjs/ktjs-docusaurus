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

        // `var(name, fallback)` is typed, so the fallback colour comes out as `rgb(…)`.
        assertTrue(css.startsWith(".ktjs-feature-card{border:1px solid var(--ktjs-card-border,rgb(127 127 127 / 0.24));"), css)
        assertTrue(css.contains("background-color:var(--ktjs-card-bg,var(--ifm-background-surface-color,#FFFFFF));"), css)
        assertTrue(css.contains("transition:border-color 0.2s ease,box-shadow 0.2s ease;"), css)
        assertTrue(css.contains("padding:1.5rem;"), css)
        // `fallback(Display.grid, Display.flex)` reaches the text as two declarations, in order.
        assertTrue(css.contains("display:grid;display:flex;"), css)
        // `&:hover` resolves to the parent selector instead of nesting a descendant rule.
        assertTrue(
            css.contains(".ktjs-feature-card:hover{border-color:var(--ktjs-accent,var(--ifm-color-primary));"),
            css,
        )
        assertTrue(css.contains("box-shadow:var(--ktjs-shadow,0px 10px 24px rgb(31 111 235 / 0.18));"), css)
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
        assertTrue(hover.contains("border-color: var(--ktjs-accent,var(--ifm-color-primary))"), hover)
    }

    @Test
    fun thePseudoSugarEmitsADescendantSelector() {
        val report = experiment.pseudoSugarReport

        // `hover {}` writes the key `:hover`; stylis joins it as a descendant, not as the parent's state.
        assertTrue(report.contains("hover {}      -> .probe-sugar :hover{background-color:#FF0000;}"), report)
        // Writing `&:hover` is what produces the intended rule.
        assertTrue(report.contains("\"&:hover\" {}  -> .probe-ampersand:hover{background-color:#FF0000;}"), report)
    }

    @Test
    fun theDslLeavesNothingButNamesToSpell() {
        val report = experiment.rawCssReport
        assertTrue(report.contains("text form: ${experiment.text.length} characters"), report)
        // No declaration, no selector, no cast — only the custom property names and the pseudo key.
        assertTrue(report.contains("no CSS text, no selector string, no cast"), report)
        assertTrue(report.contains("6 custom properties (107 characters)"), report)
        assertTrue(report.contains("PropertyName doubles as a TransitionProperty"), report)
    }
}
