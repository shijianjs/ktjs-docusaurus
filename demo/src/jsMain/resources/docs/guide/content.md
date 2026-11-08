---
sidebar_position: 2
---

# Content and i18n

Pages are Markdown files under `src/jsMain/resources`. The Gradle plugin copies them into the site
directory; nothing is generated from Kotlin for prose.

| Directory | Ends up at | Purpose |
| --- | --- | --- |
| `resources/docs` | `docs/` | Documentation pages |
| `resources/blog` | `blog/` | Blog posts (disabled in this site) |
| `resources/pages` | `src/pages/` | Standalone pages |
| `resources/css` | `src/css/` | Extra stylesheets |
| `resources/static` | `static/` | Files copied as-is |
| `resources/i18n` | `i18n/` | Translations |

## Translations

`docusaurus.config.mjs` declares two locales, `en` and `zh-Hans`, and English is the source
language. A translated page mirrors the source path under
`resources/i18n/zh-Hans/docusaurus-plugin-content-docs/current/`:

```
resources/docs/intro.md
resources/i18n/zh-Hans/docusaurus-plugin-content-docs/current/intro.md
```

Strings that are not page content live in `code.json` files under the same `i18n` directory. The
home page is the interesting case: its copy is resolved in Kotlin with
`translate { id = "homepage.tagline"; message = "…" }`, and the same id is what
`i18n/zh-Hans/code.json` translates.

## Why not MDX for prose

MDX is available, and a page may use it. It is just not the default: a page that is only prose is
a `.md` file, so nothing about it depends on a React component tree.
