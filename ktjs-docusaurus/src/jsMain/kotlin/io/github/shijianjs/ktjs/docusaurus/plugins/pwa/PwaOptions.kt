package io.github.shijianjs.ktjs.docusaurus.plugins.pwa

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions
import io.github.shijianjs.ktjs.docusaurus.config.StringOrFalse
import js.objects.Record
import js.reflect.unsafeCast
import kotlin.js.JsAny

/**
 * Options of `@docusaurus/plugin-pwa`: a service worker (Workbox), an offline mode and an offline
 * notification popup.
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-pwa">PWA plugin</a>
 */
external interface PwaOptions : PluginOptions {
    /** Turn on Workbox logging, unoptimised service worker output and source maps. */
    var debug: Boolean?

    /** How the offline mode gets activated: any combination of the [OfflineModeActivationStrategies]. */
    var offlineModeActivationStrategies: Array<OfflineModeActivationStrategy>?

    /**
     * Options for `workbox.injectManifest()`, which decides what gets precached. Upstream type is
     * `InjectManifestOptions` from `workbox-build`, a package of its own.
     */
    var injectManifestConfig: JsAny?

    /** Extra `<head>` tags injected into every page, for PWA compliance (`manifest`, icons, …). */
    var pwaHead: Array<PwaHeadTag>?

    /** Path of a custom service worker file, relative to the site directory. */
    var swCustom: String?

    /** Script that registers the service worker; `false` disables registration entirely. */
    var swRegister: StringOrFalse?
}

/**
 * Upstream `{tagName: string; href?: string; content?: string; [attributeName: string]: string |
 * boolean | undefined}`.
 *
 * The index signature is modelled by extending `Record`, so any attribute name is accepted through
 * `this["rel"] = "icon"` inside `unsafeJso`; [tagName], [href] and [content] stay typed because they
 * are the ones Docusaurus itself reads.
 */
external interface PwaHeadTag : Record<JsAny, StringOrBoolean> {
    /** Name of the tag, e.g. `link` or `meta`. */
    var tagName: String

    /** `href` attribute, for `link` tags. */
    var href: String?

    /** `content` attribute, for `meta` tags. */
    var content: String?
}

/** Upstream `string | boolean`. */
external interface StringOrBoolean

/** Wraps the [String] branch. */
inline fun StringOrBoolean(value: String): StringOrBoolean = unsafeCast<StringOrBoolean>(value)

/** Wraps the [Boolean] branch. */
inline fun StringOrBoolean(value: Boolean): StringOrBoolean = unsafeCast<StringOrBoolean>(value)

/** Upstream `'appInstalled' | 'queryString' | 'standalone' | 'mobile' | 'saveData' | 'always'`. */
sealed external interface OfflineModeActivationStrategy

/** Wraps a value coming from JavaScript. Prefer [OfflineModeActivationStrategies]. */
inline fun OfflineModeActivationStrategy(value: String): OfflineModeActivationStrategy =
    unsafeCast<OfflineModeActivationStrategy>(value)

/** The literals of [OfflineModeActivationStrategy]. */
object OfflineModeActivationStrategies {
    /** The site is installed as an app (not reliable everywhere). */
    inline val appInstalled: OfflineModeActivationStrategy get() = OfflineModeActivationStrategy("appInstalled")

    /** The URL contains `offlineMode=true`. */
    inline val queryString: OfflineModeActivationStrategy get() = OfflineModeActivationStrategy("queryString")

    /** The app runs standalone, which is usually the case once a PWA is installed. */
    inline val standalone: OfflineModeActivationStrategy get() = OfflineModeActivationStrategy("standalone")

    /** The viewport is 940px wide or narrower. */
    inline val mobile: OfflineModeActivationStrategy get() = OfflineModeActivationStrategy("mobile")

    /** `navigator.connection.saveData` is `true`. */
    inline val saveData: OfflineModeActivationStrategy get() = OfflineModeActivationStrategy("saveData")

    /** Every visitor. */
    inline val always: OfflineModeActivationStrategy get() = OfflineModeActivationStrategy("always")
}
