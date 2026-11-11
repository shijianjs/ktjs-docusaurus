package io.github.shijianjs.ktjs.dom.css

import csstype.RuleBuilder
import web.cssom.AtRules

/**
 * `@supports <condition> { … }` — the one at-rule the wrappers do not wrap.
 *
 * `csstype.RuleBuilder` already has `@media(query) { … }` (plus `@container`, `@font-face`,
 * `@view-transition`), so those are used directly; `web.cssom.AtRules` types every at-rule keyword
 * (`media`, `supports`, `layer`, `page`, …) as an `AtRules` constant. Only the condition has no type
 * — cssom models media queries (`MediaQuery`, `MediaType`, `maxWidth`, …) but no supports condition —
 * so this helper takes the condition as text and takes the keyword from [AtRules.supports]:
 *
 * ```kotlin
 * cssSheet {
 *     `@media`(MediaType.screen and maxWidth(576.px)) {
 *         actionsClass { flexDirection = FlexDirection.column }
 *     }
 *     supports("(background-clip: text)") {
 *         titleClass { backgroundClip = BackgroundClip.text }
 *     }
 * }
 * ```
 */
fun <T : Any> RuleBuilder<T>.supports(
    condition: String,
    block: T.() -> Unit,
) {
    "${AtRules.supports} $condition"(block)
}
