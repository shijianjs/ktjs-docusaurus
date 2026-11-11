package demo.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junitpioneer.jupiter.RetryingTest

/**
 * End-to-end checks against the *built* site.
 *
 * The home page is itself the fixture: its MDX already declares every `ktjs-*` element, so these
 * tests exercise the real pipeline — generated HTML, hydration, the post-hydration client module
 * that registers the elements, and the `attr` delegates reacting to attributes afterwards.
 */
@UsePlaywright(DemoOptions::class)
class SiteE2eTest {

    @RetryingTest(maxAttempts = 2)
    fun `home page upgrades every kit element after hydration`(page: Page) {
        openHome(page)

        // A shadow root with content only exists once the browser bundle has run, so this also
        // proves the client module loaded the elements after React finished hydrating.
        assertThat(page.locator("ktjs-hero h1")).hasText("ktjs-docusaurus")
        assertThat(page.locator("ktjs-features h2")).hasText("Why this project")
        assertThat(page.locator("ktjs-next-steps h2")).hasText("Where to go next")
        assertEquals(2, page.locator("ktjs-action").count())
        assertEquals(4, page.locator("ktjs-feature").count())
        assertEquals(3, page.locator("ktjs-step").count())
        assertThat(page.locator("ktjs-feature h3").first()).hasText("Configuration is Kotlin")
    }

    /**
     * The route change a full page load never exercises: React builds the `<ktjs-*>` tree again,
     * client-side, while the tags are already upgraded, and assigns each JSX prop to the matching
     * property. A property that only accepts its Kotlin type makes that assignment throw, the error
     * boundary replaces the page, and the user sees "This page crashed."
     */
    @RetryingTest(maxAttempts = 2)
    fun `a client-side navigation back to the home page renders it instead of crashing`(page: Page) {
        openHome(page)

        // Both hops are in-site links, so the router re-renders the page in place — no reload, and
        // the kit stays registered from the first hop onward.
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("User guide")).click()
        assertThat(page.locator("h1")).hasText("Introduction")

        // The navbar brand and the sidebar logo both link to the site root; either is a client-side hop.
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("ktjs-docusaurus")).first().click()

        assertThat(page.locator("ktjs-hero h1")).hasText("ktjs-docusaurus")
        assertEquals(2, page.locator("ktjs-action").count())
        assertEquals(0, page.getByText("This page crashed").count())
    }

    @RetryingTest(maxAttempts = 2)
    fun `external attribute changes repaint the shadow trees`(page: Page) {
        openHome(page)

        page.setAttribute("ktjs-hero", "title", "Renamed from HTML")
        page.setAttribute("ktjs-hero", "tagline", "A new tagline")
        page.setAttribute("ktjs-features", "section-title", "Six good reasons")

        assertThat(page.locator("ktjs-hero h1")).hasText("Renamed from HTML")
        assertThat(page.locator("ktjs-hero p")).hasText("A new tagline")
        assertThat(page.locator("ktjs-features h2")).hasText("Six good reasons")

        // The Kotlin property reads straight from the DOM, so no second copy can drift.
        assertEquals(
            "Renamed from HTML",
            page.evaluate("() => document.querySelector('ktjs-hero').titleText") as String,
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `js properties exposed through JsName write the attribute`(page: Page) {
        openHome(page)

        page.evaluate(
            """() => {
                document.querySelector('ktjs-hero').titleText = 'Set from JS';
                document.querySelector('ktjs-next-steps').sectionTitle = 'Next, from JS';
            }""",
        )

        // Without `@JsName` Kotlin/JS emits only mangled accessors: the assignment above would just
        // add a plain JS field and the attribute (and therefore the shadow tree) would not change.
        assertEquals("Set from JS", page.attributeOf("ktjs-hero", "title"))
        assertEquals("Next, from JS", page.attributeOf("ktjs-next-steps", "section-title"))
        assertThat(page.locator("ktjs-hero h1")).hasText("Set from JS")
        assertThat(page.locator("ktjs-next-steps h2")).hasText("Next, from JS")
    }

    @RetryingTest(maxAttempts = 2)
    fun `boolean attributes follow presence and removing them restores the default`(page: Page) {
        openHome(page)

        val tinted = page.backgroundColorOf("ktjs-features section")
        assertTrue(tinted.isNotEmpty() && tinted != "rgba(0, 0, 0, 0)", "initial band colour: $tinted")

        // HTML boolean semantics: `<x tinted="false">` is still true.
        page.setAttribute("ktjs-features", "tinted", "false")
        assertEquals(tinted, page.backgroundColorOf("ktjs-features section"))

        page.removeAttribute("ktjs-features", "tinted")
        assertNotEquals(tinted, page.backgroundColorOf("ktjs-features section"))

        // A missing string attribute reads as its (empty) default, without writing it back.
        page.removeAttribute("ktjs-features", "section-title")
        assertThat(page.locator("ktjs-features h2")).hasText("")
        assertEquals(null, page.attributeOf("ktjs-features", "section-title"))
    }

    @RetryingTest(maxAttempts = 2)
    fun `an unknown enum value falls back to the default and a known one repaints`(page: Page) {
        openHome(page)

        page.setAttribute("ktjs-action", "kind", "not-a-kind")
        assertTrue(page.classesOf("ktjs-action a").contains("ktjs-button-primary"))

        page.setAttribute("ktjs-action", "kind", "secondary")
        assertTrue(page.classesOf("ktjs-action a").contains("ktjs-button-secondary"))
        assertEquals("_blank", page.attributeOf("ktjs-action a", "target"))
    }

    @RetryingTest(maxAttempts = 2)
    fun `a dynamically created badge reflects all three of its attributes`(page: Page) {
        openHome(page)

        page.evaluate(
            """() => {
                const badge = document.createElement('ktjs-badge');
                badge.setAttribute('href', 'https://example.com');
                badge.setAttribute('src', 'img/ktjs-docusaurus-logo.png');
                badge.setAttribute('alt', 'build badge');
                badge.id = 'e2e-badge';
                document.body.appendChild(badge);
            }""",
        )

        assertThat(page.locator("#e2e-badge img")).hasAttribute("alt", "build badge")
        assertTrue(page.attributeOf("#e2e-badge img", "src")!!.endsWith("img/ktjs-docusaurus-logo.png"))

        page.setAttribute("#e2e-badge", "alt", "changed")
        assertThat(page.locator("#e2e-badge img")).hasAttribute("alt", "changed")
        page.removeAttribute("#e2e-badge", "src")
        assertEquals(null, page.attributeOf("#e2e-badge img", "src"))
    }

    @RetryingTest(maxAttempts = 2)
    fun `the translated home page renders the same kit elements`(page: Page) {
        page.navigate(DemoServer.zhHomeUrl, Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD))

        assertThat(page.locator("ktjs-hero h1")).hasText("ktjs-docusaurus")
        assertEquals(4, page.locator("ktjs-feature").count())
        assertEquals(3, page.locator("ktjs-step").count())
    }

    private fun openHome(page: Page) {
        page.navigate(DemoServer.homeUrl, Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD))
        // The element bundle is injected by the client module *after* hydration, so a shadow root
        // with content is the readiness signal; Playwright's assertion retries until it appears.
        assertThat(page.locator("ktjs-hero h1")).isVisible()
    }

    private fun Page.setAttribute(selector: String, name: String, value: String) {
        evaluate(
            "([s, n, v]) => document.querySelector(s).setAttribute(n, v)",
            listOf(selector, name, value),
        )
    }

    private fun Page.removeAttribute(selector: String, name: String) {
        evaluate("([s, n]) => document.querySelector(s).removeAttribute(n)", listOf(selector, name))
    }

    // Playwright's selectors pierce open shadow roots, while `document.querySelector` inside
    // `page.evaluate` does not — these values live in the elements' shadow trees.
    private fun Page.attributeOf(selector: String, name: String): String? =
        locator(selector).first().evaluate("(el, n) => el.getAttribute(n)", name) as String?

    private fun Page.classesOf(selector: String): String =
        locator(selector).first().evaluate("(el) => el.className") as String

    private fun Page.backgroundColorOf(selector: String): String =
        locator(selector).first().evaluate("(el) => getComputedStyle(el).backgroundColor") as String
}
