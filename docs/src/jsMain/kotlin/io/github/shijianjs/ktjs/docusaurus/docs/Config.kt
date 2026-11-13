package io.github.shijianjs.ktjs.docusaurus.docs

import io.github.shijianjs.ktjs.docusaurus.config.BooleanOrString
import io.github.shijianjs.ktjs.docusaurus.config.Config
import io.github.shijianjs.ktjs.docusaurus.config.Directions
import io.github.shijianjs.ktjs.docusaurus.config.HtmlTagObject
import io.github.shijianjs.ktjs.docusaurus.config.I18nConfig
import io.github.shijianjs.ktjs.docusaurus.config.I18nLocaleConfig
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
    clientModules = arrayOf("./client-module.mjs")

    // The browser bundle is self-contained (webpack inlines `iconify-icon`), so `<head>` needs no
    // import map to resolve a bare module name.

    // Local search: the index is built from the Markdown during `docusaurus build`, so there is no
    // server and no external crawler. It is a theme — `@theme/SearchBar` replaces the placeholder the
    // classic theme renders in the navbar, which is why it goes in `themes`, not `plugins`.
    themes = arrayOf<PluginConfig?>(
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
                // The layer under the kit. It is not published, but it is how every element in this
                // site is written, so it has a guide of its own instead of a corner of the kit's.
                unsafeJso<NavbarItem> {
                    type = NavbarItemTypes.docSidebar
                    sidebarId = "domKit"
                    label = "DOM kit"
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
 * Four id-matched sidebars: the user guide, the kit, the DOM kit and this repository's own
 * development notes. The navbar links to all four.
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
        domKit = arrayOf<SidebarItem>(
            unsafeJso {
                type = SidebarItemTypes.doc
                id = "dom-kit/overview"
            },
            unsafeJso {
                type = SidebarItemTypes.doc
                id = "dom-kit/custom-elements"
            },
            unsafeJso {
                type = SidebarItemTypes.doc
                id = "dom-kit/attributes"
            },
            unsafeJso {
                type = SidebarItemTypes.doc
                id = "dom-kit/styling"
            },
            unsafeJso {
                type = SidebarItemTypes.doc
                id = "dom-kit/css-sheet"
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

    var domKit: Array<SidebarItem>?

    var development: Array<SidebarItem>?
}

