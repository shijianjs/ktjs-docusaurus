package io.github.shijianjs.ktjs.docusaurus.plugins.debug

/**
 * `@docusaurus/plugin-debug` adds the `/__docusaurus/debug` routes.
 *
 * Upstream's signature is `pluginDebug({siteConfig: {baseUrl}}: LoadContext): Plugin<undefined>`,
 * so the plugin has **no options at all** — there is no object to type and nothing to pass. Register
 * it by name:
 *
 * ```kotlin
 * plugins = arrayOf<PluginConfig?>(PluginConfig(DebugPlugin.NAME))
 * ```
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-debug">Debug plugin</a>
 */
object DebugPlugin {
    /** Package name of the plugin. */
    const val NAME: String = "@docusaurus/plugin-debug"
}
