package io.github.shijianjs.ktjs.docusaurus.demo.elements

import web.dom.Element

/**
 * The declarative spellings of this site's landing page: tag names, attribute names, child tags.
 *
 * These belong to the site, not to the kit — the kit only knows about `setTitle`, `setFeatures` and
 * so on. Keeping the markup vocabulary here is what lets the kit stay a plain component library.
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

/** The tag's own text, trimmed: what MDX writes between the opening and closing tag. */
internal fun Element.label(): String = textContent.orEmpty().trim()

/** Descendants carrying [tag], in document order. */
internal fun Element.childElements(tag: String): List<Element> {
    val found = querySelectorAll(tag)
    return (0 until found.length).mapNotNull { found.item(it) as? Element }
}
