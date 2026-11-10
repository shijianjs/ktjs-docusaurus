package io.github.shijianjs.ktjs.dom

import js.reflect.unsafeCast
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Converts between a delegated property's Kotlin value and the attribute's string form.
 *
 * The built-ins are [StringAttributeCodec], [BooleanAttributeCodec], [IntAttributeCodec],
 * [DoubleAttributeCodec] and [enumAttributeCodec]; everything else (JSON, a timestamp, a parsed
 * record) is an implementation of this interface. No serialization library is involved.
 */
interface AttributeCodec<T> {

    /** The attribute value for [value]; `null` means "remove the attribute". */
    fun encode(value: T): String?

    /**
     * [value] parsed back, or `null` when it is not something this codec can read.
     *
     * A `null` here is treated exactly like a missing attribute: the delegate falls back to its
     * `default`, or to [absent] when it has none. Decoding never throws, so a typo in the markup
     * cannot break the element.
     */
    fun decode(value: String): T?

    /** The value a delegate reports while the attribute is absent and it has no `default`. */
    val absent: T
}

/**
 * One change of one attribute, as an `attr` delegate saw it.
 *
 * An object rather than just the new value, so the name, the raw strings and a removal are available
 * without changing the callback's signature later. Only real changes are reported: writing the same
 * decoded value again does nothing. On the **first** report (from
 * [CustomElementAdapter.applyInitialAttributes] or the first setter call) there is no previous
 * report, so `oldValue == newValue` and `oldRaw == newRaw`. [newRaw] being `null` tells a removal
 * apart from a value that happens to decode to the default.
 */
class AttributeChange<T> internal constructor(
    /** Attribute name, e.g. `max-count`. */
    val name: String,
    /** Decoded value before the change; equal to [newValue] on the first report. */
    val oldValue: T,
    /** Decoded value after the change. */
    val newValue: T,
    /** Raw attribute value before the change, `null` when the attribute was absent. */
    val oldRaw: String?,
    /** Raw attribute value after the change, `null` when the attribute was removed. */
    val newRaw: String?,
) {
    /** `true` when the attribute was removed rather than written. */
    val isRemoval: Boolean get() = newRaw == null

    override fun toString(): String = "AttributeChange($name: $oldRaw -> $newRaw, $oldValue -> $newValue)"
}

/**
 * The object a delegated property is declared with: `var kind: String by attr(...)`.
 *
 * [provideDelegate] runs once per element instance, when the property is initialised. It creates the
 * delegate, works out the attribute name and registers it with the element, which is how
 * [CustomElementAdapter.applyInitialAttributes] later finds it.
 */
class AttributeProvider<T> internal constructor(
    private val name: String?,
    private val default: T?,
    private val codec: AttributeCodec<T>,
    private val onChange: (AttributeChange<T>) -> Unit,
) {

    operator fun provideDelegate(
        thisRef: CustomElementAdapter,
        property: KProperty<*>,
    ): ReadWriteProperty<CustomElementAdapter, T> {
        val delegate = AttributeDelegate(
            element = thisRef,
            name = name ?: property.name.toAttributeName(),
            default = default,
            codec = codec,
            onChange = onChange,
        )
        thisRef.registerAttributeDelegate(delegate)
        return delegate
    }
}

/**
 * Backs a property with an HTML attribute.
 *
 * ```kotlin
 * class KtjsBadge : CustomElementAdapter() {
 *     var href: String by attr { change -> anchor.href = change.newValue }
 *     var height: Int by attr("height", default = 20) { change -> image.height = change.newValue }
 * }
 * ```
 *
 * - **No duplicated state.** The getter reads the attribute and decodes it, the setter encodes and
 *   writes it, so the DOM is the only place the value lives. An attribute set from outside (markup,
 *   `setAttribute`, devtools) is visible to the getter immediately.
 * - **The name defaults to the property name** in kebab-case (`logoSrc` → `logo-src`); pass [name]
 *   for anything else. Native spellings are not special-cased: `tabIndex` needs
 *   `attr("tabindex")`, and a property named like an `HTMLElement` member (`title`, `id`, `hidden`)
 *   has to be named differently and mapped explicitly (`val titleText: String by attr("title")`).
 * - **Add `@JsName` when JavaScript should see the property.** Kotlin/JS compiles a delegated
 *   property to module-mangled accessor methods (`get_text_8f3k2_k$`) and *no* JS property; only
 *   `@JsName("titleText")` makes it emit real `get` / `set` accessors, so that
 *   `element.titleText = "x"` works from the page. Kotlin callers do not need it.
 * - **Values are converted by [codec]**, which defaults to the built-in one for [T]
 *   ([StringAttributeCodec], [BooleanAttributeCodec], [IntAttributeCodec], [DoubleAttributeCodec]).
 *   Pass a codec for any other type; see [enumAttributeCodec].
 * - **[default] is not written to the markup**; it is what the getter returns while the attribute is
 *   absent. Removing the attribute goes back to it.
 * - **[onChange] runs whenever the decoded value really changed** — from the setter, from outside, or
 *   from [CustomElementAdapter.applyInitialAttributes] during `connectedCallback`. Writing the same
 *   value again is not a change, and removing the attribute is one.
 *
 * Only *reflecting* properties belong here: `attr` writes the attribute, and an element that reads
 * its own attribute back (as these components do) is a reflecting property. `input.value` is not one
 * — it is internal state that outlives the attribute — so it stays an ordinary property.
 */
inline fun <reified T> attr(
    name: String? = null,
    default: T? = null,
    codec: AttributeCodec<T>? = null,
    noinline onChange: (AttributeChange<T>) -> Unit = {},
): AttributeProvider<T> = attributeProvider(name, default, codec ?: defaultAttributeCodec<T>(), onChange)

/** Constructs the provider outside the inline function; see [attr]. */
@PublishedApi
internal fun <T> attributeProvider(
    name: String?,
    default: T?,
    codec: AttributeCodec<T>,
    onChange: (AttributeChange<T>) -> Unit,
): AttributeProvider<T> = AttributeProvider(name, default, codec, onChange)

/**
 * The codec [attr] picks when the caller passes none.
 *
 * Kotlin/JS has no runtime generics, so the type is matched on the reified [T] instead of by
 * reflection on a value.
 */
@PublishedApi
internal inline fun <reified T> defaultAttributeCodec(): AttributeCodec<T> {
    val codec: AttributeCodec<*> = when (T::class) {
        String::class -> StringAttributeCodec
        Boolean::class -> BooleanAttributeCodec
        Int::class -> IntAttributeCodec
        Double::class -> DoubleAttributeCodec
        else -> error(
            "No built-in AttributeCodec for ${T::class.simpleName}; " +
                "pass one with attr(codec = …), or use enumAttributeCodec(values).",
        )
    }
    return unsafeCast(codec)
}

/**
 * The delegate behind one `attr` property.
 *
 * It keeps the last value it reported only to tell changes apart from repeated writes; the value a
 * getter returns always comes from the DOM.
 */
internal class AttributeDelegate<T>(
    private val element: CustomElementAdapter,
    private val name: String,
    private val default: T?,
    private val codec: AttributeCodec<T>,
    private val onChange: (AttributeChange<T>) -> Unit,
) : ReadWriteProperty<CustomElementAdapter, T> {

    private var lastRaw: String? = null
    private var lastValue: T? = null
    private var reported: Boolean = false
    private var notifying: Boolean = false

    override fun getValue(thisRef: CustomElementAdapter, property: KProperty<*>): T =
        valueOf(thisRef.getAttribute(name))

    override fun setValue(thisRef: CustomElementAdapter, property: KProperty<*>, value: T) {
        val encoded = codec.encode(value)
        if (encoded == null) thisRef.removeAttribute(name) else thisRef.setAttribute(name, encoded)
        sync()
    }

    /**
     * Reads the attribute again and reports it when the value differs from the last report.
     *
     * This is the only path that calls [onChange] — setter, attribute observer and
     * [CustomElementAdapter.applyInitialAttributes] all end up here — so the callback cannot be
     * invoked twice for one change, and nothing here writes the attribute back.
     */
    internal fun sync() {
        // A callback that writes its own property would otherwise re-enter in the middle of a report.
        if (notifying) return

        val raw = element.getAttribute(name)
        val value = valueOf(raw)
        if (reported && lastValue == value) {
            lastRaw = raw
            return
        }

        val change = AttributeChange(
            name = name,
            oldValue = if (reported) lastValue ?: value else value,
            newValue = value,
            oldRaw = if (reported) lastRaw else raw,
            newRaw = raw,
        )
        lastRaw = raw
        lastValue = value
        reported = true

        notifying = true
        try {
            onChange(change)
        } finally {
            notifying = false
        }
    }

    /** Writes [default] to a missing attribute; used by `applyInitialAttributes(writeDefaults = true)`. */
    internal fun writeDefault() {
        val value = default ?: return
        if (element.getAttribute(name) != null) return
        val encoded = codec.encode(value)
        if (encoded == null) element.removeAttribute(name) else element.setAttribute(name, encoded)
    }

    private fun valueOf(raw: String?): T = when (raw) {
        null -> default ?: codec.absent
        else -> codec.decode(raw) ?: default ?: codec.absent
    }
}

/**
 * `maxCount` → `max-count`: the attribute name a property gets when `attr` is not told one.
 *
 * A dash goes before an upper-case letter that starts a word — after a lower-case letter, or in a
 * run of capitals that is followed by a lower-case one (`XMLHttp` → `xml-http`) — and everything is
 * lower-cased. Digits and existing dashes are left alone.
 */
internal fun String.toAttributeName(): String {
    val name = StringBuilder(length + 4)
    for (index in indices) {
        val char = this[index]
        if (!char.isUpperCase()) {
            name.append(char)
            continue
        }

        val previous = getOrNull(index - 1)
        val next = getOrNull(index + 1)
        val startsWord = previous != null && (!previous.isUpperCase() || (next != null && next.isLowerCase()))
        if (startsWord) name.append('-')
        name.append(char.lowercaseChar())
    }
    return name.toString()
}
