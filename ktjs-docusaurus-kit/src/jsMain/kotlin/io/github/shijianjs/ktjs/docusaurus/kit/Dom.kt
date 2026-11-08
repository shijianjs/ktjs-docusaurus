package io.github.shijianjs.ktjs.docusaurus.kit

import web.cssom.ClassName
import web.dom.document
import web.html.HTMLElement
import web.html.HTMLStyleElement

/**
 * Creates an element and gives it the kit's class names.
 *
 * The kit renders into the page's own DOM (not a shadow root): the sections are mounted by a
 * React page, and a shadow root would have to be opened on an element the page owns. Every class
 * name therefore carries the `ktjs-` prefix, and all styling goes through `--ktjs-*` custom
 * properties so a site can theme the sections without touching this module.
 */
internal fun create(
    tag: String,
    vararg classes: String,
): HTMLElement = document.createElement(tag).also { element ->
    if (classes.isNotEmpty()) {
        element.className = ClassName(classes.joinToString(" "))
    }
}

/** Sets the text of an element; `null` clears it. */
internal fun HTMLElement.text(value: String) {
    textContent = value
}

/** Appends an inline `<style>` with [css] as its only child. */
internal fun appendStyle(host: web.dom.Element, css: String) {
    val style = document.createElement("style").unsafeCastElement<HTMLStyleElement>()
    style.textContent = css
    host.appendChild(style)
}

@Suppress("UNCHECKED_CAST")
internal fun <T> web.dom.Element.unsafeCastElement(): T = this as T

/** An `<iconify-icon>` element; the `icon` attribute carries the Iconify name. */
internal fun iconElement(icon: String, vararg classes: String): HTMLElement =
    create("iconify-icon", *classes).also { element ->
        element.setAttribute("icon", icon)
        element.setAttribute("aria-hidden", "true")
    }
