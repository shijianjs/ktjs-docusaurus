package io.github.shijianjs.ktjs.docusaurus.demo

import io.github.shijianjs.ktjs.docusaurus.demo.client.registerKtjsElements
import io.github.shijianjs.ktjs.docusaurus.kit.TocToggle
import kotlin.js.JsExport

/**
 * The site's client module, re-exported by the generated `client-module.mjs`.
 *
 * Docusaurus evaluates this module in two places: in the browser, and during the Node prerender
 * pass. Everything here touches `document` or the custom-element registry, so it is guarded and only
 * runs in the browser.
 *
 * Registering the landing-page elements here — rather than from a `<script>` in the page — matters:
 * `onRouteDidUpdate` runs after hydration, so the tags React rendered are upgraded afterwards instead
 * of being changed underneath it (which makes React log hydration error #418).
 */
private val tocToggle = TocToggle()

private val isBrowser: Boolean = js("typeof window !== 'undefined'")

/** Called by Docusaurus after every route change. */
@JsExport
fun onRouteDidUpdate() {
    if (!isBrowser) return
    registerKtjsElements()
    tocToggle.install()
    tocToggle.refresh()
}
