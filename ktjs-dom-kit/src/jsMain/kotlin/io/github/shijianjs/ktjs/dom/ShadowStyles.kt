package io.github.shijianjs.ktjs.dom

import js.array.jsArrayOf
import web.components.ShadowRoot
import web.components.ShadowRootInit
import web.components.ShadowRootMode
import web.components.open
import web.cssom.CSSStyleSheet
import web.html.HTMLElement

/**
 * Attaches a shadow root and adopts [styles] as its stylesheets.
 *
 * The stylesheets are constructed objects (`CSSStyleSheet`), not text: build one wherever the CSS
 * lives, then hand the same instance to every element that needs it.
 *
 * ```kotlin
 * val sheet = CSSStyleSheet().apply { replaceSync(":host { display: block }") }
 *
 * class Panel : CustomElementAdapter() {
 *     private val shadow = attachStyledShadow(sheet)
 * }
 * ```
 *
 * Adoption is what makes sharing work: one sheet can serve any number of shadow roots, the browser
 * parses it once, and no `<style>` node is created — so a page with N elements does not carry N
 * copies of the same CSS text. Theme values still cross the boundary, because custom properties
 * inherit into shadow trees.
 *
 * [mode] defaults to open; a closed root is settable but then [ShadowRoot] is only reachable through
 * this call's return value.
 */
fun HTMLElement.attachStyledShadow(
    vararg styles: CSSStyleSheet,
    mode: ShadowRootMode = ShadowRootMode.open,
): ShadowRoot {
    val root = attachShadow(ShadowRootInit(mode = mode))
    root.adoptedStyleSheets = jsArrayOf(*styles)
    return root
}
