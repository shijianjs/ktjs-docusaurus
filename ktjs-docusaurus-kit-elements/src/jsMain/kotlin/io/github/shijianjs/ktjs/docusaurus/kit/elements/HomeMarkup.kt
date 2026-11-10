package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import web.dom.Element

/**
 * The declarative spelling of the home sections: tag and attribute names, plus the helper that reads
 * the child tags.
 */
internal object HomeMarkup {
    const val HERO: String = "ktjs-hero"
    const val FEATURES: String = "ktjs-features"
    const val NEXT_STEPS: String = "ktjs-next-steps"

    const val ACTION: String = "ktjs-action"
    const val BADGE: String = "ktjs-badge"
    const val FEATURE: String = "ktjs-feature"
    const val STEP: String = "ktjs-step"
}

/** Descendants carrying [tag], in document order. */
internal fun CustomElementAdapter.childTags(tag: String): List<Element> {
    val found = querySelectorAll(tag)
    return (0 until found.length).mapNotNull { found.item(it) as? Element }
}
