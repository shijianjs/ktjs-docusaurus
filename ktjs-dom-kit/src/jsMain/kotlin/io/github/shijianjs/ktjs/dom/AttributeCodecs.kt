package io.github.shijianjs.ktjs.dom

/**
 * Plain string value: the attribute is read and written verbatim.
 *
 * A missing attribute decodes to `""`, so a `String` property is never null. Use `String?` as the
 * property type when absence has to be distinguishable — that needs its own codec, since the value
 * would have to survive `encode(null)`, which the delegate reads as "remove the attribute".
 */
object StringAttributeCodec : AttributeCodec<String> {
    override fun encode(value: String): String = value

    override fun decode(value: String): String = value

    override val absent: String = ""
}

/**
 * HTML boolean attribute: presence means `true`, whatever the value says.
 *
 * `<x disabled>` and `<x disabled="false">` are both `true` — this is the semantics the HTML parser
 * gives `checked` / `hidden` / `disabled`, not a Kotlin design choice. `true` writes an empty string
 * (so the attribute shows up bare), `false` removes it.
 */
object BooleanAttributeCodec : AttributeCodec<Boolean> {
    override fun encode(value: Boolean): String? = if (value) "" else null

    override fun decode(value: String): Boolean = true

    override val absent: Boolean = false
}

/** Whole number; anything `toIntOrNull` rejects falls back to the delegate's `default` (or `0`). */
object IntAttributeCodec : AttributeCodec<Int> {
    override fun encode(value: Int): String = value.toString()

    override fun decode(value: String): Int? = value.trim().toIntOrNull()

    override val absent: Int = 0
}

/** Floating point number; anything `toDoubleOrNull` rejects falls back to the default (or `0.0`). */
object DoubleAttributeCodec : AttributeCodec<Double> {
    override fun encode(value: Double): String = value.toString()

    override fun decode(value: String): Double? = value.trim().toDoubleOrNull()

    override val absent: Double = 0.0
}

/**
 * Enum attribute, matched against [label] ignoring case.
 *
 * Kotlin/JS cannot enumerate an enum through reflection, so the values are passed in:
 * `attr(codec = enumAttributeCodec(ActionKind.entries), default = ActionKind.Primary)`. [label]
 * defaults to the lower-cased constant name (`Primary` → `primary`); the constant name itself is
 * accepted when decoding as well. [AttributeCodec.absent] is the first value, which only matters
 * when the delegate has no `default`.
 */
fun <E : Enum<E>> enumAttributeCodec(
    values: List<E>,
    label: (E) -> String = { it.name.lowercase() },
): AttributeCodec<E> = object : AttributeCodec<E> {
    override fun encode(value: E): String = label(value)

    override fun decode(value: String): E? {
        val text = value.trim()
        return values.firstOrNull { label(it).equals(text, ignoreCase = true) }
            ?: values.firstOrNull { it.name.equals(text, ignoreCase = true) }
    }

    override val absent: E = values.first()
}
