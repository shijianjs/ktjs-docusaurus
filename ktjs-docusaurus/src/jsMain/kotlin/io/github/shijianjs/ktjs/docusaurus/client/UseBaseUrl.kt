@file:JsModule("@docusaurus/useBaseUrl")

package io.github.shijianjs.ktjs.docusaurus.client

import kotlin.js.JsName

/**
 * Resolves a path against the site's `baseUrl`.
 *
 * Docusaurus serves a project site from a sub-path (`/ktjs-docusaurus/` on GitHub Pages), so an
 * internal `href` written as `/docs/intro` in a component or a plain `<a>` is wrong without this
 * prefix. `useDocusaurusContext().siteConfig.baseUrl` is available too, but this hook also keeps
 * the value stable across renders.
 */
@JsName("default")
external val useBaseUrl: (String) -> String
