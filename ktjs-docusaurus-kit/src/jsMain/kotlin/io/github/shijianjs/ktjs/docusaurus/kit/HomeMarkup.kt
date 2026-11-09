package io.github.shijianjs.ktjs.docusaurus.kit

import web.dom.Element

/**
 * The declarative spellings shared by the home sections: attribute names, child tag names and the two
 * small helpers that read them.
 *
 * Kept internal — a site writes these tags in MDX, it never references the constants.
 */
internal object HomeMarkup {
    /** Marks a host whose declarative children were already consumed. */
    const val READ: String = "data-ktjs-read"

    /** The landing-page tags the kit registers. */
    const val HERO: String = "ktjs-hero"
    const val FEATURES: String = "ktjs-features"
    const val NEXT_STEPS: String = "ktjs-next-steps"

    const val ACTION: String = "ktjs-action"
    const val BADGE: String = "ktjs-badge"
    const val FEATURE: String = "ktjs-feature"
    const val STEP: String = "ktjs-step"

    const val LOGO_SRC: String = "logo-src"
    const val TITLE: String = "title"
    const val TAGLINE: String = "tagline"
    const val SECTION_TITLE: String = "section-title"
    const val TINTED: String = "tinted"
}

/** The tag's own text, trimmed: what MDX writes between the opening and closing tag. */
internal fun Element.label(): String = textContent.orEmpty().trim()

/** Descendants carrying [tag], in document order. */
internal fun Element.childElements(tag: String): List<Element> {
    val found = querySelectorAll(tag)
    return (0 until found.length).mapNotNull { found.item(it) as? Element }
}
