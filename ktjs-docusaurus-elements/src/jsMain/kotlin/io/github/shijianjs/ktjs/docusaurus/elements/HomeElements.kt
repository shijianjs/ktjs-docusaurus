package io.github.shijianjs.ktjs.docusaurus.elements

import io.github.shijianjs.ktjs.docusaurus.kit.FeatureItem
import io.github.shijianjs.ktjs.docusaurus.kit.FeatureSection
import io.github.shijianjs.ktjs.docusaurus.kit.HeroAction
import io.github.shijianjs.ktjs.docusaurus.kit.HeroBadge
import io.github.shijianjs.ktjs.docusaurus.kit.HeroLink
import io.github.shijianjs.ktjs.docusaurus.kit.HomeHero
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepItem
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepSection
import js.reflect.unsafeCast
import kotlin.js.JsExport
import kotlin.js.JsName
import web.components.CustomElementReference
import web.components.customElements
import web.components.define
import web.dom.TagName
import web.html.HTMLElement

/**
 * The three landing-page tags, using the official custom-element API:
 * `class X : HTMLElement()` (via [DomAdapter]) + `CustomElementReference` + `customElements.define`.
 *
 * The only deliberate deviation is the lifecycle hook: `CustomElement.connectedCallback` is a `val`,
 * which Kotlin compiles to a getter the browser never calls, so it is spelled as a
 * `@JsName("connectedCallback") fun` instead.
 *
 * Every DOM access on the host goes through [DomAdapter]'s `…Super` methods. The child tags are plain
 * DOM elements created from MDX, so those need no special handling.
 *
 * The section each element renders into is stored in one field on the element itself: it is released
 * with the element and reused when `connectedCallback` runs again after a client-side navigation.
 */
@JsExport
fun registerHomeElements() {
    defineIfMissing(KtjsHero.NAME, KtjsHero.Companion)
    defineIfMissing(KtjsFeatures.NAME, KtjsFeatures.Companion)
    defineIfMissing(KtjsNextSteps.NAME, KtjsNextSteps.Companion)
}

private fun <T : HTMLElement> defineIfMissing(name: TagName<T>, reference: CustomElementReference<T>) {
    if (customElements.get(name) == null) {
        customElements.define(name, reference)
    }
}

private external interface HomeHostState {
    var ktjsSection: Any?
}

private fun <T : Any> HTMLElement.section(factory: (HTMLElement) -> T): T {
    val state = unsafeCast<HomeHostState>(this)
    state.ktjsSection?.let { return unsafeCast<T>(it) }
    return factory(this).also { state.ktjsSection = it }
}

/** `<ktjs-hero logo-src title tagline>` with `<ktjs-action>` / `<ktjs-badge>` children. */
class KtjsHero : DomAdapter() {
    @JsName("connectedCallback")
    fun onConnected() {
        val hero = section(::HomeHero)

        getAttributeSuper("logo-src")?.let(hero::setLogo)
        hero.setTitle(getAttributeSuper("title").orEmpty())
        hero.setTagline(getAttributeSuper("tagline").orEmpty())

        if (getAttributeSuper(HomeMarkup.READ) != null) return
        setAttributeSuper(HomeMarkup.READ, "")

        val actions = childTags(HomeMarkup.ACTION)
        actions.firstOrNull { it.getAttribute("kind") == "primary" }?.let { action ->
            hero.setPrimaryAction(HeroLink(action.label(), action.getAttribute("href").orEmpty()))
        }
        actions.firstOrNull { it.getAttribute("kind") == "secondary" }?.let { action ->
            hero.setSecondaryAction(
                HeroAction(
                    label = action.label(),
                    href = action.getAttribute("href").orEmpty(),
                    icon = action.getAttribute("icon").orEmpty(),
                ),
            )
        }
        hero.setBadges(
            childTags(HomeMarkup.BADGE).map { badge ->
                HeroBadge(
                    href = badge.getAttribute("href").orEmpty(),
                    src = badge.getAttribute("src").orEmpty(),
                    alt = badge.getAttribute("alt").orEmpty(),
                )
            },
        )

        actions.forEach { it.remove() }
        childTags(HomeMarkup.BADGE).forEach { it.remove() }
    }

    companion object : CustomElementReference<KtjsHero>(KtjsHero::class.js) {
        val NAME: TagName<KtjsHero> = TagName(HomeMarkup.HERO)
    }
}

/** `<ktjs-features section-title tinted>` with `<ktjs-feature icon title>details</ktjs-feature>`. */
class KtjsFeatures : DomAdapter() {
    @JsName("connectedCallback")
    fun onConnected() {
        val section = this.section { FeatureSection(it, tinted = hasAttributeSuper("tinted")) }

        getAttributeSuper("section-title")?.let(section::setSectionTitle)

        if (getAttributeSuper(HomeMarkup.READ) != null) return
        setAttributeSuper(HomeMarkup.READ, "")

        val features = childTags(HomeMarkup.FEATURE)
        section.setFeatures(
            features.map { feature ->
                FeatureItem(
                    icon = feature.getAttribute("icon").orEmpty(),
                    title = feature.getAttribute("title").orEmpty(),
                    details = feature.label(),
                )
            },
        )
        features.forEach { it.remove() }
    }

    companion object : CustomElementReference<KtjsFeatures>(KtjsFeatures::class.js) {
        val NAME: TagName<KtjsFeatures> = TagName(HomeMarkup.FEATURES)
    }
}

/** `<ktjs-next-steps section-title>` with `<ktjs-step href title>details</ktjs-step>`. */
class KtjsNextSteps : DomAdapter() {
    @JsName("connectedCallback")
    fun onConnected() {
        val section = this.section(::NextStepSection)

        getAttributeSuper("section-title")?.let(section::setSectionTitle)

        if (getAttributeSuper(HomeMarkup.READ) != null) return
        setAttributeSuper(HomeMarkup.READ, "")

        val steps = childTags(HomeMarkup.STEP)
        section.setSteps(
            steps.map { step ->
                NextStepItem(
                    href = step.getAttribute("href").orEmpty(),
                    title = step.getAttribute("title").orEmpty(),
                    details = step.label(),
                )
            },
        )
        steps.forEach { it.remove() }
    }

    companion object : CustomElementReference<KtjsNextSteps>(KtjsNextSteps::class.js) {
        val NAME: TagName<KtjsNextSteps> = TagName(HomeMarkup.NEXT_STEPS)
    }
}
