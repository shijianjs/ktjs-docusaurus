@file:JsModule("stylis")

package example

import js.array.ReadonlyArray
import kotlin.js.JsAny

/**
 * The other half of emotion's text pipeline, declared only as far as this probe uses it.
 *
 * stylis 4 is a parser + middleware chain, not a "compile(selector)" function: `compile` parses CSS
 * text into an element tree, `serialize` walks it, and the `middleware([prefixer, stringify])` chain
 * does what emotion's cache relies on — resolve nested selectors against their parent
 * (`:hover{…}` → `.parent:hover{…}`), normalize values and add vendor prefixes.
 */
external fun compile(css: String): ReadonlyArray<JsAny?>

external val stringify: StylisMiddleware

external val prefixer: StylisMiddleware

external fun middleware(collection: ReadonlyArray<StylisMiddleware>): JsAny?

external fun serialize(children: JsAny?, callback: JsAny?): String

typealias StylisMiddleware = (
    element: JsAny?,
    index: Int,
    children: JsAny?,
    callback: JsAny?,
) -> String
