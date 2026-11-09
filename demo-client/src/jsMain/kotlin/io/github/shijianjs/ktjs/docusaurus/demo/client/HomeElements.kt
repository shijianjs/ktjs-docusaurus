package io.github.shijianjs.ktjs.docusaurus.demo.client

import io.github.shijianjs.ktjs.docusaurus.kit.FeatureItem
import io.github.shijianjs.ktjs.docusaurus.kit.FeatureSection
import io.github.shijianjs.ktjs.docusaurus.kit.HeroAction
import io.github.shijianjs.ktjs.docusaurus.kit.HeroBadge
import io.github.shijianjs.ktjs.docusaurus.kit.HeroLink
import io.github.shijianjs.ktjs.docusaurus.kit.HomeHero
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepItem
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepSection
import web.dom.Element
import web.html.HTMLElement

/**
 * The landing-page tags.
 *
 * The markup lives in MDX — `<ktjs-hero title="…"><ktjs-action …>` — and each function below is the
 * element's `connectedCallback`: it reads the attributes and child tags once, hands them to the
 * retained-mode section and drops the declarative children. The section is kept per element, so a
 * client-side navigation that re-connects the same element does not build a second one.
 *
 * Registration goes through [defineElement], which is where the `class extends HTMLElement` syntax
 * lives — see the note there for why it cannot be a Kotlin class.
 */
private val heroes = mutableMapOf<HTMLElement, HomeHero>()
private val featureSections = mutableMapOf<HTMLElement, FeatureSection>()
private val nextStepSections = mutableMapOf<HTMLElement, NextStepSection>()

/** Defines `<ktjs-hero>`, `<ktjs-features>` and `<ktjs-next-steps>`; safe to call more than once. */
fun registerKtjsElements() {
    defineElement("ktjs-hero", ::onHeroConnected)
    defineElement("ktjs-features", ::onFeaturesConnected)
    defineElement("ktjs-next-steps", ::onNextStepsConnected)
}

private fun onHeroConnected(element: HTMLElement) {
    val component = heroes.getOrPut(element) { HomeHero(element) }

    element.getAttribute("logo-src")?.let(component::setLogo)
    component.setTitle(element.getAttribute("title").orEmpty())
    component.setTagline(element.getAttribute("tagline").orEmpty())

    if (element.getAttribute(TAG_READ) != null) return
    element.setAttribute(TAG_READ, "")

    val actions = element.childElements("ktjs-action")
    actions.firstOrNull { it.getAttribute("kind") == "primary" }?.let { action ->
        component.setPrimaryAction(HeroLink(action.label(), action.getAttribute("href").orEmpty()))
    }
    actions.firstOrNull { it.getAttribute("kind") == "secondary" }?.let { action ->
        component.setSecondaryAction(
            HeroAction(
                label = action.label(),
                href = action.getAttribute("href").orEmpty(),
                icon = action.getAttribute("icon").orEmpty(),
            ),
        )
    }
    component.setBadges(
        element.childElements("ktjs-badge").map { badge ->
            HeroBadge(
                href = badge.getAttribute("href").orEmpty(),
                src = badge.getAttribute("src").orEmpty(),
                alt = badge.getAttribute("alt").orEmpty(),
            )
        },
    )

    // The section owns the rendering now; the declarative children would show up twice.
    actions.forEach { it.remove() }
    element.childElements("ktjs-badge").forEach { it.remove() }
}

private fun onFeaturesConnected(element: HTMLElement) {
    val component = featureSections.getOrPut(element) {
        FeatureSection(element, tinted = element.hasAttribute("tinted"))
    }

    element.getAttribute("section-title")?.let(component::setSectionTitle)

    if (element.getAttribute(TAG_READ) != null) return
    element.setAttribute(TAG_READ, "")

    component.setFeatures(
        element.childElements("ktjs-feature").map { feature ->
            FeatureItem(
                icon = feature.getAttribute("icon").orEmpty(),
                title = feature.getAttribute("title").orEmpty(),
                details = feature.label(),
            )
        },
    )
    element.childElements("ktjs-feature").forEach { it.remove() }
}

private fun onNextStepsConnected(element: HTMLElement) {
    val component = nextStepSections.getOrPut(element) { NextStepSection(element) }

    element.getAttribute("section-title")?.let(component::setSectionTitle)

    if (element.getAttribute(TAG_READ) != null) return
    element.setAttribute(TAG_READ, "")

    component.setSteps(
        element.childElements("ktjs-step").map { step ->
            NextStepItem(
                href = step.getAttribute("href").orEmpty(),
                title = step.getAttribute("title").orEmpty(),
                details = step.label(),
            )
        },
    )
    element.childElements("ktjs-step").forEach { it.remove() }
}

/** Marks an element whose child tags were already consumed. */
private const val TAG_READ = "data-ktjs-read"

/** The tag's own text, trimmed: what MDX writes between the opening and closing tag. */
private fun Element.label(): String = textContent.orEmpty().trim()

/** Descendants carrying [tag], in document order. */
private fun Element.childElements(tag: String): List<Element> {
    val found = querySelectorAll(tag)
    return (0 until found.length).mapNotNull { found.item(it) as? Element }
}
