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
import web.cssom.BoxShadow
import web.cssom.CSSStyleRule
import web.cssom.CSSStyleSheet
import web.cssom.ClassName
import web.cssom.PropertyName
import web.cssom.Color
import web.cssom.CustomPropertyName
import web.cssom.Display
import web.cssom.NamedColor
import web.cssom.LineStyle
import web.cssom.Transition
import web.cssom.TransitionTimingFunction
import web.cssom.many
import web.cssom.px
import web.cssom.rem
import web.cssom.rgb
import web.cssom.s
import web.cssom.`var`
import web.dom.document
import web.html.HTMLElement

/**
 * Probe: one element's stylesheet written as CSS text and as a csstype rules object, side by side.
 *
 *     ./gradlew :probes-web-components:jsBrowserDevelopmentRun      (the page, bottom)
 *     ./gradlew :probes-web-components:jsBrowserTest                (assertions)
 *
 * 1. [text] — what `KtjsFeature`'s companion holds today: `CSSStyleSheet().replaceSync(…)`.
 * 2. [dslAsText] — the csstype rules DSL through `@emotion/serialize` + `stylis`, i.e. the two calls
 *    emotion's own cache makes: `serializeStyles([rules])` writes the declarations (hyphenating
 *    names, expanding a `fallback(…)` array into repeated ones), then
 *    `serialize(compile("selector{…}"), middleware([stringify]))` resolves nested selectors and
 *    normalizes values. Only the declarations of the two packages actually used are declared here.
 * 3. [dslAsStyleSheet] — the same rules object replayed into real CSSOM rules (`insertRule` +
 *    `rule.style`), which leaves value serialization to the browser.
 *
 * What the DSL still needs spelled out, all of it visible below:
 *
 * - `&:hover`, not the `hover {}` sugar — the sugar writes the key `:hover`, which stylis reads as a
 *   *descendant* selector (`.parent :hover`).
 * - `Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease)` needs no cast even
 *   though `TransitionProperty` only exposes `all`: every `PropertyName<T>` is an `Ident`, and
 *   `IdentType` inherits `TransitionProperty`, so a typed property name doubles as a transition
 *   property. Two of them are joined by cssom's `many`.
 * - `var(name, fallback)` from cssom takes a `CustomPropertyName` plus a **typed** fallback, so
 *   `var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff))` is three typed calls rather
 *   than one string.
 * - `"&:hover"` rather than the `hover {}` sugar — see [pseudoSugarReport]: the sugar emits the key
 *   `:hover`, which every `&`-convention consumer (stylis included) reads as a descendant rule.
 *
 * The size of that trade is [rawCssReport] — how much CSS has to be typed as a string at all — not
 * source length: jOOQ is longer than the SQL it replaces, and that is not the point either.
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

    /**
     * Custom property names — the contract with the theme, not CSS syntax. `var()` takes one, so the
     * fallback becomes a typed value instead of a hand-written `var(--x, …)` string.
     */
    private val customPropertyNames = listOf(
        "--ktjs-card-border",
        "--ktjs-card-bg",
        "--ifm-background-surface-color",
        "--ktjs-accent",
        "--ifm-color-primary",
        "--ktjs-shadow",
    )

    /** The selector as a typed class name: `ClassName.invoke` writes the leading dot (RuleBuilder.kt). */
    private val cardClass = ClassName("ktjs-feature-card")

    /** Form 2 and 3 — the same rules, built with csstype's DSL. */
    private val rules: PropertiesBuilder = unsafeJso {
        cardClass {
            border = Border(
                1.px,
                LineStyle.solid,
                `var`(CustomPropertyName(customPropertyNames[0]), rgb(127, 127, 127, 0.24)),
            )
            backgroundColor = `var`(
                CustomPropertyName(customPropertyNames[1]),
                `var`(CustomPropertyName(customPropertyNames[2]), NamedColor.white),
            )
            // `many` is cssom's comma joiner: two typed Transition values, one declaration.
            transition = many(
                Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease),
                Transition(PropertyName.boxShadow, 0.2.s, TransitionTimingFunction.ease),
            )
            // A fallback ladder is a JS array; the serializer expands it into two declarations.
            display = fallback(Display.grid, Display.flex)
            padding = 1.5.rem
            "&:hover" {
                borderColor = `var`(
                    CustomPropertyName(customPropertyNames[3]),
                    `var`(CustomPropertyName(customPropertyNames[4])),
                )
                boxShadow = `var`(
                    CustomPropertyName(customPropertyNames[5]),
                    BoxShadow(0.px, 10.px, 24.px, rgb(31, 111, 235, 0.18)),
                )
            }
        }
        (ClassName("ktjs-feature-card-22")){

        }
    }

    /** Form 2 — the rules object turned into CSS text, using the packages emotion itself uses. */
    val dslAsText: String = serialized(rules)

    /**
     * The `hover {}` sugar from csstype's `SimplePseudosRuleBuilder` against writing `"&:hover"`
     * by hand — same declarations, different selector. The sugar writes the key `:hover` (that is
     * how csstype's TS union spells it, and karakum copies it verbatim), so everything downstream
     * that only understands the `&` convention — stylis included — reads it as a descendant rule.
     */
    val pseudoSugarReport: String = buildString {
        val viaSugar: PropertiesBuilder = unsafeJso {
            // `ClassName("x") { … }` parses as a two-argument call to the factory; the parentheses
            // hand the expression to the `invoke` operator instead. A reused name can just be a val.
            (ClassName("probe-sugar")) { hover { backgroundColor = NamedColor.red } }
        }
        val viaAmpersand: PropertiesBuilder = unsafeJso {
            (ClassName("probe-ampersand")) { "&:hover" { backgroundColor = NamedColor.red } }
        }
        append("hover {}      -> ").append(serialized(viaSugar)).append('\n')
        append("\"&:hover\" {}  -> ").append(serialized(viaAmpersand))
    }

    /** The emotion pipeline: declarations first, then stylis for the nested selectors. */
    private fun serialized(rules: PropertiesBuilder): String = buildString {
        val source = rules.asDynamic()
        val keys = Object.keys(rules.unsafeCast<JsAny>())
        val middlewares = middleware(jsArrayOf(stringify))
        for (index in 0 until keys.length) {
            val selector = keys[index].toString()
            val declarations = serializeStyles(jsArrayOf<JsAny?>(source[selector] as JsAny?)).styles
            append(serialize(compile("$selector{$declarations}"), middlewares))
        }
    }

    /** Form 3 — the rules object replayed into a real stylesheet; the browser writes the values. */
    fun dslAsStyleSheet(): CSSStyleSheet {
        val sheet = CSSStyleSheet()
        writeInto(sheet, rules, selector = null)
        return sheet
    }

    /** The metric that matters: how much CSS syntax is still spelled by hand in each form. */
    val rawCssReport: String = buildString {
        append("text form: ").append(text.length).append(" characters of hand-written CSS, names included\n")
        append("DSL form:  no CSS text, no selector string, no cast — only names: ")
            .append(customPropertyNames.size).append(" custom properties (")
            .append(customPropertyNames.sumOf { it.length })
            .append(" characters) + the \"&:hover\" key (see 8f)\n")
        append('\n')
        append("Typed values: Border(1.px, LineStyle.solid, …), rgb(127, 127, 127, 0.24),\n")
        append("BoxShadow(0.px, 10.px, 24.px, …), var(name, fallback), fallback(grid, flex), 1.5.rem,\n")
        append("Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease) joined by many(…).\n")
        append("PropertyName doubles as a TransitionProperty: every PropertyName is an Ident, and\n")
        append("IdentType inherits TransitionProperty. Source length is not the measure — both forms emit\n")
        append("the same declarations.")
    }

    /** Renders every form, plus a card that adopts the CSSOM one, into the probe page. */
    fun renderInto(host: HTMLElement) {
        host.appendChild(heading(2, "8. csstype rules DSL vs CSS text"))
        host.appendChild(heading(3, "8a. text (what KtjsFeature holds today)"))
        host.appendChild(block(text))
        host.appendChild(heading(3, "8b. csstype DSL -> text, @emotion/serialize + stylis"))
        host.appendChild(block(dslAsText))
        host.appendChild(heading(3, "8c. csstype DSL -> CSSOM (browser serialized)"))
        host.appendChild(block(cssTextOf(dslAsStyleSheet())))
        host.appendChild(heading(3, "8d. a card adopting 8c — hover it"))
        host.appendChild(sampleCard())
        host.appendChild(heading(3, "8e. how much CSS text is left to spell by hand"))
        host.appendChild(block(rawCssReport))
        host.appendChild(heading(3, "8f. csstype's hover {} sugar vs \"&:hover\" (wrapper defect)"))
        host.appendChild(block(pseudoSugarReport))
    }

    // --- form 3: rules object -> CSSOM -----------------------------------------------------------

    /**
     * `CSSStyleRule.style` is the same `CSSStyleDeclaration` csstype's `Properties` describes, so
     * values land verbatim and `var()` needs no attention. What CSSOM cannot do is hold two
     * declarations of one property inside one rule: the `fallback(…)` ladder collapses to its last
     * entry, whereas the text form keeps both and lets the parser pick.
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

    // --- rendering ---------------------------------------------------------------------------------

    private fun sampleCard(): HTMLElement {
        val card = element("article") {
            className = cardClass
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

/** `:hover` / `&:hover` hang off the parent; anything else is a selector of its own. */
private fun resolvedSelector(name: String, parent: String?): String = when {
    name.startsWith("&") -> parent.orEmpty() + name.removePrefix("&")
    name.startsWith(":") -> parent.orEmpty() + name
    else -> name
}
