package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.iconify.IconifyIconElement
import io.github.shijianjs.ktjs.iconify.IconifyIconTagName
import web.components.CustomElementReference
import web.dom.TagName
import web.html.HTMLAnchorElement
import web.html.HTMLSpanElement
import web.html.HtmlTagName

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
        heading = HtmlTagName.span.createElement {
            classList.add("ktjs-next-card-title")
        }
        details = HtmlTagName.span.createElement {
            classList.add("ktjs-next-card-details")
        }
        arrow = IconifyIconTagName.iconifyIcon.createElement {
            classList.add("ktjs-next-card-arrow")
            setAttribute("icon", "lucide:arrow-right")
            setAttribute("aria-hidden", "true")
        }
        val body = HtmlTagName.span.createElement {
            classList.add("ktjs-next-card-body")
            appendChild(heading)
            appendChild(details)
        }
        anchor = HtmlTagName.a.createElement {
            classList.add("ktjs-next-card")
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
