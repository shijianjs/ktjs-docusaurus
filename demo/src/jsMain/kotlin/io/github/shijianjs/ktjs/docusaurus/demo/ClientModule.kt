package io.github.shijianjs.ktjs.docusaurus.demo

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

/** Called by Docusaurus after every route change. */
@JsExport
fun onRouteDidUpdate() {
    if (!isBrowser) return
    loadBrowserBundle(SITE_BASE_URL + "ktjs-client/index.mjs")
    tocToggle.install()
    tocToggle.refresh()
}
