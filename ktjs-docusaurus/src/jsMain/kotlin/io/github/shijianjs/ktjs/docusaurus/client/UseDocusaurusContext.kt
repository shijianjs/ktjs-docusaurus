@file:JsModule("@docusaurus/useDocusaurusContext")

package io.github.shijianjs.ktjs.docusaurus.client

import kotlin.js.JsAny
import kotlin.js.JsName

/**
 * The context `useDocusaurusContext()` returns: the resolved site configuration plus the runtime
 * data every plugin contributed.
 *
 * Upstream source: `@docusaurus/types` (`DocusaurusContext`).
 */
external interface DocusaurusContext {
    /** The site configuration after validation and normalization. */
    val siteConfig: SiteConfig

    /** Metadata about the site as a whole (`docusaurusVersion`, `pluginVersions`, …). */
    val siteMetadata: JsAny

    /** The active locale, the default locale and every configured locale. */
    val i18n: I18n

    /** Global data by plugin name and plugin id. */
    val globalData: JsAny
}

/** The part of the resolved configuration a page normally reads. */
external interface SiteConfig {
    /** The site title. */
    val title: String

    /** The tagline, empty when the site has none. */
    val tagline: String

    /** The site's origin, without the base URL. */
    val url: String

    /** The path the site is served under, always with leading and trailing slash. */
    val baseUrl: String

    /** Whatever the site put under `customFields`. */
    val customFields: JsAny?
}

/** Upstream `I18n`. */
external interface I18n {
    /** The locale the current page is rendered in. */
    val currentLocale: String

    /** The locale the sources are written in. */
    val defaultLocale: String

    /** Every published locale, the default one first. */
    val locales: Array<String>
}

/**
 * Reads the Docusaurus context inside a component.
 *
 * `@docusaurus/useDocusaurusContext` has a default export, which is what the `@JsName("default")`
 * below compiles to.
 */
@JsName("default")
external val useDocusaurusContext: () -> DocusaurusContext
