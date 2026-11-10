package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter

import io.github.shijianjs.ktjs.docusaurus.kit.NextStepItem
import io.github.shijianjs.ktjs.docusaurus.kit.NextStepSection
import web.components.CustomElementReference
import web.dom.TagName

/**
 * `<ktjs-next-steps section-title>` with `<ktjs-step href title>details</ktjs-step>`.
 *
 * The tag's body is the card's details; attributes and child tags are read once and handed to the
 * kit's [NextStepSection] setters.
 */
class KtjsNextSteps : CustomElementAdapter() {
    override fun connectedCallback() {
        val section = this.section(::NextStepSection)

        getAttribute("section-title")?.let(section::setSectionTitle)

        if (getAttribute(HomeMarkup.READ) != null) return
        setAttribute(HomeMarkup.READ, "")

        val steps = childTags(HomeMarkup.STEP)
        section.setSteps(
            steps.map { step ->
                NextStepItem(
                    href = step.getAttribute("href").orEmpty(),
                    title = step.getAttribute("title").orEmpty(),
                    details = step.label(),
                )
            },
        )
        steps.forEach { it.remove() }
    }

    companion object : CustomElementReference<KtjsNextSteps>(KtjsNextSteps::class.js) {
        val NAME: TagName<KtjsNextSteps> = TagName(HomeMarkup.NEXT_STEPS)
    }
}
