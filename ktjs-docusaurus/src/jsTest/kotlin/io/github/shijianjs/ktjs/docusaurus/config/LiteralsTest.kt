package io.github.shijianjs.ktjs.docusaurus.config

import js.reflect.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The literal objects are the only place a string from upstream can be misspelt, so every constant
 * is compared with the exact upstream value.
 *
 * Upstream sources: `@docusaurus/types` (`reporting.d.ts`, `config.d.ts`), `@docusaurus/theme-common`
 * (`utils/useThemeConfig.ts`).
 */
class LiteralsTest {

    @Test
    fun reportingSeverity() {
        assertEquals("ignore", ReportingSeverities.ignore.unsafeCast<String>())
        assertEquals("log", ReportingSeverities.log.unsafeCast<String>())
        assertEquals("warn", ReportingSeverities.warn.unsafeCast<String>())
        assertEquals("throw", ReportingSeverities.`throw`.unsafeCast<String>())
    }

    @Test
    fun routerType() {
        assertEquals("browser", RouterTypes.browser.unsafeCast<String>())
        assertEquals("hash", RouterTypes.hash.unsafeCast<String>())
    }

    @Test
    fun direction() {
        assertEquals("ltr", Directions.ltr.unsafeCast<String>())
        assertEquals("rtl", Directions.rtl.unsafeCast<String>())
    }

    @Test
    fun siteStorageType() {
        assertEquals("localStorage", SiteStorageTypes.localStorage.unsafeCast<String>())
        assertEquals("sessionStorage", SiteStorageTypes.sessionStorage.unsafeCast<String>())
    }

    @Test
    fun markdownFormat() {
        assertEquals("mdx", MarkdownFormats.mdx.unsafeCast<String>())
        assertEquals("md", MarkdownFormats.md.unsafeCast<String>())
        assertEquals("detect", MarkdownFormats.detect.unsafeCast<String>())
    }

    @Test
    fun vcsPreset() {
        assertEquals("git-ad-hoc", VcsPresets.gitAdHoc.unsafeCast<String>())
        assertEquals("git-eager", VcsPresets.gitEager.unsafeCast<String>())
        assertEquals("hardcoded", VcsPresets.hardcoded.unsafeCast<String>())
        assertEquals("disabled", VcsPresets.disabled.unsafeCast<String>())
        assertEquals("default-v1", VcsPresets.defaultV1.unsafeCast<String>())
        assertEquals("default-v2", VcsPresets.defaultV2.unsafeCast<String>())
    }

    @Test
    fun stringFactoryWrapsTheValueUnchanged() {
        assertEquals("custom", ReportingSeverity("custom").unsafeCast<String>())
        assertEquals("hash", RouterType("hash").unsafeCast<String>())
    }
}
