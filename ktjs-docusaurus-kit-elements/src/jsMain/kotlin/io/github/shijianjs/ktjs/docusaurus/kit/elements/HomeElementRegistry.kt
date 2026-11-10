package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.defineIfMissing
import io.github.shijianjs.ktjs.iconify.registerIconifyIcon
import js.reflect.unsafeCast
import kotlin.js.JsExport
import web.html.HTMLElement

/**
 * Registers every home-page tag with the browser's custom-element registry.
 *
 * Each element renders into its own shadow root and reads its content from attributes or child tags.
 * `defineIfMissing` strips the compiler-generated bridges before defining, so the elements can use the
 * native DOM spelling.
 */
@JsExport
fun registerHomeElements() {
    // Importing the npm module is what registers <iconify-icon>; the wrapper exposes that side
    // effect as this call, so the bundler keeps the module.
    registerIconifyIcon()

    defineIfMissing(KtjsHero.NAME, KtjsHero.Companion)
    defineIfMissing(KtjsAction.NAME, KtjsAction.Companion)
    defineIfMissing(KtjsBadge.NAME, KtjsBadge.Companion)

    defineIfMissing(KtjsFeatures.NAME, KtjsFeatures.Companion)
    defineIfMissing(KtjsFeature.NAME, KtjsFeature.Companion)

    defineIfMissing(KtjsNextSteps.NAME, KtjsNextSteps.Companion)
    defineIfMissing(KtjsStep.NAME, KtjsStep.Companion)
}

/**
 * The section an element renders into, kept for callers that store state on the host element.
 */
internal external interface HomeHostState {
    var ktjsSection: Any?
}

internal fun <T : Any> HTMLElement.section(factory: (HTMLElement) -> T): T {
    val state = unsafeCast<HomeHostState>(this)
    state.ktjsSection?.let { return unsafeCast<T>(it) }
    return factory(this).also { state.ktjsSection = it }
}
