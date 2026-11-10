package io.github.shijianjs.ktjs.docusaurus.plugins.searchlocal

import js.objects.Record
import js.objects.unsafeJso
import js.reflect.unsafeCast
import kotlin.js.JsAny
import kotlin.js.RegExp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Upstream source: `@easyops-cn/docusaurus-search-local` (`dist/types/index.d.ts`) version 0.55.3.
 *
 * The theme is registered by name and Docusaurus hands the options object to the package's Joi
 * schema, so what the wrapper has to get right is the shape: literal values, and one factory per
 * branch of every union.
 */
class SearchLocalOptionsTest {

    @Test
    fun hashedLiterals() {
        assertEquals("query", Hasheds.query.unsafeCast<String>())
        assertEquals("filename", Hasheds.filename.unsafeCast<String>())
    }

    @Test
    fun searchBarPositionLiterals() {
        assertEquals("auto", SearchBarPositions.auto.unsafeCast<String>())
        assertEquals("left", SearchBarPositions.left.unsafeCast<String>())
        assertEquals("right", SearchBarPositions.right.unsafeCast<String>())
    }

    @Test
    fun hashedAcceptsBothBranches() {
        assertEquals(true, BooleanOrHashed(true).unsafeCast<Boolean>())
        assertEquals("filename", BooleanOrHashed(Hasheds.filename).unsafeCast<String>())
    }

    @Test
    fun stringOrStringArrayKeepsBothBranches() {
        assertEquals("docs", StringOrStringArray("docs").unsafeCast<String>())
        assertEquals(
            listOf("en", "zh"),
            StringOrStringArray(arrayOf("en", "zh")).unsafeCast<Array<String>>().toList(),
        )
    }

    @Test
    fun booleanOrStringArrayKeepsBothBranches() {
        assertEquals(true, BooleanOrStringArray(true).unsafeCast<Boolean>())
        assertEquals(
            listOf("en"),
            BooleanOrStringArray(arrayOf("en")).unsafeCast<Array<String>>().toList(),
        )
    }

    @Test
    fun ignoreFilesAcceptsStringPatternAndList() {
        assertEquals("/legacy", IgnoreFiles("/legacy").unsafeCast<String>())

        // The factories must not rebuild the pattern: the option goes to a webpack config keyed by
        // object identity, so a copy would be a different regexp.
        val pattern = RegExp("^/legacy")
        assertTrue(IgnoreFiles(pattern).unsafeCast<RegExp>() === pattern)

        val list = IgnoreFiles(arrayOf(StringOrRegExp("/a"), StringOrRegExp(pattern)))
            .unsafeCast<Array<StringOrRegExp>>()
        assertEquals(2, list.size)
        assertEquals("/a", list[0].unsafeCast<String>())
        assertTrue(list[1].unsafeCast<RegExp>() === pattern)
    }

    @Test
    fun searchContextAcceptsBarePathAndLabelledPath() {
        assertEquals("docs", StringOrSearchContextByPath("docs").unsafeCast<String>())

        val labelled = StringOrSearchContextByPath(
            unsafeJso<SearchContextByPath> {
                label = StringOrStringRecord(
                    unsafeJso<Record<JsAny, String>> { this["zh-Hans"] = "文档" },
                )
                path = "docs"
            },
        ).unsafeCast<SearchContextByPath>()

        assertEquals("docs", labelled.path)
        val labels = labelled.label?.unsafeCast<Record<JsAny, String>>()
        assertTrue(labels != null)
        assertEquals("文档", labels["zh-Hans"])
    }

    @Test
    fun optionsKeepEveryField() {
        val options = unsafeJso<SearchLocalOptions> {
            indexDocs = true
            indexBlog = false
            indexPages = false
            docsRouteBasePath = StringOrStringArray("docs")
            language = StringOrStringArray(arrayOf("en", "zh"))
            hashed = BooleanOrHashed(Hasheds.query)
            removeDefaultStopWordFilter = BooleanOrStringArray(arrayOf("en"))
            removeDefaultStemmer = false
            searchResultLimits = 8.0
            searchResultContextMaxLength = 50.0
            searchBarShortcutKeymap = "mod+k"
            searchBarPosition = SearchBarPositions.auto
            fuzzyMatchingDistance = 1.0
        }

        assertEquals(true, options.indexDocs)
        assertEquals(false, options.indexBlog)
        assertEquals("docs", options.docsRouteBasePath?.unsafeCast<String>())
        assertEquals("query", options.hashed?.unsafeCast<String>())
        assertEquals(listOf("en"), options.removeDefaultStopWordFilter?.unsafeCast<Array<String>>()?.toList())
        assertEquals(8.0, options.searchResultLimits)
        assertEquals(50.0, options.searchResultContextMaxLength)
        assertEquals("mod+k", options.searchBarShortcutKeymap)
        assertEquals("auto", options.searchBarPosition?.unsafeCast<String>())
        assertEquals(1.0, options.fuzzyMatchingDistance)
    }
}
