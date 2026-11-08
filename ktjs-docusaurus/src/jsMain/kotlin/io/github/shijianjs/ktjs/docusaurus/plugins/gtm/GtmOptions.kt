package io.github.shijianjs.ktjs.docusaurus.plugins.gtm

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions

/**
 * Options of `@docusaurus/plugin-google-tag-manager`, also the `googleTagManager` entry of the
 * classic preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-google-tag-manager">Google Tag Manager plugin</a>
 */
external interface GtmOptions : PluginOptions {
    /** Tag Manager container id, e.g. `GTM-XXXXXXX`. */
    var containerId: String
}
