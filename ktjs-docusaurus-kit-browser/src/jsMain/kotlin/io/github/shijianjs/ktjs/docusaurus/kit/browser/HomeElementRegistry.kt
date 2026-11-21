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
 *
 * That upgrade runs the element's **constructor** while its companion is still initialising, so a
 * constructor must be self-sufficient: anything it reads has to be a file-level value, a compile-time
 * constant or an argument — never a member of the companion that is defining it. The stylesheets,
 * class names and tag strings of these elements live at file level for exactly this reason; see the
 * note under each element's `companion object`.
 */
@Suppress("UNUSED_EXPRESSION")
@JsExport
fun registerKtjsElements() {
    // Importing the npm module is what registers <iconify-icon>; the wrapper exposes that side
    // effect as this call, so the bundler keeps the module.
    registerIconifyIcon()

    // Reading `ALL` initialises every element's companion, and a companion defines its tag in `init` —
    // so this one reference defines all eight tags, and any markup the page already spells is upgraded
    // right there. The list is generated (`ktjs-dom-kit-ksp`): the `NAME` reads this function used to
    // spell out are the same act, already merged, and a ninth element needs no line here.
    KtjsCustomElements.ALL
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
