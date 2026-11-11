package example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the three forms in [CssSheetExperiment]: the text form and the DSL-through-adapter form
 * describe the same rules, and the CSSOM form loses the `fallback(…)` ladder.
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

        assertTrue(css.startsWith(".ktjs-feature-card {\n"), css)
        assertTrue(
            css.contains("border: 1px solid var(--ktjs-card-border, rgba(127, 127, 127, 0.24));\n"),
            css,
        )
        assertTrue(css.contains("background-color: var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff));\n"), css)
        assertTrue(css.contains("transition: border-color 0.2s ease, box-shadow 0.2s ease;\n"), css)
        assertTrue(css.contains("padding: 1.5rem;\n"), css)
        // The typed `hover {}` sugar resolves to the parent selector.
        assertTrue(css.contains(".ktjs-feature-card:hover {\n"), css)
        assertTrue(
            css.contains("box-shadow: var(--ktjs-shadow, 0 10px 24px rgba(31, 111, 235, 0.18));\n"),
            css,
        )
        // `fallback(…)` is an array: both declarations are emitted, in order.
        assertTrue(css.contains("display: grid;\ndisplay: flex;\n"), css)
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
}
