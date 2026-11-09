package io.github.shijianjs.ktjs.docusaurus.kit

/**
 * Registers the landing-page tags the kit understands.
 *
 * The tag names are part of the kit's markup contract — a page writes `<ktjs-hero>`,
 * `<ktjs-features>` and `<ktjs-next-steps>` and gets the sections described in `HomeBindings.kt`.
 * Nothing about them is site-specific, so a site only calls this once from its client module:
 *
 * ```kotlin
 * @JsExport
 * fun onRouteDidUpdate() {
 *     if (!isBrowser) return
 *     registerHomeElements()
 * }
 * ```
 *
 * Safe to call on every route change: `defineElement` skips tags that are already registered.
 */
fun registerHomeElements() {
    defineElement(HomeMarkup.HERO) { host -> bindHomeHero(host) }
    defineElement(HomeMarkup.FEATURES) { host -> bindFeatureSection(host) }
    defineElement(HomeMarkup.NEXT_STEPS) { host -> bindNextStepSection(host) }
}
