package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.createElement
import web.components.ShadowRoot
import web.components.ShadowRootInit
import web.dom.TagName
import web.html.HTMLElement
import web.html.HTMLStyleElement

/**
 * Attaches an open shadow root and seeds it with [HOME_CSS].
 *
 * Every home element renders into its own shadow tree, so the page's stylesheet cannot reach in and
 * these rules cannot leak out. Theme colours still cross the boundary because custom properties
 * inherit into shadow trees.
 */
internal fun HTMLElement.attachHomeShadow(): ShadowRoot {
    val options = js("""{ mode: 'open' }""") as ShadowRootInit
    val root = attachShadow(options)
    root.appendChild(
        TagName<HTMLStyleElement>("style").createElement {
            textContent = HOME_CSS
        },
    )
    return root
}
