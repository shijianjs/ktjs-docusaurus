package io.github.shijianjs.ktjs.docusaurus.docs

import io.github.shijianjs.ktjs.docusaurus.kit.TocToggle
import kotlin.js.JsExport

/**
 * The site's client module, re-exported by the generated `client-module.mjs`.
 *
 * Docusaurus evaluates this module in the browser and during the Node prerender pass, so every DOM
 * access is guarded.
 *
 * The custom-element package (`ktjs-docusaurus-kit-elements`) is loaded by inserting a module script
 * **after hydration**: `onRouteDidUpdate` runs once hydration is done, so React has already rendered
 * the `<ktjs-*>` tags and registering them afterwards produces no hydration mismatch (#418). The
 * script is inserted once; the element registry itself is idempotent as well.
 */
private val tocToggle = TocToggle()

private val isBrowser: Boolean = js("typeof window !== 'undefined'")

private val loadBrowserBundle: (String) -> Unit = js(
    """
    function (url) {
      if (document.querySelector('script[data-ktjs-client]')) {
        return;
      }
      var script = document.createElement('script');
      script.type = 'module';
      script.src = url;
      script.setAttribute('data-ktjs-client', '');
      document.head.appendChild(script);
    }
    """,
)

/**
 * Called by Docusaurus after every route change.
 *
 * [siteBaseUrl] is the one Docusaurus resolved for this bundle's locale — `/ktjs-docusaurus/` for
 * English, `/ktjs-docusaurus/zh-Hans/` for Chinese, in the built site *and* in the dev server, which
 * serves a single locale under its own prefix. A compile-time constant cannot know that, and the
 * bundle is served under the localized prefix, so the URL has to come from here.
 */
@JsExport
fun onRouteDidUpdate(siteBaseUrl: String) {
    if (!isBrowser) return
    loadBrowserBundle(siteBaseUrl + "ktjs-client/index.mjs")
    tocToggle.install()
    tocToggle.refresh()
}
