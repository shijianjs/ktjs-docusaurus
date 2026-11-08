package io.github.shijianjs.ktjs.docusaurus.theme

import js.reflect.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Upstream source: `@docusaurus/theme-common` (`utils/useThemeConfig.ts`) for the navbar, footer and
 * colour-mode literals, `@docusaurus/theme-classic` (`NavbarItem/ComponentTypes.tsx`) for the item
 * types, and `@docusaurus/theme-mermaid` for the diagram themes.
 */
class ThemeLiteralsTest {

    @Test
    fun navbarStyles() {
        assertEquals("dark", NavbarStyles.dark.unsafeCast<String>())
        assertEquals("primary", NavbarStyles.primary.unsafeCast<String>())
    }

    @Test
    fun navbarItemPositions() {
        assertEquals("left", NavbarItemPositions.left.unsafeCast<String>())
        assertEquals("right", NavbarItemPositions.right.unsafeCast<String>())
    }

    /**
     * `link` and `default` are deliberately absent: Docusaurus validates a plain link as an item
     * without a `type` and rejects both strings with `Bad navbar item type`.
     */
    @Test
    fun navbarItemTypes() {
        assertEquals("doc", NavbarItemTypes.doc.unsafeCast<String>())
        assertEquals("docSidebar", NavbarItemTypes.docSidebar.unsafeCast<String>())
        assertEquals("docsVersion", NavbarItemTypes.docsVersion.unsafeCast<String>())
        assertEquals("docsVersionDropdown", NavbarItemTypes.docsVersionDropdown.unsafeCast<String>())
        assertEquals("dropdown", NavbarItemTypes.dropdown.unsafeCast<String>())
        assertEquals("html", NavbarItemTypes.html.unsafeCast<String>())
        assertEquals("search", NavbarItemTypes.search.unsafeCast<String>())
        assertEquals("localeDropdown", NavbarItemTypes.localeDropdown.unsafeCast<String>())
    }

    @Test
    fun footerStyles() {
        assertEquals("light", FooterStyles.light.unsafeCast<String>())
        assertEquals("dark", FooterStyles.dark.unsafeCast<String>())
    }

    @Test
    fun colorModes() {
        assertEquals("light", ColorModes.light.unsafeCast<String>())
        assertEquals("dark", ColorModes.dark.unsafeCast<String>())
    }

    @Test
    fun docsVersionPersistence() {
        assertEquals("localStorage", DocsVersionPersistences.localStorage.unsafeCast<String>())
        assertEquals("none", DocsVersionPersistences.none.unsafeCast<String>())
    }

    @Test
    fun mermaidThemes() {
        assertEquals("default", MermaidThemes.`default`.unsafeCast<String>())
        assertEquals("neutral", MermaidThemes.neutral.unsafeCast<String>())
        assertEquals("dark", MermaidThemes.dark.unsafeCast<String>())
        assertEquals("forest", MermaidThemes.forest.unsafeCast<String>())
        assertEquals("base", MermaidThemes.base.unsafeCast<String>())
    }
}
