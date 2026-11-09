package io.github.shijianjs.ktjs.docusaurus.demo

import io.github.shijianjs.ktjs.docusaurus.kit.TocToggle
import io.github.shijianjs.ktjs.docusaurus.kit.upgradeHomeElements
import kotlin.js.JsExport

/**
 * The site's client module, re-exported by the generated `client-module.mjs`.
 *
 * Docusaurus evaluates this module in two places: in the browser, and during the Node prerender
 * pass. Everything here touches `document`, so it is created lazily and only run in the browser.
 */
private val tocToggle = TocToggle()

private val isBrowser: Boolean = js("typeof window !== 'undefined'")

/**
 * Called by Docusaurus after every route change.
 *
 * The landing page is MDX: it writes `<ktjs-hero>` / `<ktjs-features>` / `<ktjs-next-steps>` tags, and
 * `upgradeHomeElements()` turns them into the kit's sections. Both steps are idempotent — the upgrade
 * marks the hosts it consumed, and the toggle was already wired on the first call.
 */
@JsExport
fun onRouteDidUpdate() {
    if (!isBrowser) return
    upgradeHomeElements()
    tocToggle.install()
    tocToggle.refresh()
}
