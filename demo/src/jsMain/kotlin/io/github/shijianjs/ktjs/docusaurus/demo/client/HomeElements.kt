package io.github.shijianjs.ktjs.docusaurus.demo.client

import io.github.shijianjs.ktjs.docusaurus.kit.bindFeatureSection
import io.github.shijianjs.ktjs.docusaurus.kit.bindHomeHero
import io.github.shijianjs.ktjs.docusaurus.kit.bindNextStepSection

/**
 * The site's landing-page tags.
 *
 * This file is only the wiring: three tag names and the kit binding each one uses. Everything about
 * the markup — which attributes to read (`title`, `tagline`, `section-title`, `tinted`), which child
 * tags to consume (`ktjs-action`, `ktjs-badge`, `ktjs-feature`, `ktjs-step`), how to reuse the section
 * on a second connect — lives in the kit, in `HomeBindings.kt`.
 *
 * Registration goes through [defineElement] rather than a Kotlin element class; see the note there.
 */
fun registerKtjsElements() {
    defineElement("ktjs-hero") { host -> bindHomeHero(host) }
    defineElement("ktjs-features") { host -> bindFeatureSection(host) }
    defineElement("ktjs-next-steps") { host -> bindNextStepSection(host) }
}
