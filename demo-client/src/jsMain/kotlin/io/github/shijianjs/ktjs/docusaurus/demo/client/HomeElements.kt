package io.github.shijianjs.ktjs.docusaurus.demo.client

import io.github.shijianjs.ktjs.docusaurus.kit.FeatureItem
import io.github.shijianjs.ktjs.docusaurus.kit.FeatureSection
import io.github.shijianjs.ktjs.docusaurus.kit.HeroAction
import io.github.shijianjs.ktjs.docusaurus.kit.HeroBadge
import io.github.shijianjs.ktjs.docusaurus.kit.HeroLink
import io.github.shijianjs.ktjs.docusaurus.kit.HomeHero
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepItem
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepSection
import js.reflect.JsClass
import js.reflect.unsafeCast
import kotlin.js.JsExport
import web.components.customElements
import web.dom.Element
import web.html.HTMLElement

/**
 * The landing-page tags, as custom elements.
 *
 * The markup lives in MDX — `<ktjs-hero title="…"><ktjs-action …>` — and each class below upgrades
 * itself the first time it connects, reads its attributes and child tags once, hands them to the
 * retained-mode section and drops the declarative children.
 *
 * They live in this browser-only module rather than in the kit because `class X : HTMLElement()`
 * reads `HTMLElement` when the module loads: inside the kit they would be reachable from the
 * configuration's bundle, which Node loads, and the build would fail before it starts.
 *
 * `connectedCallback` may run more than once (a client-side navigation re-mounts the page), so the
 * section is built once and only the attributes are re-applied afterwards.
 */
@JsExport
class KtjsHero : HTMLElement() {

    private var hero: HomeHero? = null

    /** Reads the element's own attributes and child tags into the section. */
    fun connectedCallback() {
        val component = hero ?: HomeHero(this).also { hero = it }

        getAttribute("logo-src")?.let(component::setLogo)
        component.setTitle(getAttribute("title").orEmpty())
        component.setTagline(getAttribute("tagline").orEmpty())

        if (getAttribute(TAG_READ) == null) {
            setAttribute(TAG_READ, "")

            val actions = childElements("ktjs-action")
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
                childElements("ktjs-badge").map { badge ->
                    HeroBadge(
                        href = badge.getAttribute("href").orEmpty(),
                        src = badge.getAttribute("src").orEmpty(),
                        alt = badge.getAttribute("alt").orEmpty(),
                    )
                },
            )

            // The section owns the rendering now; the declarative children would show up twice.
            actions.forEach { it.remove() }
            childElements("ktjs-badge").forEach { it.remove() }
        }
    }
}

/** `<ktjs-features section-title="…" tinted>` with `<ktjs-feature icon title>body</ktjs-feature>`. */
@JsExport
class KtjsFeatures : HTMLElement() {

    private var section: FeatureSection? = null

    /** Reads the element's own attributes and child tags into the section. */
    fun connectedCallback() {
        val component = section
            ?: FeatureSection(this, tinted = hasAttribute("tinted")).also { section = it }

        getAttribute("section-title")?.let(component::setSectionTitle)

        if (getAttribute(TAG_READ) == null) {
            setAttribute(TAG_READ, "")
            component.setFeatures(
                childElements("ktjs-feature").map { feature ->
                    FeatureItem(
                        icon = feature.getAttribute("icon").orEmpty(),
                        title = feature.getAttribute("title").orEmpty(),
                        details = feature.label(),
                    )
                },
            )
            childElements("ktjs-feature").forEach { it.remove() }
        }
    }
}

/** `<ktjs-next-steps section-title="…">` with `<ktjs-step href title>body</ktjs-step>`. */
@JsExport
class KtjsNextSteps : HTMLElement() {

    private var section: NextStepSection? = null

    /** Reads the element's own attributes and child tags into the section. */
    fun connectedCallback() {
        val component = section ?: NextStepSection(this).also { section = it }

        getAttribute("section-title")?.let(component::setSectionTitle)

        if (getAttribute(TAG_READ) == null) {
            setAttribute(TAG_READ, "")
            component.setSteps(
                childElements("ktjs-step").map { step ->
                    NextStepItem(
                        href = step.getAttribute("href").orEmpty(),
                        title = step.getAttribute("title").orEmpty(),
                        details = step.label(),
                    )
                },
            )
            childElements("ktjs-step").forEach { it.remove() }
        }
    }
}

/** Defines the landing-page tags; the browser upgrades the MDX markup either way. */
fun registerKtjsElements() {
    if (Registration.done) return
    Registration.done = true

    val registry = customElements.unsafeCast<CustomElementRegistryLike>()
    registry.define("ktjs-hero", KtjsHero::class.js)
    registry.define("ktjs-features", KtjsFeatures::class.js)
    registry.define("ktjs-next-steps", KtjsNextSteps::class.js)
}

/**
 * The `CustomElementRegistry` call this file needs, with a plain `String` name.
 *
 * The typed overload takes the generated `TagName` / `CustomElementConstructor` wrappers; for three
 * fixed tags the string form is the same call without the ceremony.
 */
internal external interface CustomElementRegistryLike {
    fun define(name: String, constructor: JsClass<*>)
}

private object Registration {
    var done: Boolean = false
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
