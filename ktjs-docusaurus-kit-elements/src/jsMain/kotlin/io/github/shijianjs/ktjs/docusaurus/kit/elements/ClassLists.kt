package io.github.shijianjs.ktjs.docusaurus.kit.elements

import web.cssom.ClassName
import web.dom.DOMTokenList

/**
 * `add` / `remove` with plain string tokens.
 *
 * `Element.classList` is typed as `DOMTokenList<ClassName>`; these forwards keep the call sites free
 * of `ClassName("…")` noise while still going through the typed API.
 */
internal fun DOMTokenList<ClassName>.add(vararg tokens: String) {
    for (token in tokens) add(ClassName(token))
}

internal fun DOMTokenList<ClassName>.remove(vararg tokens: String) {
    for (token in tokens) remove(ClassName(token))
}
