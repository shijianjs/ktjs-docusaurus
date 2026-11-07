package io.github.shijianjs.docusaurus.config

import js.objects.unsafeJso
import js.reflect.unsafeCast
import kotlin.js.JsAny
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Upstream accepts `string | [string, opts] | [module, opts] | module | false | null` for a plugin
 * and `string | [string, opts] | false | null` for a preset. The factories build real JS arrays, so
 * Docusaurus' own validation sees the shape it expects.
 */
class PluginConfigTest {

    @Test
    fun bareNameIsASingleElementArray() {
        val value = PluginConfig("my-plugin").unsafeCast<Array<JsAny>>()

        assertEquals(1, value.size)
        assertEquals("my-plugin", value[0].unsafeCast<String>())
    }

    @Test
    fun nameAndOptionsIsATwoElementArray() {
        val options = unsafeJso<PluginOptions> { id = "custom" }
        val value = PluginConfig("my-plugin", options).unsafeCast<Array<JsAny>>()

        assertEquals(2, value.size)
        assertEquals("my-plugin", value[0].unsafeCast<String>())
        assertEquals("custom", value[1].unsafeCast<PluginOptions>().id)
    }

    @Test
    fun moduleAndOptionsIsATwoElementArray() {
        val options = unsafeJso<PluginOptions> { id = "custom" }
        val value = PluginConfig(options.unsafeCast<JsAny>(), options).unsafeCast<Array<JsAny>>()

        assertEquals(2, value.size)
        assertEquals("custom", value[1].unsafeCast<PluginOptions>().id)
    }

    @Test
    fun falseStaysFalse() {
        assertEquals(false, PluginConfig(false).unsafeCast<Boolean>())
    }

    @Test
    fun presetNameIsASingleElementArray() {
        val value = PresetConfig("classic").unsafeCast<Array<JsAny>>()

        assertEquals(1, value.size)
        assertEquals("classic", value[0].unsafeCast<String>())
    }

    @Test
    fun presetNameAndOptionsIsATwoElementArray() {
        val options = unsafeJso<PluginOptions> { id = "site" }
        val value = PresetConfig("classic", options).unsafeCast<Array<JsAny>>()

        assertEquals(2, value.size)
        assertEquals("classic", value[0].unsafeCast<String>())
        assertEquals("site", value[1].unsafeCast<PluginOptions>().id)
    }

    @Test
    fun presetFalseStaysFalse() {
        assertEquals(false, PresetConfig(false).unsafeCast<Boolean>())
    }
}
