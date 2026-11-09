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
| `resources/pages` | `src/pages/` | Standalone pages (`.md`, `.mdx` or the `.jsx` shim of a Kotlin component) |
| `resources/css` | `src/css/` | Extra stylesheets |
| `resources/static` | `static/` | Files copied as-is |
| `resources/i18n` | `i18n/` | Translations |

## Translations

`docusaurus.config.mjs` declares two locales, `en` and `zh-Hans`, and English is the source
language. Every translated page mirrors its source path under the locale's own plugin directory —
documentation pages under `docusaurus-plugin-content-docs/current/`, standalone pages under
`docusaurus-plugin-content-pages/`:

```
resources/docs/intro.md
resources/i18n/zh-Hans/docusaurus-plugin-content-docs/current/intro.md

resources/pages/index.md
resources/i18n/zh-Hans/docusaurus-plugin-content-pages/index.md
```

Page copy therefore stays in the page: two Markdown files, one per locale, and the home page is no
exception. `code.json` under the same `i18n` directory is for the strings a *component* owns — the
navbar, the footer, and anything a Kotlin page renders with `translate { id = …; message = "…" }`.

## Why not MDX for prose

MDX is available, and a page may use it. It is just not the default: a page that is only prose is
a `.md` file, so nothing about it depends on a React component tree. When a page does need a
`<Layout>`-level tweak or a component, name it `index.mdx` and it goes through the same MDX
pipeline — `resources/pages` is copied verbatim, so the extension is the only thing that changes.
