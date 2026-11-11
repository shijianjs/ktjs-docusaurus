package io.github.shijianjs.ktjs.dom.css

import csstype.PropertiesBuilder
import web.cssom.BackgroundClip

/**
 * `-webkit-background-clip` — cssom models `background-clip` but not the prefixed property, and
 * `background-clip: text` is still prefixed-only in Safari, so a gradient-text rule needs both.
 *
 * The declaration goes into the rules object under its kebab-case name; `@emotion/serialize`
 * hyphenates camelCase keys and leaves a key that already starts with `-` alone, so this serializes
 * exactly as written:
 *
 * ```kotlin
 * cssSheet {
 *     titleClass {
 *         backgroundImage = linearGradient(120.deg, colorStop(accent), colorStop(accentBright))
 *         webkitBackgroundClip(BackgroundClip.text)
 *         backgroundClip = BackgroundClip.text
 *         color = Color.transparent
 *     }
 * }
 * ```
 *
 * Add one function per missing vendor property, in this file, with the value typed — a generic
 * `putRaw(name, value)` at call sites would put the strings back that `cssSheet` exists to remove.
 */
fun PropertiesBuilder.webkitBackgroundClip(value: BackgroundClip) {
    asDynamic()["-webkit-background-clip"] = value
}
