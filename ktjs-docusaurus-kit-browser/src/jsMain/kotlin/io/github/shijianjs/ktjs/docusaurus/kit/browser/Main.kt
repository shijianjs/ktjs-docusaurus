package io.github.shijianjs.ktjs.docusaurus.kit.browser

/**
 * Bundle entry: registering on load is the whole job.
 *
 * The site loads the bundle only after hydration, so there is nothing to defer here; the tags React
 * has already rendered are upgraded the moment their companion defines itself.
 */
fun main() {
    registerKtjsElements()
}
