package io.github.shijianjs.ktjs.docusaurus.docs

import io.github.shijianjs.ktjs.docusaurus.config.BooleanOrString
import io.github.shijianjs.ktjs.docusaurus.config.Config
import io.github.shijianjs.ktjs.docusaurus.config.Directions
import io.github.shijianjs.ktjs.docusaurus.config.HtmlTagObject
import io.github.shijianjs.ktjs.docusaurus.config.I18nConfig
import io.github.shijianjs.ktjs.docusaurus.config.I18nLocaleConfig
import io.github.shijianjs.ktjs.docusaurus.config.MarkdownConfig
import io.github.shijianjs.ktjs.docusaurus.config.PluginConfig
import io.github.shijianjs.ktjs.docusaurus.config.PresetConfig
import io.github.shijianjs.ktjs.docusaurus.config.ReportingSeverities
import io.github.shijianjs.ktjs.docusaurus.config.StringOrDouble
import io.github.shijianjs.ktjs.docusaurus.config.StringOrFalse
import io.github.shijianjs.ktjs.docusaurus.plugins.contentdocs.DocsOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.searchlocal.BooleanOrHashed
import io.github.shijianjs.ktjs.docusaurus.plugins.searchlocal.Hasheds
import io.github.shijianjs.ktjs.docusaurus.plugins.searchlocal.SearchBarPositions
import io.github.shijianjs.ktjs.docusaurus.plugins.searchlocal.SearchLocalOptions
import io.github.shijianjs.ktjs.docusaurus.plugins.searchlocal.StringOrStringArray as SearchLocalStringOrStringArray
import io.github.shijianjs.ktjs.docusaurus.plugins.themeclassic.StringOrStringArray
import io.github.shijianjs.ktjs.docusaurus.plugins.themeclassic.ThemeClassicOptions
import io.github.shijianjs.ktjs.docusaurus.presets.BlogOptionsOrFalse
import io.github.shijianjs.ktjs.docusaurus.presets.DocsOptionsOrFalse
import io.github.shijianjs.ktjs.docusaurus.presets.classicPreset
import io.github.shijianjs.ktjs.docusaurus.sidebars.SidebarCategoryLink
import io.github.shijianjs.ktjs.docusaurus.sidebars.SidebarCategoryLinkTypes
import io.github.shijianjs.ktjs.docusaurus.sidebars.SidebarItem
import io.github.shijianjs.ktjs.docusaurus.sidebars.SidebarItemTypes
import io.github.shijianjs.ktjs.docusaurus.sidebars.SidebarsConfig
import io.github.shijianjs.ktjs.docusaurus.theme.ColorModes
import io.github.shijianjs.ktjs.docusaurus.theme.Footer
import io.github.shijianjs.ktjs.docusaurus.theme.FooterStyles
import io.github.shijianjs.ktjs.docusaurus.theme.MermaidTheme
import io.github.shijianjs.ktjs.docusaurus.theme.MermaidThemeByColorMode
import io.github.shijianjs.ktjs.docusaurus.theme.MermaidThemeConfig
import io.github.shijianjs.ktjs.docusaurus.theme.NavbarItem
import io.github.shijianjs.ktjs.docusaurus.theme.NavbarItemPositions
import io.github.shijianjs.ktjs.docusaurus.theme.NavbarItemTypes
import io.github.shijianjs.ktjs.docusaurus.theme.NavbarLogo
import io.github.shijianjs.ktjs.docusaurus.theme.PrismThemes
import io.github.shijianjs.ktjs.docusaurus.theme.SimpleFooter
import io.github.shijianjs.ktjs.docusaurus.theme.ThemeConfig
import js.objects.Record
import js.objects.unsafeJso
import kotlin.js.JsAny
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.js.unsafeCast

private const val REPOSITORY = "https://github.com/shijianjs/ktjs-docusaurus"

private const val SITE_URL = "https://shijianjs.github.io"

internal const val SITE_BASE_URL = "/ktjs-docusaurus/"

/**
 * The API reference: Dokka output that `pagesSite` deploys next to the site, so it is not a route of
 * this Docusaurus build. The link is a full URL because Docusaurus validates absolute internal paths
 * against its own routes and would report `/ktjs-docusaurus/api/` as broken; a full URL is treated as
 * external and left alone.
 */
private const val API_URL = "$SITE_URL$SITE_BASE_URL" + "api/"

/**
 * The DOM helpers this site is built on: a library with its own repository, site and API reference
 * (`shijianjs/ktjs-dom-kit`). Full URL, because it is not a route of this Docusaurus build.
 */
private const val DOM_KIT_URL = "$SITE_URL/ktjs-dom-kit/"

/**
 * The site configuration, imported by the generated `docusaurus.config.mjs`.
 *
 * Everything a `docusaurus.config.ts` can express is a Kotlin value here; the Gradle plugin turns
 * the exported function into the default export Docusaurus loads.
 */
@JsExport
fun docusaurusConfig(): Config = unsafeJso {
    title = "ktjs-docusaurus"
    tagline = "Documentation sites as Kotlin/JS projects"
    url = SITE_URL
    baseUrl = SITE_BASE_URL
    favicon = "img/ktjs-docusaurus-logo.png"
    trailingSlash = true
    onBrokenLinks = ReportingSeverities.`throw`

    // ```mermaid fences render as diagrams. Two halves, and both are needed: this flag is what
    // compiles the fence into a `<Mermaid>` element, and the theme registered below is what provides
    // that element in the browser.
    markdown = unsafeJso<MarkdownConfig> {
        mermaid = true
    }

    // English is the source language; every page — `docs/` and `pages/` alike — has a translation
    // under `i18n/zh-Hans/`, in that plugin's own directory (`docusaurus-plugin-content-docs` and
    // `docusaurus-plugin-content-pages`). `code.json` there is only for strings a component owns.
    i18n = unsafeJso<I18nConfig> {
        defaultLocale = "en"
        locales = arrayOf("en", "zh-Hans")
        localeConfigs = unsafeJso<DocsLocaleConfigs> {
            en = unsafeJso<I18nLocaleConfig> {
                label = "English"
                direction = Directions.ltr
                htmlLang = "en"
            }
            zhHans = unsafeJso<I18nLocaleConfig> {
                label = "简体中文"
                direction = Directions.ltr
                htmlLang = "zh-Hans"
            }
        }.unsafeCast<Record<JsAny, I18nLocaleConfig>>()
    }

    // The landing-page elements and the TOC toggle are registered by `onRouteDidUpdate` in the
    // generated `client-module.mjs`. That runs after hydration, so the custom elements are upgraded
    // once React is done with the page.
    //
    // `d3-transition` is imported for its side effect, not its exports: it is what puts `transition()`
    // on d3 selections, and mermaid calls it for the flowchart node tooltips. Mermaid only reaches it
    // through `d3`'s re-exports, and `d3` declares `sideEffects: false`, so webpack drops the module
    // and hovering any flowchart node throws "tooltipElem.transition is not a function". Importing the
    // package itself keeps it (its own package.json says `sideEffects: true`). The path is relative to
    // the site directory, which the Gradle plugin fixes at `<root>/build/js/docusaurus/<module>`.
    clientModules = arrayOf(
        "./client-module.mjs",
        "../../node_modules/d3-transition",
    )

    // The browser bundle is self-contained (webpack inlines `iconify-icon`), so `<head>` needs no
    // import map to resolve a bare module name.

    // Two themes, both resolved by name from the generated configuration:
    //
    // - `@docusaurus/theme-mermaid` provides `@theme/Mermaid`, the element a ```mermaid fence
    //   compiles into. Its palette and look are site configuration (`themeConfig.mermaid` below), so
    //   it is registered with its own defaults.
    // - Local search: the index is built from the Markdown during `docusaurus build`, so there is no
    //   server and no external crawler. It is a theme — `@theme/SearchBar` replaces the placeholder
    //   the classic theme renders in the navbar, which is why it goes in `themes`, not `plugins`.
    themes = arrayOf<PluginConfig?>(
        PluginConfig("@docusaurus/theme-mermaid"),
        PluginConfig(
            "@easyops-cn/docusaurus-search-local",
            unsafeJso<SearchLocalOptions> {
                // Every language on the site has to be listed: each one gets its own tokenizer.
                language = SearchLocalStringOrStringArray(arrayOf("en", "zh"))
                // The blog is not enabled in this site, so there is nothing to index.
                indexBlog = false
                // Put the content hash in the index URL: a redeploy invalidates browser caches.
                hashed = BooleanOrHashed(Hasheds.query)
                searchBarPosition = SearchBarPositions.auto
            },
        ),
    )

    presets = arrayOf<PresetConfig?>(
        classicPreset(
            unsafeJso {
                docs = DocsOptionsOrFalse(
                    unsafeJso<DocsOptions> {
                        // Generated by the same Gradle plugin; it sits next to the config.
                        sidebarPath = StringOrFalse("sidebars.mjs")
                        routeBasePath = "docs"
                        breadcrumbs = true
                    },
                )
                blog = BlogOptionsOrFalse(false)
                theme = unsafeJso<ThemeClassicOptions> {
                    customCss = StringOrStringArray("src/css/custom.css")
                }
            },
        ),
    )

    themeConfig = unsafeJso<ThemeConfig> {
        navbar = unsafeJso {
            title = "ktjs-docusaurus"
            // The same file the favicon uses, so the navbar logo costs no extra download; `alt` is
            // left out because Docusaurus marks it decorative when the navbar also shows the title.
            logo = unsafeJso<NavbarLogo> {
                src = "img/ktjs-docusaurus-logo.png"
                width = StringOrDouble(32.0)
                height = StringOrDouble(32.0)
            }
            hideOnScroll = true
            items = arrayOf(
                unsafeJso<NavbarItem> {
                    type = NavbarItemTypes.docSidebar
                    sidebarId = "userGuide"
                    label = "User guide"
                    position = NavbarItemPositions.left
                },
                // The kit gets its own top-level entry, the same shape the duckfn docs use for their
                // docs-kit section: it is a separate product with its own guide.
                unsafeJso<NavbarItem> {
                    type = NavbarItemTypes.docSidebar
                    sidebarId = "docsKit"
                    label = "Docs kit"
                    position = NavbarItemPositions.left
                },
                // The layer under the kit — the custom-element base class, the attribute delegate and
                // the typed stylesheets. It has a repository and a site of its own, so this is a jump
                // out rather than a section here; the URLs are absolute because a relative one would
                // land under this site's baseUrl.
                unsafeJso<NavbarItem> {
                    label = "DOM kit"
                    href = DOM_KIT_URL
                    position = NavbarItemPositions.left
                },
                // How this repository is built, as opposed to how a site is built with it. Kept apart
                // so the two audiences do not have to read each other's pages.
                unsafeJso<NavbarItem> {
                    type = NavbarItemTypes.docSidebar
                    sidebarId = "development"
                    label = "Development"
                    position = NavbarItemPositions.left
                },
                unsafeJso<NavbarItem> {
                    type = NavbarItemTypes.localeDropdown
                    position = NavbarItemPositions.right
                },
                // The API reference is the other half of this project's documentation: Dokka output
                // deployed next to the site by `pagesSite`.
                unsafeJso<NavbarItem> {
                    label = "API"
                    href = API_URL
                    position = NavbarItemPositions.right
                },
                // A plain link has no `type`: Docusaurus validates a missing type as the default
                // item and rejects an explicit "default" or "link".
                unsafeJso<NavbarItem> {
                    label = "GitHub"
                    href = REPOSITORY
                    position = NavbarItemPositions.right
                },
            )
        }
        colorMode = unsafeJso {
            defaultMode = ColorModes.light
            respectPrefersColorScheme = true
        }
        docs = unsafeJso {
            sidebar = unsafeJso { hideable = true }
        }
        prism = unsafeJso {
            theme = PrismThemes.github
            darkTheme = PrismThemes.dracula
            additionalLanguages = arrayOf("bash", "kotlin", "json")
        }
        // Both names are mermaid's own and both come from its `redux` family: a white card with a
        // dark outline in light mode, its dark counterpart in dark mode. `MermaidThemes` lists the
        // five older names (default / neutral / dark / forest / base), so these two enter through the
        // factory that wraps a value mermaid defines.
        //
        // `options` is spread into `mermaid.initialize` as `{startOnLoad: false, ...options, theme}`,
        // the theme above winning over anything set here. Mermaid *ignores* a value it does not
        // recognise rather than reporting it, so the palette and `look: 'neo'` are verified in a
        // browser, never by a build.
        mermaid = unsafeJso<MermaidThemeConfig> {
            theme = unsafeJso<MermaidThemeByColorMode> {
                light = MermaidTheme("redux-color")
                dark = MermaidTheme("redux-dark-color")
            }
            options = unsafeJso<MermaidOptions> {
                look = "neo"
            }
        }
        footer = Footer(
            unsafeJso<SimpleFooter> {
                style = FooterStyles.dark
                links = arrayOf(
                    unsafeJso {
                        label = "API"
                        href = API_URL
                    },
                    unsafeJso {
                        label = "GitHub"
                        href = REPOSITORY
                    },
                )
                copyright = "Built with Docusaurus from a Kotlin/JS project."
            },
        )
    }
}

/**
 * The sidebars, imported by the generated `sidebars.mjs`.
 *
 * Three id-matched sidebars: the user guide, the kit and this repository's own development notes. The
 * navbar links to all three; the DOM helpers have a site of their own.
 *
 * Only the categories carry a label here. A doc keeps its own: Docusaurus labels the sidebar entry
 * and the previous/next buttons from the page's `sidebar_label` or its heading, so a translated page
 * brings its translated label along, and a translated sidebar entry is not enough for the buttons.
 *
 * Every category links to a `generated-index` page — the label is clickable and the section gets a
 * landing page without a hand-written index doc. The slug is explicit because the default is derived
 * from the label, which is translated; the title still comes from the label, so the page is localised.
 *
 * The Gradle plugin belongs to the user guide, not the kit: the wrapper and the plugin are the two
 * halves of a Docusaurus site, while the kit is this project's own additions on top.
 */
@JsExport
fun sidebars(): SidebarsConfig = unsafeJso<DocsSidebars> {
        userGuide = arrayOf<SidebarItem>(
            unsafeJso {
                type = SidebarItemTypes.doc
                id = "intro"
            },
            unsafeJso {
                type = SidebarItemTypes.category
                label = "Guide"
                collapsed = false
                // A category label is only a link when the category says what it links to. Both
                // categories point at a page Docusaurus generates from the docs below them, so the
                // label is clickable and the section needs no hand-written index doc. The slug is
                // spelled out because the default is derived from the label, which is translated.
                link = unsafeJso<SidebarCategoryLink> {
                    type = SidebarCategoryLinkTypes.generatedIndex
                    slug = "category/guide"
                }
                items = arrayOf(
                    unsafeJso {
                        type = SidebarItemTypes.doc
                        id = "guide/getting-started"
                    },
                    unsafeJso {
                        type = SidebarItemTypes.doc
                        id = "guide/content"
                    },
                    unsafeJso {
                        type = SidebarItemTypes.doc
                        id = "guide/local-search"
                    },
                )
            },
            unsafeJso {
                type = SidebarItemTypes.doc
                id = "guide/gradle-plugin"
            },
        )
        docsKit = arrayOf<SidebarItem>(
            unsafeJso {
                type = SidebarItemTypes.doc
                id = "kit/overview"
            },
            unsafeJso {
                type = SidebarItemTypes.category
                label = "Building blocks"
                collapsed = false
                link = unsafeJso<SidebarCategoryLink> {
                    type = SidebarCategoryLinkTypes.generatedIndex
                    slug = "category/building-blocks"
                }
                items = arrayOf(
                    unsafeJso {
                        type = SidebarItemTypes.doc
                        id = "kit/home-sections"
                    },
                    unsafeJso {
                        type = SidebarItemTypes.doc
                        id = "kit/toc-toggle"
                    },
                )
            },
        )
        development = arrayOf<SidebarItem>(
            unsafeJso {
                type = SidebarItemTypes.doc
                id = "development/roadmap"
            },
        )
}.unsafeCast<SidebarsConfig>()

/** Attributes for the generated `<script type="importmap">`. */
private external interface ImportMapAttributes {
    var type: String
}

/**
 * The one key this site sets in mermaid's `Config`; upstream type is the whole `mermaidAPI.Config`,
 * which the wrapper leaves opaque. Declared rather than built from a raw object literal so the key
 * name and the value are checked here — add a property per key this site needs.
 */
private external interface MermaidOptions {
    var look: String
}

/** The locale record the site declares; keys must match the ids in `Config.i18n.locales`. */
private external interface DocsLocaleConfigs {
    var en: I18nLocaleConfig?

    @JsName("zh-Hans")
    var zhHans: I18nLocaleConfig?
}

/** The sidebar record: one property per sidebar id. */
private external interface DocsSidebars {
    var userGuide: Array<SidebarItem>?

    var docsKit: Array<SidebarItem>?

    var development: Array<SidebarItem>?
}

