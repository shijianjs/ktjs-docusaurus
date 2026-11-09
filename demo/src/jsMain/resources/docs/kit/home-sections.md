---
sidebar_position: 1
---

# Home sections

A landing page is written in MDX: the section tags below read like the markup they produce, and the
site's client module turns them into the kit's sections. No React page and no `.tsx` file.

```mdx
<ktjs-hero logo-src="img/favicon.svg" title="my-project" tagline="One sentence about it">
  <ktjs-action kind="primary" href="docs/intro">Get started</ktjs-action>
  <ktjs-action kind="secondary" href="https://github.com/…" icon="simple-icons:github">GitHub</ktjs-action>
</ktjs-hero>

<ktjs-features section-title="Why this project" tinted>
  <ktjs-feature icon="lucide:package" title="No npm project to maintain">
    Versions live in one package.json for Dependabot.
  </ktjs-feature>
</ktjs-features>

<ktjs-next-steps section-title="Where to go next">
  <ktjs-step href="docs/intro" title="Introduction">What the project is.</ktjs-step>
</ktjs-next-steps>
```

| Tag | Attributes | Children |
| --- | --- | --- |
| `ktjs-hero` | `logo-src`, `title`, `tagline` | `ktjs-action` (`kind`, `href`, `icon`), `ktjs-badge` (`href`, `src`, `alt`) |
| `ktjs-features` | `section-title`, `tinted` | `ktjs-feature` (`icon`, `title`), body text is the details |
| `ktjs-next-steps` | `section-title` | `ktjs-step` (`href`, `title`), body text is the details |

## Wiring

**The kit is a plain component library.** `HomeHero(mountPoint)`, `FeatureSection(mountPoint, tinted)`
and `NextStepSection(mountPoint)` build a subtree and expose named setters (`setTitle`, `setFeatures`,
`setSteps`, …). It knows nothing about MDX, tag names, attributes or custom elements — the table above
describes the *site's* markup, not the kit's API.

That declarative layer lives in a separate package (`demo/.../demo/elements`): it registers the tags,
reads each host's attributes and child tags once, and drives the kit's setters. A site wires it from
its client module:

```kotlin
// build.gradle.kts
docusaurus {
    clientModuleFunction = "onRouteDidUpdate"
}
```

```kotlin
// the site's client module
@JsExport
fun onRouteDidUpdate() {
    if (!isBrowser) return
    registerHomeElements()
    tocToggle.install()
    tocToggle.refresh()
}
```

`registerHomeElements()` is idempotent and safe on every route change, so tags that arrive with a
client-side navigation are upgraded too. Each element owns the section it renders into — one field on
the element — so the section is released with it; `data-ktjs-read` guards a second connect.

The element classes come from a short JavaScript bridge in that package — a Kotlin class extending
`HTMLElement` is not usable here (see `AGENTS.md`). The bridge is a function expression, not
module-level code, so the bundle stays loadable by Node: no separate browser bundle or `<script>` tag
is needed, and registering after hydration means React sees no mismatch.

The js targets still compile at ES2015 (set once in the root build). Kotlin/JS 2.4 accepts only
`es5` / `es2015`; `es2020` arrives with 2.5.

## Links

Write `href`s **relative to the page** — `docs/intro`, not `/docs/intro`. The same MDX file then works
under every locale prefix (`/my-site/` and `/my-site/zh-Hans/`) without the kit having to resolve a
base URL.

## Icons

`icon` values are Iconify names (`lucide:sparkles`, `simple-icons:github`). The kit creates
`<iconify-icon>` elements and lets the official package fetch the glyph; nothing ships with the site.

## Styling

Class names are prefixed with `ktjs-`, and the palette is read from inherited custom properties, so a
site restyles the sections without forking them:

```css
:root {
  --ktjs-accent: #1f6feb;
  --ktjs-accent-strong: #1a5fd0;
}
```

## From Kotlin

The tags are sugar over the classes; code can mount a section directly, which is what a page that
needs computed content would do:

```kotlin
val hero = HomeHero(host)
hero.setTitle("my-project")
hero.setLogo("img/favicon.svg")
hero.setPrimaryAction(HeroLink("Get started", "docs/intro"))
```

Both paths end in the same setters — there is no `render()` / `apply()` entry point.
