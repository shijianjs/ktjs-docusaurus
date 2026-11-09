package example

import web.cssom.ElementCSSInlineStyle
import web.dom.HTMLOrSVGOrMathMLElement
import web.globals.GlobalEventHandlers
import web.html.ElementContentEditable

/**
 * The interface experiment without `Element`, which is an external *class* and therefore cannot be
 * part of an interface.
 *
 * Kept only until the compiler verdict is recorded; it is expected to still fail, because
 * `HTMLOrSVGOrMathMLElement` lists `Element` among its supertypes as well.
 */
external interface BridgeWithoutElement :
    ElementCSSInlineStyle,
    ElementContentEditable,
    GlobalEventHandlers,
    HTMLOrSVGOrMathMLElement
