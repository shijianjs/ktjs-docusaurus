package io.github.shijianjs.ktjs.docusaurus.kit.bundle

import io.github.shijianjs.ktjs.docusaurus.kit.elements.registerHomeElements

/**
 * Bundle entry: registering on load is the whole job.
 *
 * The site imports the bundle only after hydration, so there is nothing to defer here; the tags
 * React has already rendered are upgraded by `defineIfMissing`.
 */
fun main() {
    registerHomeElements()
}
