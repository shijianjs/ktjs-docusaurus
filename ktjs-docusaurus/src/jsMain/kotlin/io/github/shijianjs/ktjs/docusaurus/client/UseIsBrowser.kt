@file:JsModule("@docusaurus/useIsBrowser")

package io.github.shijianjs.ktjs.docusaurus.client

import kotlin.js.JsName

/**
 * `true` in the browser, `false` during the Node prerender pass.
 *
 * Components that touch `window` / `document` directly must branch on this instead of testing for
 * `window` themselves: the value changes between the prerender and the hydration render, which is
 * exactly what React needs to know.
 */
@JsName("default")
external val useIsBrowser: () -> Boolean
