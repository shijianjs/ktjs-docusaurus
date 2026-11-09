package io.github.shijianjs.ktjs.docusaurus.kit

import web.dom.Element
import web.html.HTMLElement

/**
 * Reads a home-section custom element into the kit's sections.
 *
 * The declarative spelling — `<ktjs-hero title="…"><ktjs-action kind="primary" href="…">Get started</ktjs-action>`
 * — belongs to the kit, not to the site: every site that uses these sections would otherwise repeat
 * the same attribute names, child tags and cleanup. A site only supplies the element classes and the
 * tag names (see the demo's `HomeElements.kt`).
 *
 * Each `bind*` is idempotent: the section is created on the first call and kept in a map keyed by the
 * host, so re-connecting the element (StrictMode, client-side navigation) does not build a second one.
 * The declarative children are consumed once and removed, guarded by [READ_ATTRIBUTE].
 */
private val heroes = mutableMapOf<HTMLElement, HomeHero>()
private val featureSections = mutableMapOf<HTMLElement, FeatureSection>()
private val nextStepSections = mutableMapOf<HTMLElement, NextStepSection>()

/** Fills a `<ktjs-hero>` element from its attributes and `<ktjs-action>` / `<ktjs-badge>` children. */
fun bindHomeHero(host: HTMLElement): HomeHero {
    val component = heroes.getOrPut(host) { HomeHero(host) }

    host.getAttribute("logo-src")?.let(component::setLogo)
    component.setTitle(host.getAttribute("title").orEmpty())
    component.setTagline(host.getAttribute("tagline").orEmpty())

    if (host.getAttribute(READ_ATTRIBUTE) != null) return component
    host.setAttribute(READ_ATTRIBUTE, "")

    val actions = host.childElements(ACTION_TAG)
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
        host.childElements(BADGE_TAG).map { badge ->
            HeroBadge(
                href = badge.getAttribute("href").orEmpty(),
                src = badge.getAttribute("src").orEmpty(),
                alt = badge.getAttribute("alt").orEmpty(),
            )
        },
    )

    // The section owns the rendering now; the declarative children would show up twice.
    actions.forEach { it.remove() }
    host.childElements(BADGE_TAG).forEach { it.remove() }
    return component
}

/** Fills a `<ktjs-features>` element from its attributes and `<ktjs-feature>` children. */
fun bindFeatureSection(host: HTMLElement): FeatureSection {
    val component = featureSections.getOrPut(host) {
        FeatureSection(host, tinted = host.hasAttribute("tinted"))
    }

    host.getAttribute("section-title")?.let(component::setSectionTitle)

    if (host.getAttribute(READ_ATTRIBUTE) != null) return component
    host.setAttribute(READ_ATTRIBUTE, "")

    component.setFeatures(
        host.childElements(FEATURE_TAG).map { feature ->
            FeatureItem(
                icon = feature.getAttribute("icon").orEmpty(),
                title = feature.getAttribute("title").orEmpty(),
                details = feature.label(),
            )
        },
    )
    host.childElements(FEATURE_TAG).forEach { it.remove() }
    return component
}

/** Fills a `<ktjs-next-steps>` element from its attributes and `<ktjs-step>` children. */
fun bindNextStepSection(host: HTMLElement): NextStepSection {
    val component = nextStepSections.getOrPut(host) { NextStepSection(host) }

    host.getAttribute("section-title")?.let(component::setSectionTitle)

    if (host.getAttribute(READ_ATTRIBUTE) != null) return component
    host.setAttribute(READ_ATTRIBUTE, "")

    component.setSteps(
        host.childElements(STEP_TAG).map { step ->
            NextStepItem(
                href = step.getAttribute("href").orEmpty(),
                title = step.getAttribute("title").orEmpty(),
                details = step.label(),
            )
        },
    )
    host.childElements(STEP_TAG).forEach { it.remove() }
    return component
}

/** Marks a host whose declarative children were already consumed. */
private const val READ_ATTRIBUTE = "data-ktjs-read"

private const val ACTION_TAG = "ktjs-action"
private const val BADGE_TAG = "ktjs-badge"
private const val FEATURE_TAG = "ktjs-feature"
private const val STEP_TAG = "ktjs-step"

/** The tag's own text, trimmed: what MDX writes between the opening and closing tag. */
private fun Element.label(): String = textContent.orEmpty().trim()

/** Descendants carrying [tag], in document order. */
private fun Element.childElements(tag: String): List<Element> {
    val found = querySelectorAll(tag)
    return (0 until found.length).mapNotNull { found.item(it) as? Element }
}
