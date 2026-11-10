package io.github.shijianjs.ktjs.docusaurus.plugins.csscascadelayers

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import js.objects.Record
import kotlin.js.JsAny

/**
 * Options of `@docusaurus/plugin-css-cascade-layers`: wraps the site's CSS in `@layer` blocks, so the
 * cascade order no longer depends on import order.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-css-cascade-layers">CSS cascade layers plugin</a>
 */
external interface CssCascadeLayersOptions : PluginOptions {
    /**
     * Layer name to matcher. A CSS file matches a layer when its path satisfies the callback; the
     * declaration order of the record is the cascade order, lowest priority first.
     */
    var layers: Record<JsAny, (String) -> Boolean>?
}
