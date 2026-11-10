package io.github.shijianjs.ktjs.docusaurus.plugins.rsdoctor

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import js.objects.Record
import kotlin.js.JsAny

/**
 * Options of `@docusaurus/plugin-rsdoctor`: attaches [Rsdoctor](https://rsdoctor.dev)'s bundle
 * analyser to the client build.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-rsdoctor">Rsdoctor plugin</a>
 */
external interface RsdoctorOptions : PluginOptions {
    /**
     * Passed straight to `@rsdoctor/rspack-plugin`. Upstream type is `Record<string, unknown>`,
     * because the shape belongs to Rsdoctor rather than to Docusaurus.
     */
    var rsdoctorOptions: Record<JsAny, JsAny>?
}
