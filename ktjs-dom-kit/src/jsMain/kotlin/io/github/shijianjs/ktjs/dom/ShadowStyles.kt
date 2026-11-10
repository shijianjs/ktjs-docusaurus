package io.github.shijianjs.ktjs.dom

import js.array.jsArrayOf
import web.components.ShadowRoot
import web.components.ShadowRootInit
import web.components.ShadowRootMode
import web.components.open
import web.cssom.CSSStyleSheet
import web.html.HTMLStyleElement
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * Attaches a shadow root to this element. Attached open by default.
 *
 * Styling normally belongs on the nodes themselves (`element.style`): a component's own box, colours
 * and typography are its private state, not a resource. Reach for [attachStyledShadow] only when a
 * sheet is genuinely shared, and for [ShadowRoot.appendStyle] when a rule needs a selector that
 * inline styles cannot express (`:hover`, `::before`, `@media`).
 */
fun HTMLElement.attachShadowRoot(
    mode: ShadowRootMode = ShadowRootMode.open,
): ShadowRoot = attachShadow(ShadowRootInit(mode = mode))

/**
 * Attaches a shadow root that adopts [styles].
 *
 * Adoption is a **sharing** mechanism: one constructed sheet serves any number of shadow roots and
 * the browser parses it once. Use it for what really is common to several components — a reset, the
 * theme tokens, a layout a parent imposes on its children — not as the default home for a
 * component's own CSS, which stays next to the component (inline where possible, a local
 * [ShadowRoot.appendStyle] where not).
 *
 * ```kotlin
 * internal val RESET = CSSStyleSheet().apply { replaceSync("*, *::before, *::after { box-sizing: border-box }") }
 *
 * class Panel : CustomElementAdapter() {
 *     private val shadow = attachStyledShadow(RESET)
 * }
 * ```
 */
fun HTMLElement.attachStyledShadow(
    vararg styles: CSSStyleSheet,
    mode: ShadowRootMode = ShadowRootMode.open,
): ShadowRoot {
    val root = attachShadowRoot(mode)
    if (styles.isNotEmpty()) {
        root.adoptedStyleSheets = jsArrayOf(*styles)
    }
    return root
}

/**
 * Appends a `<style>` whose text is [css] to this shadow tree.
 *
 * For the rules inline styles cannot carry: `:hover` / `:focus-visible` states, `::before` /
 * `::after`, media queries, `@keyframes`. Keep the text next to the component that owns it — this is
 * that component's own stylesheet, not a place to collect other components' rules.
 */
fun ShadowRoot.appendStyle(css: String): HTMLStyleElement =
    HtmlTagName.style.createElement<HTMLStyleElement> {
        textContent = css
    }.also(::appendChild)
