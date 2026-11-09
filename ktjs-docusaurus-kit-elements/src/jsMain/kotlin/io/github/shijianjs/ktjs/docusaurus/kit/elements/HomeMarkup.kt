package io.github.shijianjs.ktjs.docusaurus.kit.elements

import web.dom.Element

/**
 * The declarative spelling of the home sections: tag and attribute names, plus the two small helpers
 * that read them. Shared by the three element classes in this package; not part of the kit's API.
 */
internal object HomeMarkup {
    const val READ: String = "data-ktjs-read"

    const val HERO: String = "ktjs-hero"
    const val FEATURES: String = "ktjs-features"
    const val NEXT_STEPS: String = "ktjs-next-steps"

    const val ACTION: String = "ktjs-action"
    const val BADGE: String = "ktjs-badge"
    const val FEATURE: String = "ktjs-feature"
    const val STEP: String = "ktjs-step"
}

/** Descendants carrying [tag], in document order. Uses the adapter's safe `super` forwarding. */
internal fun CustomElementAdapter.childTags(tag: String): List<Element> {
    val found = querySelectorAllSuper(tag)
    return (0 until found.length).mapNotNull { found.item(it) as? Element }
}

/** The tag's own text, trimmed: what MDX writes between the opening and closing tag. */
internal fun Element.label(): String = textContent.orEmpty().trim()
