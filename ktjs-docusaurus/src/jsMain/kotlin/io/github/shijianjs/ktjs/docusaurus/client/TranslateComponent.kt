@file:JsModule("@docusaurus/Translate")

package io.github.shijianjs.ktjs.docusaurus.client

import react.FC
import react.PropsWithChildren
import kotlin.js.JsName

/**
 * The `<Translate>` component: renders [TranslateOptions.message] for the current locale.
 *
 * Prefer this over calling `translate()` when the string sits inside JSX; the function exists for
 * places that need a plain string (a DOM section's setter, an attribute).
 *
 * Upstream source: `@docusaurus/core` (`Translate`).
 */
external interface TranslateProps : PropsWithChildren, TranslateOptions

/** The translate component itself. */
@JsName("default")
external val Translate: FC<TranslateProps>
