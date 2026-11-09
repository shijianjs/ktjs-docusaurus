package io.github.shijianjs.ktjs.docusaurus.kit

import web.html.HTMLAnchorElement
import web.html.HTMLElement

/**
 * A titled section with a grid of cards.
 *
 * Both the feature grid and the "where to go next" row are this shape; they differ only in what a
 * card is. The grid is built once and `setItems` grows or shrinks the list, reusing the cards that
 * are already there — the section is never cleared and rebuilt.
 */
abstract class HomeSection internal constructor(
    container: HTMLElement,
    tinted: Boolean,
) : HomeElementBinding {
    private val root = create(
        "section",
        if (tinted) "ktjs-section ktjs-section-tint" else "ktjs-section",
    )
    private val inner = create("div", "ktjs-section-inner")
    private val heading = create("h2", "ktjs-section-title")
    private val grid = create("div", gridClass)
    private val cards = mutableListOf<Card>()

    /** Class name of the card grid. */
    protected abstract val gridClass: String

    /** Creates one card. */
    protected abstract fun createCard(): Card

    init {
        ensureHomeStyles()
        inner.append(heading, grid)
        root.appendChild(inner)
        container.appendChild(root)
    }

    /** Sets the section heading. */
    fun setSectionTitle(text: String) {
        heading.text(text)
    }

    /**
     * Reads a host element into this section: the shared `section-title` on every call, the
     * section-specific child tags exactly once (guarded by [HomeMarkup.READ]).
     */
    override fun fillFrom(host: HTMLElement) {
        host.getAttribute(HomeMarkup.SECTION_TITLE)?.let(::setSectionTitle)

        if (host.getAttribute(HomeMarkup.READ) != null) return
        host.setAttribute(HomeMarkup.READ, "")
        readDeclarativeChildren(host)
    }

    /** Consumes this section's own child tags and removes them from the host; runs once. */
    protected abstract fun readDeclarativeChildren(host: HTMLElement)

    /**
     * The common half of both sections: reconcile the card list, then hand each card its item.
     */
    protected fun <T> setItems(items: List<T>, apply: (Card, T) -> Unit) {
        while (cards.size > items.size) {
            cards.removeAt(cards.lastIndex).let { card -> card.root.remove() }
        }
        while (cards.size < items.size) {
            createCard().also { card ->
                cards += card
                grid.appendChild(card.root)
            }
        }
        items.forEachIndexed { index, item -> apply(cards[index], item) }
    }

    /** A card: the root node plus the DOM its own `set*` / `apply` methods mutate. */
    abstract class Card internal constructor(val root: HTMLElement)
}

/**
 * The "why this project" grid of feature cards.
 *
 * ```kotlin
 * val features = FeatureSection(mountPoint)
 * features.setFeatures(listOf(FeatureItem("lucide:sparkles", "Title", "Details")))
 * ```
 */
class FeatureSection(
    container: HTMLElement,
    tinted: Boolean = false,
) : HomeSection(container, tinted) {

    override val gridClass: String = "ktjs-feature-grid"

    override fun createCard(): Card = FeatureCard()

    /** Replaces the cards. */
    fun setFeatures(items: List<FeatureItem>) {
        setItems(items) { card, item -> (card as FeatureCard).apply(item) }
    }

    override fun readDeclarativeChildren(host: HTMLElement) {
        val features = host.childElements(HomeMarkup.FEATURE)
        setFeatures(
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

    private class FeatureCard : Card(create("article", "ktjs-feature-card")) {
        private val icon = iconElement("lucide:sparkles", "ktjs-feature-icon")
        private val title = create("h3", "ktjs-feature-title")
        private val details = create("p", "ktjs-feature-details")

        init {
            // The chip only wraps the icon and is never touched again.
            root.appendChild(create("span", "ktjs-feature-icon-chip").also { it.appendChild(icon) })
            root.append(title, details)
        }

        fun apply(item: FeatureItem) {
            icon.setAttribute("icon", item.icon)
            title.text(item.title)
            details.text(item.details)
        }
    }
}

/**
 * The "where to go next" row of link cards; the whole card is the anchor.
 *
 * ```kotlin
 * val next = NextStepSection(mountPoint, tinted = true)
 * next.setSteps(listOf(NextStepItem(useBaseUrl("/docs/intro"), "Introduction", "…")))
 * ```
 */
class NextStepSection(
    container: HTMLElement,
    tinted: Boolean = false,
) : HomeSection(container, tinted) {

    override val gridClass: String = "ktjs-next-grid"

    override fun createCard(): Card = NextStepCard()

    /** Replaces the cards. */
    fun setSteps(items: List<NextStepItem>) {
        setItems(items) { card, item -> (card as NextStepCard).apply(item) }
    }

    override fun readDeclarativeChildren(host: HTMLElement) {
        val steps = host.childElements(HomeMarkup.STEP)
        setSteps(
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

    private class NextStepCard :
        Card(create("a", "ktjs-next-card").unsafeCastElement<HTMLAnchorElement>()) {
        private val body = create("span", "ktjs-next-card-body")
        private val title = create("span", "ktjs-next-card-title")
        private val details = create("span", "ktjs-next-card-details")
        private val arrow = iconElement("lucide:arrow-right", "ktjs-next-card-arrow")

        init {
            body.append(title, details)
            root.append(body, arrow)
        }

        fun apply(item: NextStepItem) {
            (root as HTMLAnchorElement).href = item.href
            title.text(item.title)
            details.text(item.details)
        }
    }
}
