package io.github.shijianjs.ktjs.docusaurus.plugins.googleanalytics

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions

/**
 * Options of `@docusaurus/plugin-google-analytics`, also the `googleAnalytics` entry of the classic
 * preset.
 *
 * Upstream marks this plugin as legacy: Google replaced Universal Analytics with GA4, which is what
 * `@docusaurus/plugin-google-gtag` configures. It is still wrapped because the package is still
 * published and still accepted by the preset.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-google-analytics">Google Analytics plugin</a>
 */
external interface GoogleAnalyticsOptions : PluginOptions {
    /** Universal Analytics tracking id, e.g. `UA-XXXXXXXXX-X`. */
    var trackingID: String

    /** Ask Google to anonymize the stored IP address. */
    var anonymizeIP: Boolean?
}
