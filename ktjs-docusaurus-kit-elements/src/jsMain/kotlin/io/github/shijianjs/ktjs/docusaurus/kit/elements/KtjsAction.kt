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
import web.window.WindowTarget
import web.window._blank

/**
 * `<ktjs-action kind="primary|secondary" href="…" icon="…">Label</ktjs-action>`
 *
 * A call-to-action pill. `secondary` actions open in a new tab and carry an Iconify glyph; the glyph
 * is dropped when `icon` is empty.
 */
class KtjsAction : CustomElementAdapter() {
    private val anchor: HTMLAnchorElement
    private val label: HTMLSpanElement
    private val icon: IconifyIconElement

    init {
        val shadow = attachHomeShadow()
        label = TagName<HTMLSpanElement>("span").createElement()
        icon = IconifyIconTagName.iconifyIcon.createElement {
            className = ClassName("ktjs-button-icon")
            setAttribute("aria-hidden", "true")
        }
        anchor = TagName<HTMLAnchorElement>("a").createElement {
            append(icon, label)
        }
        shadow.appendChild(anchor)
    }

    override fun connectedCallback() {
        val secondary = getAttribute("kind") == "secondary"
        anchor.className = ClassName(
            if (secondary) "ktjs-button ktjs-button-secondary" else "ktjs-button ktjs-button-primary",
        )
        anchor.href = getAttribute("href").orEmpty()
        if (secondary) {
            anchor.target = WindowTarget._blank
            anchor.rel = "noopener noreferrer"
        }
        label.textContent = textContent?.trim().orEmpty()
        val iconName = getAttribute("icon")
        if (iconName.isNullOrBlank()) {
            icon.remove()
        } else {
            icon.setAttribute("icon", iconName)
        }
    }

    companion object : CustomElementReference<KtjsAction>(KtjsAction::class.js) {
        val NAME: TagName<KtjsAction> = TagName(HomeMarkup.ACTION)
    }
}
