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

The kit is split by runtime into two shared packages:

| Package | Loaded by | Contents |
| --- | --- | --- |
| `ktjs-docusaurus-kit` | browser + Node | the section classes and setters |
| `ktjs-docusaurus-elements` | browser only | `CustomElementAdapter`, the three element classes, tag parsing, registration |

The elements package uses the official custom-element API: `class KtjsHero : CustomElementAdapter()`
(which extends `HTMLElement`) plus a `CustomElementReference` companion and `customElements.define`.
`CustomElementAdapter` declares `connectedCallback()` once under its native name — subclasses just
override it — and provides `…Super` methods that forward to `super.foo(...)`, bypassing the
self-recursive bridges Kotlin/JS generates (see `AGENTS.md`). The official `CustomElement` interface
is deliberately not implemented: its `val connectedCallback` compiles to a getter the browser never
calls.

Because those classes evaluate `HTMLElement` at module load, the elements bundle must never be imported
by the Node side. The Gradle plugin publishes it under `static/ktjs-client/` and the client module
inserts its `<script type="module">` **after hydration**:

```kotlin
// build.gradle.kts
docusaurus {
    clientModuleFunction = "onRouteDidUpdate"
    browserBundle = "ktjs-docusaurus-ktjs-docusaurus-elements.mjs"
    browserBundleEntry = "registerHomeElements"
    browserBundleFiles.from(/* the elements module's productionLibrary/kotlin output */)
}
```

```kotlin
// the site's client module
@JsExport
fun onRouteDidUpdate() {
    if (!isBrowser) return
    loadBrowserBundle(SITE_BASE_URL + "ktjs-client/index.mjs")
    tocToggle.install()
    tocToggle.refresh()
}
```

Loading after hydration means React has already rendered the tags, so there is no hydration mismatch.
Each element owns the section it renders into — one field on the element — so the section is released
with it; `data-ktjs-read` guards a second connect.

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
