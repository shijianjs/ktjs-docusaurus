package io.github.shijianjs.ktjs.docusaurus.kit.elements

import io.github.shijianjs.ktjs.dom.createElement
import io.github.shijianjs.ktjs.dom.defineIfMissing
import io.github.shijianjs.ktjs.dom.inlineStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import web.cssom.Auto
import web.cssom.ClassName
import web.cssom.Margin
import web.cssom.px
import web.dom.document
import web.html.HTMLElement
import web.html.HtmlTagName
import web.window.window

class HomeElementsTest {

    @Test
    fun heroFollowsItsAttributes() {
        defineIfMissing(KtjsHero.NAME, KtjsHero.Companion)
        val hero = KtjsHero.NAME.createElement()
        hero.setAttribute("title", "First")
        hero.setAttribute("tagline", "The tagline")
        hero.setAttribute("logo-src", "img/logo.png")
        document.body.appendChild(hero)

        val shadow = assertNotNull(hero.shadowRoot)
        val heading = assertNotNull(shadow.querySelector(HtmlTagName.h1))
        val tagline = assertNotNull(shadow.querySelector(HtmlTagName.p))
        val logo = assertNotNull(shadow.querySelector(HtmlTagName.img))

        assertEquals("First", heading.textContent)
        assertEquals("The tagline", tagline.textContent)
        assertEquals("img/logo.png", logo.getAttribute("src"))

        hero.setAttribute("title", "Second")
        hero.setAttribute("tagline", "Changed")
        hero.setAttribute("logo-src", "")
        hero.applyInitialAttributes()

        assertEquals("Second", heading.textContent)
        assertEquals("Changed", tagline.textContent)
        assertNull(logo.getAttribute("src"))

        document.body.removeChild(hero)
    }

    @Test
    fun actionFollowsKindHrefAndIcon() {
        defineIfMissing(KtjsAction.NAME, KtjsAction.Companion)
        val action = KtjsAction.NAME.createElement()
        action.setAttribute("kind", "primary")
        action.setAttribute("href", "docs/intro")
        action.textContent = " Get started "
        document.body.appendChild(action)

        val shadow = assertNotNull(action.shadowRoot)
        val anchor = assertNotNull(shadow.querySelector(HtmlTagName.a))

        assertTrue(anchor.classList.contains(ClassName("ktjs-button-primary")))
        assertEquals("Get started", anchor.textContent)
        assertTrue(anchor.getAttribute("href")!!.endsWith("docs/intro"))

        action.setAttribute("kind", "secondary")
        action.setAttribute("href", "")
        action.applyInitialAttributes()

        assertTrue(anchor.classList.contains(ClassName("ktjs-button-secondary")))
        assertNull(anchor.getAttribute("href"))
        assertEquals("_blank", anchor.getAttribute("target"))

        action.setAttribute("icon", "lucide:github")
        action.applyInitialAttributes()
        val icon = assertNotNull(shadow.querySelector("iconify-icon"))
        assertEquals("lucide:github", icon.getAttribute("icon"))

        action.setAttribute("icon", "")
        action.applyInitialAttributes()
        assertNull(shadow.querySelector("iconify-icon"))

        document.body.removeChild(action)
    }

    @Test
    fun badgeFollowsItsAttributes() {
        defineIfMissing(KtjsBadge.NAME, KtjsBadge.Companion)
        val badge = KtjsBadge.NAME.createElement()
        badge.setAttribute("href", "https://example.com")
        badge.setAttribute("src", "badge.svg")
        badge.setAttribute("alt", "badge")
        document.body.appendChild(badge)

        val shadow = assertNotNull(badge.shadowRoot)
        val anchor = assertNotNull(shadow.querySelector(HtmlTagName.a))
        val image = assertNotNull(shadow.querySelector(HtmlTagName.img))

        assertEquals("badge", image.alt)
        assertTrue(image.getAttribute("src")!!.endsWith("badge.svg"))
        assertTrue(anchor.getAttribute("href")!!.endsWith("example.com"))

        badge.setAttribute("alt", "other")
        badge.setAttribute("src", "")
        badge.applyInitialAttributes()

        assertEquals("other", image.alt)
        assertNull(image.getAttribute("src"))

        document.body.removeChild(badge)
    }

    @Test
    fun featureAndStepFollowTheirAttributes() {
        defineIfMissing(KtjsFeature.NAME, KtjsFeature.Companion)
        defineIfMissing(KtjsStep.NAME, KtjsStep.Companion)

        val feature = KtjsFeature.NAME.createElement()
        feature.setAttribute("title", "Card")
        feature.setAttribute("icon", "lucide:package")
        feature.textContent = "Details"
        document.body.appendChild(feature)

        val featureShadow = assertNotNull(feature.shadowRoot)
        assertEquals("Card", featureShadow.querySelector(HtmlTagName.h3)!!.textContent)
        assertEquals("Details", featureShadow.querySelector(HtmlTagName.p)!!.textContent)
        assertEquals(
            "lucide:package",
            featureShadow.querySelector("iconify-icon")!!.getAttribute("icon"),
        )

        feature.setAttribute("title", "Renamed")
        feature.applyInitialAttributes()
        assertEquals("Renamed", featureShadow.querySelector(HtmlTagName.h3)!!.textContent)

        val step = KtjsStep.NAME.createElement()
        step.setAttribute("title", "Intro")
        step.setAttribute("href", "docs/intro")
        step.textContent = "Read this"
        document.body.appendChild(step)

        val stepShadow = assertNotNull(step.shadowRoot)
        val anchor = assertNotNull(stepShadow.querySelector(HtmlTagName.a))
        assertEquals("Intro", anchor.textContent!!.substringBefore("Read this"))

        step.setAttribute("title", "Start")
        step.applyInitialAttributes()
        assertTrue(anchor.textContent!!.startsWith("Start"))

        document.body.removeChild(feature)
        document.body.removeChild(step)
    }

    @Test
    fun sectionsFollowTheirAttributes() {
        defineIfMissing(KtjsFeatures.NAME, KtjsFeatures.Companion)
        val features = KtjsFeatures.NAME.createElement()
        features.setAttribute("section-title", "Why")
        document.body.appendChild(features)

        val shadow = assertNotNull(features.shadowRoot)
        val band = assertNotNull(shadow.querySelector(HtmlTagName.section))
        assertEquals("Why", shadow.querySelector(HtmlTagName.h2)!!.textContent)
        assertEquals("", band.style.backgroundColor)

        features.setAttribute("tinted", "")
        features.applyInitialAttributes()
        assertTrue(band.style.backgroundColor.isNotEmpty())

        features.setAttribute("section-title", "Why this")
        features.removeAttribute("tinted")
        features.applyInitialAttributes()
        assertEquals("Why this", shadow.querySelector(HtmlTagName.h2)!!.textContent)
        assertEquals("", band.style.backgroundColor)
        assertFalse(features.hasAttribute("tinted"))

        document.body.removeChild(features)
    }

    /**
     * A band is one viewport wide and centred on the viewport, so it reaches both window edges even
     * though the page keeps its article inside a narrower column (Docusaurus caps `.container` at
     * `--ifm-container-width-xl` on screens 1440px and wider). `100vw` counts the desktop
     * scrollbar's gutter, which the site absorbs with `html { overflow-x: clip }`; this test pins
     * down the bleed itself, the e2e one pins down "no horizontal scrollbar".
     */
    @Test
    fun fullBleedSectionsSpanTheViewportNotTheContentColumn() {
        defineIfMissing(KtjsHero.NAME, KtjsHero.Companion)
        defineIfMissing(KtjsFeatures.NAME, KtjsFeatures.Companion)

        // A narrow centred column stands in for the page container the band has to escape.
        val column = HtmlTagName.div.createElement {
            inlineStyle.apply {
                width = 500.px
                margin = Margin(0.px, Auto.auto)
            }
        }
        val hero = KtjsHero.NAME.createElement()
        val features = KtjsFeatures.NAME.createElement()
        column.appendChild(hero)
        column.appendChild(features)
        document.body.appendChild(column)

        val viewport = window.innerWidth.toDouble()
        for (host in listOf<HTMLElement>(hero, features)) {
            val band = assertNotNull(host.shadowRoot?.querySelector(HtmlTagName.section)).getBoundingClientRect()
            assertEquals(viewport, band.width, 0.5, "${host.tagName} band width")
            assertEquals(viewport / 2, band.left + band.width / 2, 0.5, "${host.tagName} band centre")
        }

        document.body.removeChild(column)
    }
}
