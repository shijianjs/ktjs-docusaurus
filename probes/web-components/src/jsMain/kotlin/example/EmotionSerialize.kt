@file:JsModule("@emotion/serialize")

package example

import js.array.ReadonlyArray
import kotlin.js.JsAny

/**
 * The corner of `@emotion/serialize` this probe needs, declared as-is — a rules object in, CSS text
 * out. The package exports more, but nothing else is called here.
 *
 * `serializeStyles` takes the **argument list as an array**, the way emotion's `css` tagged template
 * passes its `arguments`/template strings: `serializeStyles([styles])`. Passing the object directly
 * reads `args[0]` of the object and serializes nothing.
 */
external fun serializeStyles(args: ReadonlyArray<JsAny?>): SerializedStyles

external interface SerializedStyles {
    val name: String

    /** The serialized CSS. */
    val styles: String

    val map: String?
}
