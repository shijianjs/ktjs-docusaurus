package io.github.shijianjs.ktjs.docusaurus.config

import kotlin.js.JsAny
import js.reflect.unsafeCast

/**
 * Upstream `{id?: string} & {[key: string]: unknown}`.
 *
 * Declare the concrete plugin options in Kotlin instead of filling a loose record: every option type
 * in this wrapper extends this interface, so `unsafeJso<DocsOptions> { ... }` type-checks the keys it
 * knows about.
 */
external interface PluginOptions {
    /** Plugin instance id, for plugins that can be registered more than once. */
    var id: String?
}

/**
 * Upstream `string | [string, opts] | [pluginModule, opts] | pluginModule | false | null`.
 *
 * Build one with the factories below:
 *
 * ```kotlin
 * PluginConfig("@docusaurus/plugin-sitemap", unsafeJso<SitemapOptions> { changefreq = "weekly" })
 * PluginConfig("my-plugin") // a bare package name
 * ```
 *
 * `null` is expressed as Kotlin `null` in [Config.plugins]; `false` disables a plugin that a preset
 * enabled and is expressed with [PluginConfig] and `false`.
 */
external interface PluginConfig

/** A bare package name, with the plugin's default options. */
inline fun PluginConfig(name: String): PluginConfig =
    unsafeCast<PluginConfig>(arrayOf<JsAny>(unsafeCast<JsAny>(name)))

/** A package name (or a `@site/...` module path) with its options. */
inline fun PluginConfig(name: String, options: PluginOptions): PluginConfig =
    unsafeCast<PluginConfig>(arrayOf<JsAny>(unsafeCast<JsAny>(name), unsafeCast<JsAny>(options)))

/** A plugin module imported from JavaScript, with its options. */
inline fun PluginConfig(module: JsAny, options: PluginOptions): PluginConfig =
    unsafeCast<PluginConfig>(arrayOf<JsAny>(module, unsafeCast<JsAny>(options)))

/**
 * Upstream `false`: disables a plugin a preset registered.
 *
 * Only `false` makes sense here; passing `true` is a configuration error in Docusaurus.
 */
inline fun PluginConfig(enabled: Boolean): PluginConfig = unsafeCast<PluginConfig>(enabled)

/**
 * Upstream `string | [string, opts] | false | null`.
 *
 * Presets are registered with the same tuple shape as plugins. Build one with the factories below,
 * or use [io.github.shijianjs.ktjs.docusaurus.presets.classicPreset].
 */
external interface PresetConfig

/** A bare preset package name, with the preset's default options. */
inline fun PresetConfig(name: String): PresetConfig =
    unsafeCast<PresetConfig>(arrayOf<JsAny>(unsafeCast<JsAny>(name)))

/** A preset package name with its options. */
inline fun PresetConfig(name: String, options: JsAny): PresetConfig =
    unsafeCast<PresetConfig>(arrayOf<JsAny>(unsafeCast<JsAny>(name), options))

/** Upstream `false`: disables a preset a parent preset registered. */
inline fun PresetConfig(enabled: Boolean): PresetConfig = unsafeCast<PresetConfig>(enabled)
