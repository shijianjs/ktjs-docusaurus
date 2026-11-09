package io.github.shijianjs.ktjs.docusaurus.demo.client

import io.github.shijianjs.ktjs.docusaurus.kit.FeatureItem
import io.github.shijianjs.ktjs.docusaurus.kit.FeatureSection
import io.github.shijianjs.ktjs.docusaurus.kit.HeroAction
import io.github.shijianjs.ktjs.docusaurus.kit.HeroBadge
import io.github.shijianjs.ktjs.docusaurus.kit.HeroLink
import io.github.shijianjs.ktjs.docusaurus.kit.HomeHero
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepItem
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepSection
import web.components.CustomElement
import web.components.CustomElementReference
import web.components.customElements
import web.components.define
import web.dom.Element
import web.dom.TagName
import web.html.HTMLElement

/**
 * The landing-page tags.
 *
 * The markup lives in MDX — `<ktjs-hero title="…"><ktjs-action …>` — and each class is the element's
 * `connectedCallback`: it reads the attributes and child tags once, hands them to the retained-mode
 * section and drops the declarative children. The section is kept per instance, so re-connecting the
 * same element does not build a second one.
 *
 * The shape is the one from `kotlin-wrappers/examples/web-components`: `class X : HTMLElement(),
 * CustomElement` plus a `CustomElementReference` companion carrying the tag name. Kotlin only emits a
 * real `class X extends HTMLElement` when the js target compiles at ES2015 or later — see the
 * `compilerOptions { target = "es2015" }` block in this module's build script. Without it the class
 * becomes `function X() { HTMLElement.call(this) }` and the browser throws "Please use the 'new'
 * operator" the first time an element is constructed.
 */
class KtjsHero : HTMLElement(), CustomElement {

    private var hero: HomeHero? = null

    override val connectedCallback: () -> Unit = {
        val element = this
        val component = hero ?: HomeHero(element).also { hero = it }

        element.getAttribute("logo-src")?.let(component::setLogo)
        component.setTitle(element.getAttribute("title").orEmpty())
        component.setTagline(element.getAttribute("tagline").orEmpty())

        if (element.getAttribute(TAG_READ) == null) {
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
    }

    companion object : CustomElementReference<KtjsHero>(KtjsHero::class.js) {
        /** The tag the MDX markup uses. */
        val NAME: TagName<KtjsHero> = TagName("ktjs-hero")
    }
}

/** `<ktjs-features section-title="…" tinted>` with `<ktjs-feature icon title>body</ktjs-feature>`. */
class KtjsFeatures : HTMLElement(), CustomElement {

    private var section: FeatureSection? = null

    override val connectedCallback: () -> Unit = {
        val element = this
        val component = section
            ?: FeatureSection(element, tinted = element.hasAttribute("tinted")).also { section = it }

        element.getAttribute("section-title")?.let(component::setSectionTitle)

        if (element.getAttribute(TAG_READ) == null) {
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
    }

    companion object : CustomElementReference<KtjsFeatures>(KtjsFeatures::class.js) {
        /** The tag the MDX markup uses. */
        val NAME: TagName<KtjsFeatures> = TagName("ktjs-features")
    }
}

/** `<ktjs-next-steps section-title="…">` with `<ktjs-step href title>body</ktjs-step>`. */
class KtjsNextSteps : HTMLElement(), CustomElement {

    private var section: NextStepSection? = null

    override val connectedCallback: () -> Unit = {
        val element = this
        val component = section ?: NextStepSection(element).also { section = it }

        element.getAttribute("section-title")?.let(component::setSectionTitle)

        if (element.getAttribute(TAG_READ) == null) {
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
    }

    companion object : CustomElementReference<KtjsNextSteps>(KtjsNextSteps::class.js) {
        /** The tag the MDX markup uses. */
        val NAME: TagName<KtjsNextSteps> = TagName("ktjs-next-steps")
    }
}

/** Defines `<ktjs-hero>`, `<ktjs-features>` and `<ktjs-next-steps>`; safe to call more than once. */
fun registerKtjsElements() {
    if (customElements.get(KtjsHero.NAME) == null) {
        customElements.define(KtjsHero.NAME, KtjsHero.Companion)
    }
    if (customElements.get(KtjsFeatures.NAME) == null) {
        customElements.define(KtjsFeatures.NAME, KtjsFeatures.Companion)
    }
    if (customElements.get(KtjsNextSteps.NAME) == null) {
        customElements.define(KtjsNextSteps.NAME, KtjsNextSteps.Companion)
    }
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
