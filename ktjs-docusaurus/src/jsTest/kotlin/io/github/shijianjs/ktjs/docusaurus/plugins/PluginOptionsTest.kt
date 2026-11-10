package io.github.shijianjs.ktjs.docusaurus.plugins

import io.github.shijianjs.ktjs.docusaurus.plugins.clientredirects.ClientRedirectRule
import io.github.shijianjs.ktjs.docusaurus.plugins.clientredirects.ClientRedirectsOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.clientredirects.StringOrStringArray as RedirectFroms
import io.github.shijianjs.ktjs.docusaurus.plugins.contentblog.BlogOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.contentblog.BlogXsltOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.contentblog.BooleanOrBlogXsltOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.contentblog.StringOrBoolean as BlogStringOrBoolean
import io.github.shijianjs.ktjs.docusaurus.plugins.contentdocs.DocsVersionBanners
import io.github.shijianjs.ktjs.docusaurus.plugins.contentpages.PagesEditUrlParams
import io.github.shijianjs.ktjs.docusaurus.plugins.contentpages.PagesOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.contentpages.StringOrPagesEditUrlFunction
import io.github.shijianjs.ktjs.docusaurus.plugins.gtag.GtagOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.gtag.StringOrStringArray as GtagTrackingIds
import io.github.shijianjs.ktjs.docusaurus.plugins.idealimage.IdealImageOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.pwa.OfflineModeActivationStrategies
import io.github.shijianjs.ktjs.docusaurus.plugins.pwa.PwaHeadTag
import io.github.shijianjs.ktjs.docusaurus.plugins.pwa.PwaOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.pwa.StringOrBoolean as PwaStringOrBoolean
import io.github.shijianjs.ktjs.docusaurus.plugins.sitemap.SitemapLastmodOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.sitemap.SitemapOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.vercelanalytics.VercelAnalyticsModes
import io.github.shijianjs.ktjs.docusaurus.theme.PlaygroundPositions
import js.objects.unsafeJso
import js.reflect.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Upstream sources: the 3.10.2 `.d.ts` of every official plugin that is not a content plugin.
 *
 * The values come from the package own schemas (`lib/options.d.ts` and the `declare module` blocks
 * in `src`), which is what Docusaurus validates the configuration against, so a wrong literal here
 * is a build error at runtime rather than a typo the compiler should have caught.
 */
class PluginOptionsTest {

    @Test
    fun sitemapLastmodLiterals() {
        assertEquals("date", SitemapLastmodOptions.date.unsafeCast<String>())
        assertEquals("datetime", SitemapLastmodOptions.datetime.unsafeCast<String>())
    }

    @Test
    fun docsVersionBannerLiterals() {
        assertEquals("none", DocsVersionBanners.none.unsafeCast<String>())
        assertEquals("unreleased", DocsVersionBanners.unreleased.unsafeCast<String>())
        assertEquals("unmaintained", DocsVersionBanners.unmaintained.unsafeCast<String>())
    }

    @Test
    fun playgroundPositionLiterals() {
        assertEquals("top", PlaygroundPositions.top.unsafeCast<String>())
        assertEquals("bottom", PlaygroundPositions.bottom.unsafeCast<String>())
    }

    @Test
    fun vercelAnalyticsModeLiterals() {
        assertEquals("auto", VercelAnalyticsModes.auto.unsafeCast<String>())
        assertEquals("production", VercelAnalyticsModes.production.unsafeCast<String>())
        assertEquals("development", VercelAnalyticsModes.development.unsafeCast<String>())
    }

    @Test
    fun offlineModeActivationStrategyLiterals() {
        assertEquals("appInstalled", OfflineModeActivationStrategies.appInstalled.unsafeCast<String>())
        assertEquals("queryString", OfflineModeActivationStrategies.queryString.unsafeCast<String>())
        assertEquals("standalone", OfflineModeActivationStrategies.standalone.unsafeCast<String>())
        assertEquals("mobile", OfflineModeActivationStrategies.mobile.unsafeCast<String>())
        assertEquals("saveData", OfflineModeActivationStrategies.saveData.unsafeCast<String>())
        assertEquals("always", OfflineModeActivationStrategies.always.unsafeCast<String>())
    }

    @Test
    fun sitemapKeepsLastmodAndChangefreqApart() {
        val options = unsafeJso<SitemapOptions> {
            lastmod = SitemapLastmodOptions.datetime
            changefreq = io.github.shijianjs.ktjs.docusaurus.plugins.sitemap.SitemapChangefreqs.monthly
            priority = 0.8
        }

        assertEquals("datetime", options.lastmod.unsafeCast<String>())
        assertEquals("monthly", options.changefreq.unsafeCast<String>())
        assertEquals(0.8, options.priority)
    }

    @Test
    fun clientRedirectsAcceptsOneOrManyFromPaths() {
        val options = unsafeJso<ClientRedirectsOptions> {
            fromExtensions = arrayOf(".html")
            redirects = arrayOf(
                unsafeJso<ClientRedirectRule> {
                    to = "/docs/intro"
                    from = RedirectFroms("/intro")
                },
                unsafeJso<ClientRedirectRule> {
                    to = "/docs/kit"
                    from = RedirectFroms(arrayOf("/kit", "/plugins"))
                },
            )
        }

        assertEquals(listOf(".html"), options.fromExtensions?.toList())
        val rules = assertNotNull(options.redirects)
        assertEquals("/intro", rules[0].from.unsafeCast<String>())
        assertEquals(listOf("/kit", "/plugins"), rules[1].from.unsafeCast<Array<String>>().toList())
    }

    @Test
    fun gtagAcceptsOneOrManyTrackingIds() {
        assertEquals("G-1", unsafeJso<GtagOptions> { trackingID = GtagTrackingIds("G-1") }.trackingID.unsafeCast<String>())

        val several = unsafeJso<GtagOptions> {
            trackingID = GtagTrackingIds(arrayOf("G-1", "G-2"))
        }
        assertEquals(listOf("G-1", "G-2"), several.trackingID.unsafeCast<Array<String>>().toList())
    }

    @Test
    fun pwaHeadTagAcceptsArbitraryAttributes() {
        val tag = unsafeJso<PwaHeadTag> {
            tagName = "link"
            this["rel"] = PwaStringOrBoolean("icon")
            this["crossorigin"] = PwaStringOrBoolean(true)
        }

        assertEquals("link", tag.tagName)
        assertEquals("icon", tag["rel"]?.unsafeCast<String>())
        assertEquals(true, tag["crossorigin"]?.unsafeCast<Boolean>())
    }

    @Test
    fun pwaOfflineStrategiesStayAList() {
        val options = unsafeJso<PwaOptions> {
            offlineModeActivationStrategies = arrayOf(
                OfflineModeActivationStrategies.appInstalled,
                OfflineModeActivationStrategies.saveData,
            )
            swRegister = io.github.shijianjs.ktjs.docusaurus.config.StringOrFalse("./registerSW.js")
        }

        val strategies = assertNotNull(options.offlineModeActivationStrategies)
        assertEquals(listOf("appInstalled", "saveData"), strategies.map { it.unsafeCast<String>() })
        assertEquals("./registerSW.js", options.swRegister.unsafeCast<String>())
    }

    @Test
    fun idealImageWidthsAreNumbers() {
        val options = unsafeJso<IdealImageOptions> {
            sizes = arrayOf(320.0, 640.0)
            quality = 70.0
            disableInDev = true
        }

        assertEquals(listOf(320.0, 640.0), options.sizes?.toList())
        assertEquals(70.0, options.quality)
        assertEquals(true, options.disableInDev)
    }

    @Test
    fun blogXsltAcceptsBooleanAndPerFormatObject() {
        assertEquals(
            true,
            unsafeJso<BlogOptions> { feedOptions = unsafeJso { xslt = BooleanOrBlogXsltOptions(true) } }
                .feedOptions?.xslt?.unsafeCast<Boolean>(),
        )

        val perFormat = unsafeJso<BlogXsltOptions> {
            rss = BlogStringOrBoolean("/feeds/rss.xsl")
            atom = BlogStringOrBoolean(false)
        }
        val options = unsafeJso<BlogOptions> {
            feedOptions = unsafeJso { xslt = BooleanOrBlogXsltOptions(perFormat) }
        }

        val xslt = options.feedOptions?.xslt?.unsafeCast<BlogXsltOptions>()
        assertEquals("/feeds/rss.xsl", xslt?.rss?.unsafeCast<String>())
        assertEquals(false, xslt?.atom?.unsafeCast<Boolean>())
    }

    @Test
    fun pagesEditUrlCallbackIsAFunction() {
        val options = unsafeJso<PagesOptions> {
            editUrl = StringOrPagesEditUrlFunction { params: PagesEditUrlParams ->
                "https://github.com/shijianjs/ktjs-docusaurus/edit/main/${params.pagesPath}"
            }
        }

        val callback = options.editUrl?.unsafeCast<(PagesEditUrlParams) -> String>()
        val params = unsafeJso<PagesEditUrlParams> {
            pagesDirPath = "src/pages"
            pagesPath = "index.md"
            permalink = "/index.html"
            locale = "en"
        }
        assertEquals(
            "https://github.com/shijianjs/ktjs-docusaurus/edit/main/index.md",
            callback?.invoke(params),
        )
    }
}
