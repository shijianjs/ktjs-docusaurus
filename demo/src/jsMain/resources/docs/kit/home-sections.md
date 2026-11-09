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

Each tag is a real custom element. The classes live in a **browser-only** Kotlin/JS module — a class
that extends `HTMLElement` reads it when the module loads, and the configuration's bundle is loaded by
Node, which has none. The Gradle plugin publishes that bundle under `static/` and the site loads it
with a `<script type="module">` tag:

```kotlin
// build.gradle.kts
docusaurus {
    browserBundle = "ktjs-docusaurus-demo-client.mjs"
    browserBundleEntry = "start"
    additionalJs.from(project(":demo-client").layout.buildDirectory.dir("compileSync/js/main/productionLibrary/kotlin"))
}
```

```kotlin
// demo-client, the browser module
@JsExport
fun start() {
    registerKtjsElements()   // defines <ktjs-hero>, <ktjs-features>, <ktjs-next-steps>
}
```

The browser upgrades the tags as soon as the registry knows the classes — including tags that arrive
with a client-side navigation. `connectedCallback` reads the attributes and child tags once, builds the
section into the element itself and drops the declarative children (`data-ktjs-read` guards the second
call).

The module has to compile at ES2015 or later (`js { compilerOptions { target = "es2015" } }`, already
set for every module in the root build). At the default ES5 target Kotlin turns the class into
`function X() { HTMLElement.call(this) }`, and a DOM constructor may only be invoked with `new` — the
browser throws `Please use the 'new' operator` the first time an element is constructed.

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
