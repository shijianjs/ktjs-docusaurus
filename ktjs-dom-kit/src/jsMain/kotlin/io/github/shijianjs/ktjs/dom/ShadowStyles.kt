package io.github.shijianjs.ktjs.dom

import js.array.jsArrayOf
import web.components.ShadowRoot
import web.components.ShadowRootInit
import web.components.ShadowRootMode
import web.components.open
import web.cssom.CSSStyleSheet
import web.html.HTMLElement

/**
 * Attaches a shadow root to this element. Attached open by default.
 *
 * A component's own box, colours and typography belong on the nodes themselves
 * ([HTMLElement.inlineStyle]) or in a sheet of its own ([attachStyledShadow]); this call is for the
 * components that need neither.
 */
fun HTMLElement.attachShadowRoot(
    mode: ShadowRootMode = ShadowRootMode.open,
): ShadowRoot = attachShadow(ShadowRootInit(mode = mode))

/**
 * Attaches a shadow root that adopts [styles].
 *
 * Adoption is a **sharing** mechanism, and it works at two levels:
 *
 * - **One sheet per component class**, held in that class's `companion object` and given to every
 *   instance of that element. This is the normal case for a component that has any CSS rule at all:
 *   the rules are static, so they are parsed once and one `CSSStyleSheet` object serves every
 *   instance — the model Lit uses for `static styles`, in the place Kotlin keeps a class's statics.
 *   Never create a `<style>` node per instance: a page with four hundred cells would carry four
 *   hundred copies of the same text and pay parse cost four hundred times.
 * - **One sheet across components**, for what genuinely must change together: a reset, the theme
 *   tokens, a layout a parent imposes on its children.
 *
 * Everything a plain declaration can express stays inline ([HTMLElement.inlineStyle]); a sheet only
 * holds what inline cannot say — `:hover` / `:focus-visible`, `::before` / `::after`, `@media`,
 * `@supports`, `@keyframes`.
 *
 * ```kotlin
 * class Card : CustomElementAdapter() {
 *     private val shadow = attachStyledShadow(RESET, CARD_SHEET)
 *
 *     companion object {
 *         private val CARD_CSS: String = """
 *             .card:hover { border-color: var(--accent); }
 *         """.trimIndent()
 *
 *         private val CARD_SHEET: CSSStyleSheet = CSSStyleSheet().apply { replaceSync(CARD_CSS) }
 *     }
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
