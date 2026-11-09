package io.github.shijianjs.ktjs.docusaurus.elements

import js.array.ReadonlyArray
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
 * A complete adapter for the DOM methods Kotlin/JS shadows on a class extending `HTMLElement`.
 *
 * The compiler emits one self-recursive bridge for every member that is declared with
 * `= definedExternally` on an external interface (`ParentNode`, `ChildNode`, `Animatable`,
 * `HTMLOrSVGOrMathMLElement`, `EventTargetLike`, …):
 *
 * ```js
 * querySelectorAll(selectors) { return this.querySelectorAll(selectors); }
 * ```
 *
 * This adapter covers the **whole** list seen in the development link output — `dispatchEvent`,
 * `animate` (all four overloads), `getAnimations`, `after`/`before`/`replaceWith`/`append`/`prepend`/
 * `replaceChildren` (Node and String varargs), `moveBefore`, `querySelector`, `querySelectorAll`,
 * `blur`, `focus`, `remove` — with a differently named `…Super` method that forwards to
 * `super.foo(...)`. At ES2015 that is a real ES `super` call: lookup starts at `HTMLElement.prototype`
 * and never reaches the bridge.
 *
 * `remove()` is also overridden: that JS name is free, so the normal spelling works; the compiler then
 * emits `remove() { return this.remove_xxx$() }` → `remove_xxx$() { super.remove() }`.
 *
 * `getAttribute` / `setAttribute` / `hasAttribute` / `textContent` are deliberately **absent**: they
 * are abstract members of the external `Element` class, no bridge is generated for them, and calling
 * them on `this` already reaches the native implementation. Consumers use those directly.
 */
open class DomAdapter : HTMLElement() {

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
