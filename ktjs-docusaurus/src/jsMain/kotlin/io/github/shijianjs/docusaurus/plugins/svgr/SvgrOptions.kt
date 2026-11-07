package io.github.shijianjs.docusaurus.plugins.svgr

import io.github.shijianjs.docusaurus.config.PluginOptions
import kotlin.js.JsAny

/**
 * Options of `@docusaurus/plugin-svgr`, also the `svgr` entry of the classic preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-svgr">SVGR plugin</a>
 */
external interface SvgrOptions : PluginOptions {
    /**
     * Options passed to `@svgr/webpack`. Upstream type is SVGR's `Config`, which belongs to that
     * package rather than to Docusaurus, so it stays opaque here.
     */
    var svgrConfig: JsAny?
}
