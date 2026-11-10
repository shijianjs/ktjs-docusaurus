package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.defineIfMissing
import js.reflect.unsafeCast
import kotlin.js.JsExport
import web.html.HTMLElement

/**
 * Registers the three landing-page tags with the browser's custom-element registry.
 *
 * Each element class follows the official API (`CustomElementAdapter` → `HTMLElement`,
 * `CustomElementReference` companion) and owns the section it renders into. This file is the only
 * place that knows the full set of tags; the classes live in their own files.
 *
 * `defineIfMissing` comes from `ktjs-dom-kit`; it strips the compiler-generated bridges before
 * defining the tag, so the elements can use the native DOM spelling.
 */
@JsExport
fun registerHomeElements() {
    defineIfMissing(KtjsHero.NAME, KtjsHero.Companion)
    defineIfMissing(KtjsFeatures.NAME, KtjsFeatures.Companion)
    defineIfMissing(KtjsNextSteps.NAME, KtjsNextSteps.Companion)
}

/**
 * The section an element renders into, stored on the element itself: released with the element, and
 * reused if `connectedCallback` runs again after a client-side navigation.
 */
internal external interface HomeHostState {
    var ktjsSection: Any?
}

internal fun <T : Any> HTMLElement.section(factory: (HTMLElement) -> T): T {
    val state = unsafeCast<HomeHostState>(this)
    state.ktjsSection?.let { return unsafeCast<T>(it) }
    return factory(this).also { state.ktjsSection = it }
}
