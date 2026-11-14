package io.github.shijianjs.ktjs.docusaurus.config

import js.objects.unsafeJso
import js.reflect.unsafeCast
import kotlin.js.JsAny
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Upstream accepts `string | [string, opts] | [module, opts] | module | false | null` for a plugin
 * and `string | [string, opts] | false | null` for a preset.
 *
 * The bare-name form has to stay a plain string: Docusaurus validates the tuple form as exactly two
 * elements, so a one-element array is rejected with "Bad Docusaurus plugin/themes value".
 */
class PluginConfigTest {

    @Test
    fun bareNameStaysAString() {
        assertEquals("my-plugin", PluginConfig("my-plugin").unsafeCast<String>())
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
    fun presetBareNameStaysAString() {
        assertEquals("classic", PresetConfig("classic").unsafeCast<String>())
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
