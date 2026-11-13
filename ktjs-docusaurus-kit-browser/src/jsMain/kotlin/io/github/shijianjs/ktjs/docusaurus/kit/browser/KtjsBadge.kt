package io.github.shijianjs.ktjs.docusaurus.kit.browser

import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import io.github.shijianjs.ktjs.dom.webcomponent.attachStyledShadow
import io.github.shijianjs.ktjs.dom.attribute.attr
import io.github.shijianjs.ktjs.dom.element.createElement
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import kotlin.js.JsName
import web.components.CustomElementReference
import web.cssom.Auto
import web.cssom.Display
import web.cssom.px
import web.dom.TagName
import web.html.HTMLAnchorElement
import web.html.HTMLImageElement
import web.html.HtmlTagName
import web.window.WindowTarget
import web.window._blank

/**
 * `<ktjs-badge href="…" src="…" alt="…">` — a shields.io-style image inside an external link.
 *
 * One delegate per attribute: the wrapper link and the image are updated from the callbacks, so the
 * badge follows its attributes after it is connected too.
 *
 * No stylesheet: an image in a row is exactly what inline styles can say.
 */
class KtjsBadge : CustomElementAdapter() {
    private val anchor: HTMLAnchorElement
    private val image: HTMLImageElement

    /** `href`: where the badge links to; blank leaves the anchor without `href`. */
    @JsName("href")
    var href: String by attr { change ->
        if (change.newValue.isBlank()) {
            anchor.removeAttribute("href")
        } else {
            anchor.href = change.newValue
        }
    }

    /** `src`: the badge image; blank drops the `src` instead of pointing the image at the page. */
    @JsName("src")
    var src: String by attr { change ->
        if (change.newValue.isBlank()) {
            image.removeAttribute("src")
        } else {
            image.src = change.newValue
        }
    }

    /** `alt`: the image's alternative text. */
    @JsName("alt")
    var alt: String by attr { change -> image.alt = change.newValue }

    init {
        val shadow = attachStyledShadow(HOME_RESET)
        image = HtmlTagName.img.createElement {
            inlineStyle.apply {
                display = Display.block
                width = Auto.auto
                height = 20.px
            }
        }
        anchor = HtmlTagName.a.createElement {
            inlineStyle.display = Display.inlineFlex
            target = WindowTarget._blank
            rel = "noopener noreferrer"
            appendChild(image)
        }
        shadow.appendChild(anchor)
    }

    override fun connectedCallback() {
        applyInitialAttributes()
    }

    companion object : CustomElementReference<KtjsBadge>(KtjsBadge::class.js) {
        val NAME: TagName<KtjsBadge> = TagName(HomeMarkup.BADGE)
    }
}
