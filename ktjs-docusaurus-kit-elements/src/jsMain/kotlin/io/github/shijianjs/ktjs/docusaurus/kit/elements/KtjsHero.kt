package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.docusaurus.kit.HeroAction
import io.github.shijianjs.ktjs.docusaurus.kit.HeroBadge
import io.github.shijianjs.ktjs.docusaurus.kit.HeroLink
import io.github.shijianjs.ktjs.docusaurus.kit.HomeHero
import web.components.CustomElementReference
import web.dom.TagName

/**
 * `<ktjs-hero logo-src title tagline>` with `<ktjs-action>` / `<ktjs-badge>` children.
 *
 * Attributes and child tags are read once (guarded by `data-ktjs-read`) and handed to the kit's
 * [HomeHero] setters. The section instance is kept on the element by [section].
 */
class KtjsHero : CustomElementAdapter() {
    override fun connectedCallback() {
        val hero = section(::HomeHero)

        getAttribute("logo-src")?.let(hero::setLogo)
        hero.setTitle(getAttribute("title").orEmpty())
        hero.setTagline(getAttribute("tagline").orEmpty())

        if (getAttribute(HomeMarkup.READ) != null) return
        setAttribute(HomeMarkup.READ, "")

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

        // The section owns the rendering now; the declarative children would show up twice.
        actions.forEach { it.remove() }
        childTags(HomeMarkup.BADGE).forEach { it.remove() }
    }

    companion object : CustomElementReference<KtjsHero>(KtjsHero::class.js) {
        val NAME: TagName<KtjsHero> = TagName(HomeMarkup.HERO)
    }
}
