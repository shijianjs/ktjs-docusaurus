package example

import csstype.PropertiesBuilder
import csstype.fallback
import js.array.jsArrayOf
import js.objects.Object
import js.objects.unsafeJso
import js.reflect.unsafeCast
import kotlin.js.JsAny
import web.components.ShadowRootInit
import web.components.ShadowRootMode
import web.components.open
import web.cssom.Border
import web.cssom.CSSStyleRule
import web.cssom.CSSStyleSheet
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.px
import web.cssom.rem
import web.dom.document
import web.html.HTMLElement

/**
 * Probe: one element's stylesheet written three ways, so they can be compared side by side.
 *
 *     ./gradlew :probes-web-components:jsBrowserDevelopmentRun      (the page, bottom)
 *     ./gradlew :probes-web-components:jsBrowserTest                (assertions)
 *
 * 1. [text] — what `KtjsFeature`'s companion holds today: `CSSStyleSheet().replaceSync(…)`.
 * 2. [dslAsText] — kotlin-csstype's rules DSL, i.e. `unsafeJso<PropertiesBuilder>` with
 *    `RuleBuilder` / `SimplePseudosRuleBuilder` (the shape emotion, styled and MUI consume), pushed
 *    through [writeRules]. **That serializer is the adapter an adoption would have to own**:
 *    csstype's DSL builds a JSO, not CSS.
 * 3. [dslAsStyleSheet] — the same JSO replayed into real CSSOM rules (`insertRule` + `rule.style`),
 *    which leaves value serialization to the browser instead of a hand-written writer.
 *
 * Shared by all three: property names are typed (`border = Border(1.px, LineStyle.solid, …)`) and
 * `hover {}` resolves to the parent selector. Not shared: Kotlin's generated unions drop
 * TypeScript's `(string & {})` escape hatch, so a multi-item `transition` or a whole-value `var()`
 * still needs `unsafeCast(String)` — see the two casts in [rules]. A `fallback(…)` ladder is a JS
 * array, which only the text form keeps (CSSOM collapses it, see [dslAsStyleSheet]); that is why
 * `display` appears twice.
 *
 * The run also shows the two adapters drifting from the hand-written text: [writeRules] writes a
 * nested rule inside its parent's braces (CSS Nesting — current browsers accept it, older ones do
 * not), while CSSOM re-serializes values (`transition: border-color 0.2s ease` comes back as
 * `border-color 0.2s`).
 */
class CssSheetExperiment {

    /** Form 1 — the CSS as written in the elements module today. */
    val text: String = """
        .ktjs-feature-card {
          border: 1px solid var(--ktjs-card-border, rgba(127, 127, 127, 0.24));
          background-color: var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff));
          transition: border-color 0.2s ease, box-shadow 0.2s ease;
          display: grid;
          display: flex;
          padding: 1.5rem;
        }

        .ktjs-feature-card:hover {
          border-color: var(--ktjs-accent, var(--ifm-color-primary));
          box-shadow: var(--ktjs-shadow, 0 10px 24px rgba(31, 111, 235, 0.18));
        }
    """.trimIndent()

    /** Forms 2 and 3 — the same rules, built with csstype's DSL. */
    private val rules: PropertiesBuilder = unsafeJso {
        ".ktjs-feature-card" {
            border = Border(
                1.px,
                LineStyle.solid,
                Color("var(--ktjs-card-border, rgba(127, 127, 127, 0.24))"),
            )
            backgroundColor = Color("var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff))")
            // Typed property, stringly value: the generated Kotlin unions have no "any string".
            transition = unsafeCast("border-color 0.2s ease, box-shadow 0.2s ease")
            // A fallback ladder is a JS array — kept by the text form, collapsed by CSSOM.
            display = fallback(Display.grid, Display.flex)
            padding = 1.5.rem
            hover {
                borderColor = Color("var(--ktjs-accent, var(--ifm-color-primary))")
                // `BoxShadow` has no String overload: a whole-value var() needs a cast as well.
                boxShadow = unsafeCast("var(--ktjs-shadow, 0 10px 24px rgba(31, 111, 235, 0.18))")
            }
        }
    }

    /** Form 2 — the JSO turned into CSS text. */
    val dslAsText: String = buildString { writeRules(rules, parentSelector = null) }

    /** Form 3 — the JSO replayed into a real stylesheet; the browser writes the values. */
    fun dslAsStyleSheet(): CSSStyleSheet {
        val sheet = CSSStyleSheet()
        writeInto(sheet, rules, selector = null)
        return sheet
    }

    /** Renders all three forms, plus a card that adopts the CSSOM one, into the probe page. */
    fun renderInto(host: HTMLElement) {
        host.appendChild(heading(2, "8. csstype rules DSL vs CSS text"))
        host.appendChild(heading(3, "8a. text (what KtjsFeature holds today)"))
        host.appendChild(block(text))
        host.appendChild(heading(3, "8b. csstype DSL, serialized to text by the adapter below"))
        host.appendChild(block(dslAsText))
        host.appendChild(heading(3, "8c. csstype DSL, replayed into CSSOM (browser serialized)"))
        host.appendChild(block(cssTextOf(dslAsStyleSheet())))
        host.appendChild(heading(3, "8d. a card adopting 8c — hover it"))
        host.appendChild(sampleCard())
    }

    // --- adapter A: JSO -> text ------------------------------------------------------------------

    /**
     * Writes one level of a rules object: declarations first, then nested rules.
     *
     * The declaration/rule split is a **naming heuristic** (`&…`, `:…`, `@…`, everything else that is
     * not a value is a selector). Nothing in the DSL marks the difference, and nothing expands a
     * `fallback(…)` array into repeated declarations — that is stylis' job in the emotion world, and
     * would be ours here.
     */
    private fun StringBuilder.writeRules(
        node: JsAny?,
        parentSelector: String?,
    ) {
        val source = node.asDynamic()
        val keys = Object.keys(node.unsafeCast<JsAny>())

        for (index in 0 until keys.length) {
            val name = keys[index].toString()
            val value: Any? = source[name] as Any?
            if (isRuleValue(value)) continue
            if (isArrayValue(value)) {
                // `fallback(Display.grid, Display.flex)` -> two declarations, in order.
                val items = value.asDynamic()
                for (item in 0 until items.length) {
                    append(hyphenized(name)).append(": ").append("${items[item]}").append(";\n")
                }
            } else {
                append(hyphenized(name)).append(": ").append("$value").append(";\n")
            }
        }

        for (index in 0 until keys.length) {
            val name = keys[index].toString()
            val value: Any? = source[name] as Any?
            if (!isRuleValue(value)) continue
            val selector = resolvedSelector(name, parentSelector)
            append(selector).append(" {\n")
            writeRules(value, selector)
            append("}\n")
        }
    }

    // --- adapter B: JSO -> CSSOM -----------------------------------------------------------------

    /**
     * [CSSStyleRule.style] is the same `CSSStyleDeclaration` that csstype's `Properties` describes, so
     * values land verbatim and `var()` needs no attention. What CSSOM cannot do is hold two
     * declarations of one property inside one rule: a `fallback(…)` ladder collapses to its last
     * entry here, whereas the text form keeps both and lets the parser pick.
     */
    private fun writeInto(
        sheet: CSSStyleSheet,
        node: JsAny?,
        selector: String?,
    ) {
        val source = node.asDynamic()
        val keys = Object.keys(node.unsafeCast<JsAny>())
        val declarations = (0 until keys.length)
            .map { keys[it].toString() to (source[keys[it].toString()] as Any?) }
            .filter { !isRuleValue(it.second) }

        if (selector != null && declarations.isNotEmpty()) {
            val index = sheet.insertRule("$selector {}", sheet.cssRules.length)
            val rule = sheet.cssRules.item(index).unsafeCast<CSSStyleRule>()
            val style = rule.style.asDynamic()
            for ((name, value) in declarations) {
                val last = value.asDynamic()
                style[name] = if (isArrayValue(value)) last[last.length - 1] else value
            }
        }

        for (index in 0 until keys.length) {
            val name = keys[index].toString()
            val value: Any? = source[name] as Any?
            if (!isRuleValue(value)) continue
            writeInto(sheet, value, resolvedSelector(name, selector))
        }
    }

    // --- rendering -------------------------------------------------------------------------------

    private fun sampleCard(): HTMLElement {
        val card = element("article") {
            className = ClassName("ktjs-feature-card")
            textContent = "Hover me: border and shadow come from the adopted sheet."
        }
        return element("div") {
            val shadow = attachShadow(ShadowRootInit(mode = ShadowRootMode.open))
            shadow.adoptedStyleSheets = jsArrayOf(dslAsStyleSheet())
            shadow.appendChild(card)
        }
    }

    private fun cssTextOf(sheet: CSSStyleSheet): String = buildString {
        val rules = sheet.cssRules
        for (index in 0 until rules.length) {
            append(rules.item(index)?.cssText).append('\n')
        }
    }

    private fun heading(level: Int, text: String): HTMLElement =
        element("h$level") { textContent = text }

    private fun block(text: String): HTMLElement =
        element("pre") {
            className = ClassName("css-probe")
            textContent = text
        }
}

private fun element(tag: String, block: HTMLElement.() -> Unit): HTMLElement =
    document.createElement(tag).apply(block)

// Plain functions, not extensions: a value read out of a JSO keeps Kotlin/JS's `dynamic` type, and a
// call on `dynamic` is a JS property lookup (`value.isRule is not a function`), not a static call.

private fun isArrayValue(value: Any?): Boolean =
    jsTypeOf(value) == "object" && value != null && js("Array.isArray")(value) as Boolean

private fun isRuleValue(value: Any?): Boolean =
    jsTypeOf(value) == "object" && value != null && !isArrayValue(value)

/** `background-color`, and `-webkit-transform` for a leading capital. */
private fun hyphenized(name: String): String = name.replace(Regex("([A-Z])"), "-$1").lowercase()

/** `:hover` / `&:hover` hang off the parent; anything else is a selector of its own. */
private fun resolvedSelector(name: String, parent: String?): String = when {
    name.startsWith("&") -> parent.orEmpty() + name.removePrefix("&")
    name.startsWith(":") -> parent.orEmpty() + name
    else -> name
}
