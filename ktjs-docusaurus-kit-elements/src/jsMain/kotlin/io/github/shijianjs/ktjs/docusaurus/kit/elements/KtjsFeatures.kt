package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter

import io.github.shijianjs.ktjs.docusaurus.kit.FeatureItem
import io.github.shijianjs.ktjs.docusaurus.kit.FeatureSection
import web.components.CustomElementReference
import web.dom.TagName

/**
 * `<ktjs-features section-title tinted>` with `<ktjs-feature icon title>details</ktjs-feature>`.
 *
 * The tag's body is the card's details; attributes and child tags are read once and handed to the
 * kit's [FeatureSection] setters.
 */
class KtjsFeatures : CustomElementAdapter() {
    override fun connectedCallback() {
        val section = this.section { FeatureSection(it, tinted = hasAttribute("tinted")) }

        getAttribute("section-title")?.let(section::setSectionTitle)

        if (getAttribute(HomeMarkup.READ) != null) return
        setAttribute(HomeMarkup.READ, "")

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
