package io.github.shijianjs.ktjs.docusaurus.plugins.vercelanalytics

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import js.reflect.unsafeCast

/**
 * Options of `@docusaurus/plugin-vercel-analytics`: injects Vercel Analytics into the head of every
 * page.
 *
 * @see <a href="https://vercel.com/docs/analytics">Vercel Analytics</a>
 */
external interface VercelAnalyticsOptions : PluginOptions {
    /** When the script is injected; `'auto'` follows the build mode. */
    var mode: VercelAnalyticsMode?

    /** Log what the plugin injects. */
    var debug: Boolean?
}

/** Upstream `'auto' | 'production' | 'development'`. */
sealed external interface VercelAnalyticsMode

/** Wraps a value coming from JavaScript. Prefer [VercelAnalyticsModes]. */
inline fun VercelAnalyticsMode(value: String): VercelAnalyticsMode = unsafeCast<VercelAnalyticsMode>(value)

/** The literals of [VercelAnalyticsMode]. */
object VercelAnalyticsModes {
    /** Inject in production builds only. */
    inline val auto: VercelAnalyticsMode get() = VercelAnalyticsMode("auto")

    /** Always inject. */
    inline val production: VercelAnalyticsMode get() = VercelAnalyticsMode("production")

    /** Inject in dev builds too. */
    inline val development: VercelAnalyticsMode get() = VercelAnalyticsMode("development")
}
