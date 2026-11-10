package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.stripBridgesFrom
import js.reflect.unsafeCast
import kotlin.js.JsExport
import web.components.CustomElementReference
import web.components.customElements
import web.components.define
import web.dom.TagName
import web.html.HTMLElement

/**
 * Registers the three landing-page tags with the browser's custom-element registry.
 *
 * Each element class follows the official API (`CustomElementAdapter` → `HTMLElement`,
 * `CustomElementReference` companion) and owns the section it renders into. This file is the only
 * place that knows the full set of tags; the classes live in their own files.
 */
@JsExport
fun registerHomeElements() {
    stripBridgesFrom(KtjsHero::class.js)
    stripBridgesFrom(KtjsFeatures::class.js)
    stripBridgesFrom(KtjsNextSteps::class.js)

    defineIfMissing(KtjsHero.NAME, KtjsHero.Companion)
    defineIfMissing(KtjsFeatures.NAME, KtjsFeatures.Companion)
    defineIfMissing(KtjsNextSteps.NAME, KtjsNextSteps.Companion)
}

private fun <T : HTMLElement> defineIfMissing(name: TagName<T>, reference: CustomElementReference<T>) {
    if (customElements.get(name) == null) {
        customElements.define(name, reference)
    }
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
