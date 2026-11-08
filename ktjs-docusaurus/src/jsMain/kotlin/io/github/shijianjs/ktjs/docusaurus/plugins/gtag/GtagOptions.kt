package io.github.shijianjs.ktjs.docusaurus.plugins.gtag

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions

/**
 * Options of `@docusaurus/plugin-google-gtag`, also the `gtag` entry of the classic preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-google-gtag">gtag plugin</a>
 */
external interface GtagOptions : PluginOptions {
    /** Google Analytics measurement id, e.g. `G-XXXXXXXXXX`. */
    var trackingID: String

    /** Ask Google to anonymize the stored IP address. */
    var anonymizeIP: Boolean?
}
