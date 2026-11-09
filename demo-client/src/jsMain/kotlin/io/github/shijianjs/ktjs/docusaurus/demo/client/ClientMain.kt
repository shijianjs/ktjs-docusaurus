package io.github.shijianjs.ktjs.docusaurus.demo.client

import io.github.shijianjs.ktjs.docusaurus.kit.TocToggle
import kotlin.js.JsExport

/**
 * Entry point of the site's browser-only bundle, called by the generated `ktjs-client/index.mjs`.
 *
 * The bundle is published under `static/` and loaded with `<script type="module">` instead of going
 * through `clientModules`: it defines custom-element classes, which read `HTMLElement` when the module
 * loads, and Docusaurus also puts `clientModules` into the server bundle that Node evaluates.
 */
private val tocToggle = TocToggle()

/** Registers the landing-page tags and attaches the TOC button. */
@JsExport
fun start() {
    registerKtjsElements()
    tocToggle.install()
    tocToggle.refresh()
}
