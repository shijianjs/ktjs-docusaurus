package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.iconify.IconifyIconElement
import io.github.shijianjs.ktjs.iconify.IconifyIconTagName
import web.components.CustomElementReference
import web.cssom.ClassName
import web.dom.TagName
import web.html.HTMLHeadingElement
import web.html.HTMLParagraphElement
import web.html.HTMLSpanElement
import web.html.HTMLElement

/**
 * `<ktjs-feature icon="lucide:…" title="…">details</ktjs-feature>` — one card in a
 * [`KtjsFeatures`] grid.
 *
 * Renders itself into a shadow root, so the parent only lays the cards out.
 */
class KtjsFeature : CustomElementAdapter() {
    private val icon: IconifyIconElement
    private val heading: HTMLHeadingElement
    private val details: HTMLParagraphElement

    init {
        val shadow = attachHomeShadow()
        icon = IconifyIconTagName.iconifyIcon.createElement {
            className = ClassName("ktjs-feature-icon")
            setAttribute("aria-hidden", "true")
        }
        heading = TagName<HTMLHeadingElement>("h3").createElement {
            className = ClassName("ktjs-feature-title")
        }
        details = TagName<HTMLParagraphElement>("p").createElement {
            className = ClassName("ktjs-feature-details")
        }
        val card = TagName<HTMLElement>("article").createElement {
            className = ClassName("ktjs-feature-card")
            appendChild(
                TagName<HTMLSpanElement>("span").createElement {
                    className = ClassName("ktjs-feature-icon-chip")
                    appendChild(icon)
                },
            )
            appendChild(heading)
            appendChild(details)
        }
        shadow.appendChild(card)
    }

    override fun connectedCallback() {
        icon.setAttribute("icon", getAttribute("icon").orEmpty())
        heading.textContent = getAttribute("title").orEmpty()
        details.textContent = textContent?.trim().orEmpty()
    }

    companion object : CustomElementReference<KtjsFeature>(KtjsFeature::class.js) {
        val NAME: TagName<KtjsFeature> = TagName(HomeMarkup.FEATURE)
    }
}
