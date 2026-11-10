package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.iconify.IconifyIconElement
import io.github.shijianjs.ktjs.iconify.IconifyIconTagName
import web.components.CustomElementReference
import web.cssom.ClassName
import web.dom.TagName
import web.html.HTMLAnchorElement
import web.html.HTMLSpanElement

/**
 * `<ktjs-step href="…" title="…">details</ktjs-step>` — one link card in a [`KtjsNextSteps`] grid.
 *
 * The whole card is the anchor, so it renders itself into a shadow root and the parent only lays it
 * out.
 */
class KtjsStep : CustomElementAdapter() {
    private val anchor: HTMLAnchorElement
    private val heading: HTMLSpanElement
    private val details: HTMLSpanElement
    private val arrow: IconifyIconElement

    init {
        val shadow = attachHomeShadow()
        heading = TagName<HTMLSpanElement>("span").createElement {
            className = ClassName("ktjs-next-card-title")
        }
        details = TagName<HTMLSpanElement>("span").createElement {
            className = ClassName("ktjs-next-card-details")
        }
        arrow = IconifyIconTagName.iconifyIcon.createElement {
            className = ClassName("ktjs-next-card-arrow")
            setAttribute("icon", "lucide:arrow-right")
            setAttribute("aria-hidden", "true")
        }
        val body = TagName<HTMLSpanElement>("span").createElement {
            className = ClassName("ktjs-next-card-body")
            appendChild(heading)
            appendChild(details)
        }
        anchor = TagName<HTMLAnchorElement>("a").createElement {
            className = ClassName("ktjs-next-card")
            appendChild(body)
            appendChild(arrow)
        }
        shadow.appendChild(anchor)
    }

    override fun connectedCallback() {
        anchor.href = getAttribute("href").orEmpty()
        heading.textContent = getAttribute("title").orEmpty()
        details.textContent = textContent?.trim().orEmpty()
    }

    companion object : CustomElementReference<KtjsStep>(KtjsStep::class.js) {
        val NAME: TagName<KtjsStep> = TagName(HomeMarkup.STEP)
    }
}
