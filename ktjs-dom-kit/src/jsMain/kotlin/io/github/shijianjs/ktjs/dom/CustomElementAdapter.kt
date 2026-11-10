package io.github.shijianjs.ktjs.dom

import js.array.ReadonlyArray
import kotlin.js.JsAny
import kotlin.js.JsName
import web.animations.Animation
import web.animations.GetAnimationsOptions
import web.animations.Keyframe
import web.animations.KeyframeAnimationOptions
import web.animations.PropertyIndexedKeyframes
import web.dom.Element
import web.dom.FocusOptions
import web.dom.Node
import web.dom.NodeList
import web.events.Event
import web.html.HTMLElement

/**
 * Base class for a custom element written in Kotlin.
 *
 * Besides extending the official [HTMLElement], it does two things a site should not have to repeat:
 *
 * 1. Declares **all** of `web.components.CustomElement`'s lifecycle hooks as overridable methods under
 *    their native names: `connectedCallback`, `connectedMoveCallback`, `disconnectedCallback`,
 *    `adoptedCallback`, `attributeChangedCallback`. The official interface spells these as `val`s,
 *    which Kotlin compiles to getters the browser never calls, so the interface is deliberately not
 *    implemented here. Element classes just `override fun connectedCallback()` and friends.
 *
 *    `attributeChangedCallback` only fires for attributes listed in the constructor's static
 *    `observedAttributes`; declaring that is up to the element class.
 *
 * 2. Provides safe entry points for every DOM method Kotlin/JS shadows on such a class. The compiler
 *    emits one self-recursive bridge for each member declared with `= definedExternally` on an
 *    external interface (`ParentNode`, `ChildNode`, `Animatable`, `HTMLOrSVGOrMathMLElement`,
 *    `EventTargetLike`, …):
 *
 *    ```js
 *    querySelectorAll(selectors) { return this.querySelectorAll(selectors); }
 *    ```
 *
 *    The `…Super` methods below forward to `super.foo(...)`; at ES2015 that is a real ES `super`
 *    call, so lookup starts at `HTMLElement.prototype` and never reaches the bridge. Where the stable
 *    JS name is free (`remove`, `dispatchEvent`, `blur`, `moveBefore`) the method is overridden too,
 *    so the normal spelling works as well.
 *
 * `getAttribute` / `setAttribute` / `hasAttribute` / `textContent` are deliberately absent: they are
 * abstract members of the external `Element` class, no bridge is generated for them.
 */
open class CustomElementAdapter : HTMLElement() {

    @JsName("connectedCallback")
    open fun connectedCallback() {}

    @JsName("connectedMoveCallback")
    open fun connectedMoveCallback() {}

    @JsName("disconnectedCallback")
    open fun disconnectedCallback() {}

    @JsName("adoptedCallback")
    open fun adoptedCallback() {}

    @JsName("attributeChangedCallback")
    open fun attributeChangedCallback(name: String, oldValue: JsAny?, newValue: JsAny?) {}

    // EventTargetLike

    fun dispatchEventSuper(event: Event): Boolean = super.dispatchEvent(event)

    // Animatable

    fun animateSuper(keyframes: ReadonlyArray<Keyframe>?, options: Double): Animation =
        super.animate(keyframes, options)

    fun animateSuper(keyframes: ReadonlyArray<Keyframe>?, options: KeyframeAnimationOptions): Animation =
        super.animate(keyframes, options)

    fun animateSuper(keyframes: PropertyIndexedKeyframes?, options: Double): Animation =
        super.animate(keyframes, options)

    fun animateSuper(keyframes: PropertyIndexedKeyframes?, options: KeyframeAnimationOptions): Animation =
        super.animate(keyframes, options)

    fun getAnimationsSuper(options: GetAnimationsOptions): ReadonlyArray<Animation> =
        super.getAnimations(options)

    // ChildNode — the Node and String vararg overloads get distinct names, since JS methods cannot
    // overload by signature.

    fun afterNodesSuper(vararg nodes: Node) {
        super.after(*nodes)
    }

    fun afterTextSuper(vararg nodes: String) {
        super.after(*nodes)
    }

    fun beforeNodesSuper(vararg nodes: Node) {
        super.before(*nodes)
    }

    fun beforeTextSuper(vararg nodes: String) {
        super.before(*nodes)
    }

    fun replaceWithNodesSuper(vararg nodes: Node) {
        super.replaceWith(*nodes)
    }

    fun replaceWithTextSuper(vararg nodes: String) {
        super.replaceWith(*nodes)
    }

    override fun remove() {
        super.remove()
    }

    // ParentNode

    fun appendNodesSuper(vararg nodes: Node) {
        super.append(*nodes)
    }

    fun appendTextSuper(vararg nodes: String) {
        super.append(*nodes)
    }

    fun prependNodesSuper(vararg nodes: Node) {
        super.prepend(*nodes)
    }

    fun prependTextSuper(vararg nodes: String) {
        super.prepend(*nodes)
    }

    fun moveBeforeSuper(node: Node, child: Node?) {
        super.moveBefore(node, child)
    }

    fun replaceChildrenNodesSuper(vararg nodes: Node) {
        super.replaceChildren(*nodes)
    }

    fun replaceChildrenTextSuper(vararg nodes: String) {
        super.replaceChildren(*nodes)
    }

    fun replaceChildrenSuper() {
        super.replaceChildren()
    }

    fun querySelectorSuper(selectors: String): Element? = super.querySelector(selectors)

    fun querySelectorAllSuper(selectors: String): NodeList<Element> = super.querySelectorAll(selectors)

    // HTMLOrSVGOrMathMLElement

    fun blurSuper() {
        super.blur()
    }

    fun focusSuper(options: FocusOptions) {
        super.focus(options)
    }

    // Where the stable JS name is still free, override as well so the normal spelling works too.
    // Not possible for vararg members (`after` / `before` / `append` / `prepend` / `replaceWith` /
    // `replaceChildren`), the query methods, or external functions with `= definedExternally` default
    // parameters (`animate`, `focus`, `getAnimations`): Kotlin either reports a JS name clash with
    // the compiler-generated bridge or "Overriding 'external' function with optional parameters".
    // Those all have `…Super` counterparts above.

    override fun dispatchEvent(event: Event): Boolean = super.dispatchEvent(event)

    override fun blur() {
        super.blur()
    }

    override fun moveBefore(node: Node, child: Node?) {
        super.moveBefore(node, child)
    }
}
