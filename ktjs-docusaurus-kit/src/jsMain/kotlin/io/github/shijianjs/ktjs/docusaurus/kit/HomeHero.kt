package io.github.shijianjs.ktjs.docusaurus.kit

import web.html.HTMLAnchorElement
import web.html.HTMLElement
import web.html.HTMLImageElement
import web.window.WindowTarget
import web.window._blank

/**
 * The landing hero: logo, title, tagline, the two call-to-action buttons and the badge row.
 *
 * Retained mode: the tree is built once in the constructor and appended to [container]; every
 * `set*` method mutates only the nodes it owns, so calling them before or after mounting is safe
 * and nothing is ever rebuilt.
 *
 * ```kotlin
 * val hero = HomeHero(mountPoint)
 * hero.setTitle("ktjs-docusaurus")
 * hero.setPrimaryAction(HeroLink("Get started", useBaseUrl("/docs/intro")))
 * ```
 */
class HomeHero(container: HTMLElement) : HomeElementBinding {
    private val root = create("section", "ktjs-hero")
    private val inner = create("div", "ktjs-hero-inner")
    private val stage = create("span", "ktjs-hero-logo-stage")
    // The <h1> spells the name out, so the logo is decorative.
    private val logo = create("img", "ktjs-hero-logo").unsafeCastElement<HTMLImageElement>().apply {
        alt = ""
        setAttribute("width", "480")
        setAttribute("height", "480")
    }
    private val title = create("h1", "ktjs-hero-title")
    private val tagline = create("p", "ktjs-hero-tagline")
    private val actions = create("div", "ktjs-hero-actions")
    private val primary = create("a", "ktjs-button", "ktjs-button-primary").unsafeCastElement<HTMLAnchorElement>()
    private val primaryLabel = create("span")
    private val secondary = create("a", "ktjs-button", "ktjs-button-secondary").unsafeCastElement<HTMLAnchorElement>()
    private val secondaryIcon = iconElement("simple-icons:github", "ktjs-button-icon")
    private val secondaryLabel = create("span")
    private val badges = create("div", "ktjs-hero-badges")
    private val badgeLinks = mutableListOf<BadgeLink>()

    init {
        ensureHomeStyles()
        stage.appendChild(logo)
        primary.appendChild(primaryLabel)
        secondary.append(secondaryIcon, secondaryLabel)
        actions.append(primary, secondary)
        inner.append(stage, title, tagline, actions, badges)
        root.appendChild(inner)
        container.appendChild(root)
    }

    /** Points the logo at an image; the path must already carry the base URL prefix. */
    fun setLogo(src: String) {
        logo.src = src
    }

    /** Sets the page title rendered in the hero heading. */
    fun setTitle(text: String) {
        title.text(text)
    }

    /** Sets the line under the title. */
    fun setTagline(text: String) {
        tagline.text(text)
    }

    /** The main call to action: an internal link, so it stays in the same tab. */
    fun setPrimaryAction(link: HeroLink) {
        fillLink(primary, primaryLabel, link, external = false)
    }

    /** The secondary call to action: always external, always carries its glyph. */
    fun setSecondaryAction(action: HeroAction) {
        fillLink(secondary, secondaryLabel, HeroLink(action.label, action.href), external = true)
        secondaryIcon.setAttribute("icon", action.icon)
    }

    /**
     * Replaces the badge row.
     *
     * Grows or shrinks the row to the new length and mutates the links already there: the row is a
     * fixed part of the hero, only its entries change.
     */
    fun setBadges(items: List<HeroBadge>) {
        while (badgeLinks.size > items.size) {
            badgeLinks.removeAt(badgeLinks.lastIndex).let { link ->
                link.root.remove()
            }
        }
        while (badgeLinks.size < items.size) {
            val link = BadgeLink()
            badgeLinks += link
            badges.appendChild(link.root)
        }
        items.forEachIndexed { index, badge ->
            badgeLinks[index].apply(badge)
        }
    }

    private fun fillLink(
        anchor: HTMLAnchorElement,
        label: HTMLElement,
        link: HeroLink,
        external: Boolean,
    ) {
        anchor.href = link.href
        label.text(link.label)
        if (external) {
            anchor.target = WindowTarget._blank
            anchor.rel = "noopener noreferrer"
        } else {
            anchor.removeAttribute("target")
            anchor.removeAttribute("rel")
        }
    }

    /**
     * Reads a `<ktjs-hero>` host into this section.
     *
     * The attribute names and child tags are the declarative spelling of the setters above, so they
     * belong to this class; `bindHomeSection` only decides which instance handles the host. Attributes are
     * re-read on every call, the child tags are consumed once (guarded by [HomeMarkup.READ]).
     */
    override fun fillFrom(host: HTMLElement) {
        host.getAttribute(HomeMarkup.LOGO_SRC)?.let(::setLogo)
        setTitle(host.getAttribute(HomeMarkup.TITLE).orEmpty())
        setTagline(host.getAttribute(HomeMarkup.TAGLINE).orEmpty())

        if (host.getAttribute(HomeMarkup.READ) != null) return
        host.setAttribute(HomeMarkup.READ, "")

        val actions = host.childElements(HomeMarkup.ACTION)
        actions.firstOrNull { it.getAttribute("kind") == "primary" }?.let { action ->
            setPrimaryAction(HeroLink(action.label(), action.getAttribute("href").orEmpty()))
        }
        actions.firstOrNull { it.getAttribute("kind") == "secondary" }?.let { action ->
            setSecondaryAction(
                HeroAction(
                    label = action.label(),
                    href = action.getAttribute("href").orEmpty(),
                    icon = action.getAttribute("icon").orEmpty(),
                ),
            )
        }
        setBadges(
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

    /** One badge: an external link wrapping the shields.io image. Built once. */
    private class BadgeLink {
        val root = create("a", "ktjs-badge").unsafeCastElement<HTMLAnchorElement>()
        private val image = create("img", "ktjs-badge-image").unsafeCastElement<HTMLImageElement>()

        init {
            root.target = WindowTarget._blank
            root.rel = "noopener noreferrer"
            root.appendChild(image)
        }

        fun apply(badge: HeroBadge) {
            root.href = badge.href
            image.src = badge.src
            image.alt = badge.alt
        }
    }
}
