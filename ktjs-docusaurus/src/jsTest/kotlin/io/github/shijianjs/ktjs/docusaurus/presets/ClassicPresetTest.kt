package io.github.shijianjs.ktjs.docusaurus.presets

import js.objects.unsafeJso
import js.reflect.unsafeCast
import kotlin.js.JsAny
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Upstream source: `@docusaurus/preset-classic` (`lib/index.js`) — a preset is registered as
 * `['classic', options]`, and each content plugin can be disabled with `false`.
 */
class ClassicPresetTest {

    @Test
    fun presetIsTheClassicTuple() {
        val options = unsafeJso<ClassicOptions> {
            blog = BlogOptionsOrFalse(false)
            pages = PagesOptionsOrFalse(false)
        }

        val value = classicPreset(options).unsafeCast<Array<JsAny>>()

        assertEquals(2, value.size)
        assertEquals("classic", value[0].unsafeCast<String>())
        assertEquals(false, value[1].unsafeCast<ClassicOptions>().blog.unsafeCast<Boolean>())
        assertEquals(false, value[1].unsafeCast<ClassicOptions>().pages.unsafeCast<Boolean>())
    }

    @Test
    fun docsCanBeDisabledOrConfigured() {
        assertEquals(true, DocsOptionsOrFalse(true).unsafeCast<Boolean>())
        assertEquals(false, SitemapOptionsOrFalse(false).unsafeCast<Boolean>())
        assertEquals(false, SvgrOptionsOrFalse(false).unsafeCast<Boolean>())
    }
}
