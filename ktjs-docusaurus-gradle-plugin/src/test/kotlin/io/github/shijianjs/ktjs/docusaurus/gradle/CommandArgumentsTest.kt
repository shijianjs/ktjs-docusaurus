package io.github.shijianjs.ktjs.docusaurus.gradle

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandArgumentsTest {

    @Test
    fun `without overrides the base list is used as written`() {
        assertEquals(
            listOf("start", "--no-open", "--locale", "zh-Hans"),
            CommandArguments.resolve(listOf("start", "--no-open", "--locale", "zh-Hans"), null, null, null),
        )
    }

    @Test
    fun `a locale replaces the one the site committed`() {
        assertEquals(
            listOf("start", "--no-open", "--locale", "en"),
            CommandArguments.resolve(listOf("start", "--no-open", "--locale", "zh-Hans"), null, "en", null),
        )
    }

    @Test
    fun `the equals spelling is replaced too`() {
        assertEquals(
            listOf("start", "--no-open", "--locale", "en"),
            CommandArguments.resolve(listOf("start", "--no-open", "--locale=zh-Hans"), null, "en", null),
        )
    }

    @Test
    fun `a port is appended and its old value dropped`() {
        assertEquals(
            listOf("start", "--no-open", "--port", "3001"),
            CommandArguments.resolve(listOf("start", "--no-open", "--port", "3000"), null, null, 3001),
        )
    }

    @Test
    fun `unmodelled flags are appended after the base list`() {
        assertEquals(
            listOf("start", "--no-open", "--host", "0.0.0.0", "--poll", "1000"),
            CommandArguments.resolve(listOf("start", "--no-open"), "  --host 0.0.0.0 --poll 1000  ", null, null),
        )
    }

    @Test
    fun `unmodelled flags come before the typed ones`() {
        assertEquals(
            listOf("start", "--no-open", "--host", "0.0.0.0", "--locale", "en", "--port", "3001"),
            CommandArguments.resolve(
                base = listOf("start", "--no-open"),
                extra = "--host 0.0.0.0",
                locale = "en",
                port = 3001,
            ),
        )
    }
}
