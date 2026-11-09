package io.github.shijianjs.ktjs.docusaurus.kit

import web.dom.Element
import web.dom.document
import web.html.HTMLElement

/**
 * Turns the declarative `<ktjs-…>` tags a page writes in MDX into the kit's retained-mode sections.
 *
 * MDX is enough for a landing page: the structure reads like the markup it produces, and the only
 * dynamic part — building the section trees — happens here. This is the same shape the duckfn docs
 * kit uses, without needing a custom-element class per section: the tags are unknown HTML elements
 * until this function runs, and they are upgraded in place.
 *
 * The tags are read once (attributes and child elements), the section is built into the same host and
 * the declarative children are dropped. Re-running is safe: upgraded hosts carry
 * [UPGRADED_ATTRIBUTE] and are skipped, which matters because Docusaurus calls the client module
 * after every route change.
 *
 * ```mdx
 * <ktjs-hero logo-src="img/favicon.svg" title="my-project" tagline="Documentation sites as Kotlin/JS projects">
 *   <ktjs-action kind="primary" href="/docs/intro">Get started</ktjs-action>
 *   <ktjs-action kind="secondary" href="https://github.com/…" icon="simple-icons:github">GitHub</ktjs-action>
 * </ktjs-hero>
 *
 * <ktjs-features section-title="Why this project" tinted>
 *   <ktjs-feature icon="lucide:package" title="No npm project to maintain">
 *     Versions live in one package.json for Dependabot.
 *   </ktjs-feature>
 * </ktjs-features>
 * ```
 */
fun upgradeHomeElements() {
    val body = document.body ?: return
    body.childElements("ktjs-hero").forEach(::upgradeHero)
    body.childElements("ktjs-features").forEach(::upgradeFeatures)
    body.childElements("ktjs-next-steps").forEach(::upgradeNextSteps)
}

/** Marks a host this function already upgraded. */
internal const val UPGRADED_ATTRIBUTE = "data-ktjs-upgraded"

private fun upgradeHero(host: Element) {
    val element = host.asUpgradable() ?: return

    val actions = element.childElements("ktjs-action")
    val primary = actions.firstOrNull { it.getAttribute("kind") == "primary" }
    val secondary = actions.firstOrNull { it.getAttribute("kind") == "secondary" }
    val badges = element.childElements("ktjs-badge").map { badge ->
        HeroBadge(
            href = badge.getAttribute("href").orEmpty(),
            src = badge.getAttribute("src").orEmpty(),
            alt = badge.getAttribute("alt").orEmpty(),
        )
    }

    element.replaceChildren()
    HomeHero(element).apply {
        element.getAttribute("logo-src")?.takeIf { it.isNotBlank() }?.let(::setLogo)
        setTitle(element.getAttribute("title").orEmpty())
        setTagline(element.getAttribute("tagline").orEmpty())
        primary?.let { action ->
            setPrimaryAction(HeroLink(action.label(), action.getAttribute("href").orEmpty()))
        }
        secondary?.let { action ->
            setSecondaryAction(
                HeroAction(
                    label = action.label(),
                    href = action.getAttribute("href").orEmpty(),
                    icon = action.getAttribute("icon").orEmpty(),
                ),
            )
        }
        setBadges(badges)
    }
}

private fun upgradeFeatures(host: Element) {
    val element = host.asUpgradable() ?: return

    val features = element.childElements("ktjs-feature").map { feature ->
        FeatureItem(
            icon = feature.getAttribute("icon").orEmpty(),
            title = feature.getAttribute("title").orEmpty(),
            details = feature.label(),
        )
    }

    element.replaceChildren()
    FeatureSection(element, tinted = element.hasAttribute("tinted")).apply {
        element.getAttribute("section-title")?.let(::setSectionTitle)
        setFeatures(features)
    }
}

private fun upgradeNextSteps(host: Element) {
    val element = host.asUpgradable() ?: return

    val steps = element.childElements("ktjs-step").map { step ->
        NextStepItem(
            href = step.getAttribute("href").orEmpty(),
            title = step.getAttribute("title").orEmpty(),
            details = step.label(),
        )
    }

    element.replaceChildren()
    NextStepSection(element).apply {
        element.getAttribute("section-title")?.let(::setSectionTitle)
        setSteps(steps)
    }
}

/**
 * Marks [this] as owned by the kit and returns it as an [HTMLElement], or `null` when it was already
 * upgraded.
 */
private fun Element.asUpgradable(): HTMLElement? {
    if (hasAttribute(UPGRADED_ATTRIBUTE)) return null
    val element = this as? HTMLElement ?: return null
    setAttribute(UPGRADED_ATTRIBUTE, "")
    // The tag is an unknown element, i.e. `display: inline`; the section it receives is a block.
    element.style.display = "block"
    return element
}

/** The tag's own text, trimmed: what MDX writes between the opening and closing tag. */
private fun Element.label(): String = textContent.orEmpty().trim()

/** Direct and nested descendants carrying [tag], in document order. */
private fun Element.childElements(tag: String): List<Element> {
    val found = querySelectorAll(tag)
    return (0 until found.length).mapNotNull { found.item(it) as? Element }
}
