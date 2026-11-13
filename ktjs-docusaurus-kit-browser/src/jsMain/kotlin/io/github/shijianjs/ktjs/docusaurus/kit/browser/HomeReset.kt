package io.github.shijianjs.ktjs.docusaurus.kit.browser

import io.github.shijianjs.ktjs.dom.style.cssSheet
import web.cssom.BoxSizing
import web.cssom.CSSStyleSheet
import web.cssom.Display

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
internal val HOME_RESET: CSSStyleSheet = cssSheet {
    "*" { boxSizing = BoxSizing.borderBox }
    "*::before" { boxSizing = BoxSizing.borderBox }
    "*::after" { boxSizing = BoxSizing.borderBox }
    ":host" { display = Display.block }
}
