package io.github.shijianjs.ktjs.docusaurus.demo

import io.github.shijianjs.ktjs.docusaurus.kit.TocToggle
import io.github.shijianjs.ktjs.docusaurus.kit.registerHomeElements
import kotlin.js.JsExport

/**
 * The site's client module, re-exported by the generated `client-module.mjs`.
 *
 * Docusaurus evaluates this module in two places: in the browser, and during the Node prerender pass.
 * Everything here touches `document` or the custom-element registry, so it is guarded and only runs
 * in the browser.
 *
 * Both behaviours come from the kit: `registerHomeElements()` defines the landing-page tags, and
 * [TocToggle] adds the collapse control to the table of contents. Running this after hydration — as
 * `onRouteDidUpdate` does — means React has already rendered the tags before they are upgraded, so
 * there is no hydration mismatch.
 */
private val tocToggle = TocToggle()

private val isBrowser: Boolean = js("typeof window !== 'undefined'")

/** Called by Docusaurus after every route change. */
@JsExport
fun onRouteDidUpdate() {
    if (!isBrowser) return
    registerHomeElements()
    tocToggle.install()
    tocToggle.refresh()
}
