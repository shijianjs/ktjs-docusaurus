package io.github.shijianjs.ktjs.docusaurus.kit.elements

import web.cssom.CSSStyleSheet

/**
 * The one stylesheet the elements share, and the only one they adopt.
 *
 * `box-sizing` has to apply to every node in every shadow tree and cannot be expressed as an inline
 * style, so it is genuinely common: one parsed sheet serves all seven elements. Everything else is
 * owned by the element that paints it — inline where inline suffices, a local `<style>` for `:hover`
 * and friends.
 */
private const val RESET_CSS: String = """
*,
*::before,
*::after {
  box-sizing: border-box;
}
"""

internal val HOME_RESET: CSSStyleSheet = CSSStyleSheet().apply { replaceSync(RESET_CSS) }
