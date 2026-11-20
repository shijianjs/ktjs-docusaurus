package io.github.shijianjs.ktjs.docusaurus.kit.browser

import io.github.shijianjs.ktjs.iconify.registerIconifyIcon
import js.reflect.unsafeCast
import kotlin.js.JsExport
import web.html.HTMLElement

/**
 * Registers every `<ktjs-…>` tag with the browser's custom-element registry.
 *
 * Each element renders into its own shadow root and reads its content from attributes, child tags or
 * its own position in the page. The tag itself belongs to the element's companion: an
 * `CustomElementReferenceAdapter` defines it in `init`, so reading `NAME` on each companion here is
 * what registers it — and the browser upgrades any markup the page already spells.
 */
@Suppress("UNUSED_EXPRESSION")
@JsExport
fun registerKtjsElements() {
    // Importing the npm module is what registers <iconify-icon>; the wrapper exposes that side
    // effect as this call, so the bundler keeps the module.
    registerIconifyIcon()

    // Reading `NAME` on each companion is what registers its tag, and any markup the page already
    // spells is upgraded right there.
    KtjsHero.NAME
    KtjsAction.NAME
    KtjsBadge.NAME

    KtjsFeatures.NAME
    KtjsFeature.NAME

    KtjsNextSteps.NAME
    KtjsStep.NAME

    KtjsTocToggle.NAME
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
