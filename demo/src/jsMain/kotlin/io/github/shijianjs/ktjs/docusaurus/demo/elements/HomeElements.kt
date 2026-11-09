package io.github.shijianjs.ktjs.docusaurus.demo.elements

import io.github.shijianjs.ktjs.docusaurus.kit.FeatureItem
import io.github.shijianjs.ktjs.docusaurus.kit.FeatureSection
import io.github.shijianjs.ktjs.docusaurus.kit.HeroAction
import io.github.shijianjs.ktjs.docusaurus.kit.HeroBadge
import io.github.shijianjs.ktjs.docusaurus.kit.HeroLink
import io.github.shijianjs.ktjs.docusaurus.kit.HomeHero
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepItem
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepSection
import js.reflect.unsafeCast
import web.html.HTMLElement

/**
 * The site's landing-page tags, as a layer **separate from the kit**.
 *
 * The kit is a plain component library: `HomeHero(mountPoint)` + `setTitle` / `setFeatures` / … It
 * knows nothing about MDX, tag names, attributes or custom elements. This package owns the declarative
 * spelling, reads it once per element and drives the kit's setters.
 *
 * Each element owns the section it renders into — one field on the element, released with it — so a
 * second `connectedCallback` (StrictMode, client-side navigation) reuses the same instance.
 */
fun registerHomeElements() {
    defineElement(HomeMarkup.HERO, ::onHeroConnected)
    defineElement(HomeMarkup.FEATURES, ::onFeaturesConnected)
    defineElement(HomeMarkup.NEXT_STEPS, ::onNextStepsConnected)
}

private external interface HomeHostState {
    var ktjsSection: Any?
}

private fun <T : Any> HTMLElement.section(factory: (HTMLElement) -> T): T {
    val state = unsafeCast<HomeHostState>(this)
    state.ktjsSection?.let { return unsafeCast<T>(it) }
    return factory(this).also { state.ktjsSection = it }
}

private fun onHeroConnected(host: HTMLElement) {
    val hero = host.section(::HomeHero)

    host.getAttribute("logo-src")?.let(hero::setLogo)
    hero.setTitle(host.getAttribute("title").orEmpty())
    hero.setTagline(host.getAttribute("tagline").orEmpty())

    if (host.getAttribute(HomeMarkup.READ) != null) return
    host.setAttribute(HomeMarkup.READ, "")

    val actions = host.childElements(HomeMarkup.ACTION)
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
        host.childElements(HomeMarkup.BADGE).map { badge ->
            HeroBadge(
                href = badge.getAttribute("href").orEmpty(),
                src = badge.getAttribute("src").orEmpty(),
                alt = badge.getAttribute("alt").orEmpty(),
            )
        },
    )

    // The section owns the rendering now; the declarative children would show up twice.
    actions.forEach { it.remove() }
    host.childElements(HomeMarkup.BADGE).forEach { it.remove() }
}

private fun onFeaturesConnected(host: HTMLElement) {
    val section = host.section {
        FeatureSection(it, tinted = it.hasAttribute("tinted"))
    }

    host.getAttribute("section-title")?.let(section::setSectionTitle)

    if (host.getAttribute(HomeMarkup.READ) != null) return
    host.setAttribute(HomeMarkup.READ, "")

    val features = host.childElements(HomeMarkup.FEATURE)
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

private fun onNextStepsConnected(host: HTMLElement) {
    val section = host.section(::NextStepSection)

    host.getAttribute("section-title")?.let(section::setSectionTitle)

    if (host.getAttribute(HomeMarkup.READ) != null) return
    host.setAttribute(HomeMarkup.READ, "")

    val steps = host.childElements(HomeMarkup.STEP)
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
