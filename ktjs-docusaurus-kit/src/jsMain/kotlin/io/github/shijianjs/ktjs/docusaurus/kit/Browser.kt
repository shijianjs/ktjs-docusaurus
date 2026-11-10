package io.github.shijianjs.ktjs.docusaurus.kit

/**
 * The two browser APIs the generated kotlin-browser bindings leave out.
 *
 * `localStorage` and `setTimeout` are globals rather than members of the generated `Window`
 * interface in this binding version, so they are declared here instead of reaching for `dynamic`.
 */

/** The Web Storage surface the TOC toggle uses. */
internal external interface WebStorage {
    fun getItem(key: String): String?

    fun setItem(key: String, value: String)
}

/** `localStorage`, or `null` where the environment has no Web Storage. */
@Suppress("UNCHECKED_CAST")
internal fun localStorageOrNull(): WebStorage? =
    js("typeof localStorage === 'undefined' ? null : localStorage") as WebStorage?

/** The global `setTimeout`. */
internal external fun setTimeout(handler: () -> Unit, timeout: Int)

/** Reinterprets a DOM element as the concrete subtype the caller knows it is. */
@Suppress("UNCHECKED_CAST")
internal fun <T> web.dom.Element.unsafeCastElement(): T = this as T

/** Appends an inline `<style>` with [css] as its only child. */
internal fun appendStyle(host: web.dom.Element, css: String) {
    val style = web.dom.document.createElement("style").unsafeCastElement<web.html.HTMLStyleElement>()
    style.textContent = css
    host.appendChild(style)
}
