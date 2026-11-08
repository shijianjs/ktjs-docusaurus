package io.github.shijianjs.ktjs.docusaurus.demo

import io.github.shijianjs.ktjs.docusaurus.kit.TocToggle
import kotlin.js.JsExport

/**
 * The site's client module, re-exported by the generated `client-module.mjs`.
 *
 * Docusaurus evaluates this module twice: in the browser, and during the Node prerender pass. The
 * toggle touches `document`, so the instance is created lazily and only installed in the browser.
 */
private val tocToggle = TocToggle()

private val isBrowser: Boolean = js("typeof window !== 'undefined'")

/**
 * Called by Docusaurus after every route change.
 *
 * `install()` is idempotent (it wires the viewport listener once); `refresh()` re-attaches the
 * button to the freshly rendered table of contents.
 */
@JsExport
fun onRouteDidUpdate() {
    if (!isBrowser) return
    tocToggle.install()
    tocToggle.refresh()
}
