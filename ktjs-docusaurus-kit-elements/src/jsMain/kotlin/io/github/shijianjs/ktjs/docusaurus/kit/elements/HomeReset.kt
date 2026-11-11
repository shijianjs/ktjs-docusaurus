package io.github.shijianjs.ktjs.docusaurus.kit.elements

import web.cssom.CSSStyleSheet

/**
 * The one stylesheet all seven elements share, adopted alongside each element's own sheet.
 *
 * Two things are common to every shadow tree and cannot be said per node:
 *
 * - `box-sizing` is not inherited, so the page's `* { box-sizing: border-box }` cannot reach into a
 *   shadow tree; without it every padded box falls back to `content-box` and layout thresholds move.
 * - `:host { display: block }` — a custom element is inline by default, and the host's own `style`
 *   cannot be touched from the constructor (assigning it adds an attribute, which makes
 *   `document.createElement("ktjs-…")` throw), so `display` has to come from a sheet.
 *
 * Everything else is owned by the element that paints it: inline where inline suffices, that
 * element's own sheet for `:hover` and friends.
 */
private const val RESET_CSS: String = """
*,
*::before,
*::after {
  box-sizing: border-box;
}

:host {
  display: block;
}
"""

internal val HOME_RESET: CSSStyleSheet = CSSStyleSheet().apply { replaceSync(RESET_CSS) }
