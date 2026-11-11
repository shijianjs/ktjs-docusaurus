---
sidebar_position: 4
---

# Gradle plugin

The wrapper and this plugin are the two halves of the same thing: `ktjs-docusaurus` makes the site
configuration Kotlin, and `io.github.shijianjs.ktjs.docusaurus` assembles a Kotlin/JS module into a
Docusaurus site and runs the CLI on it. Together they replace the npm project — `package.json`,
`docusaurus.config.ts`, `sidebars.ts` and the scripts around them.

```kotlin
plugins {
    kotlin("multiplatform")
    id("io.github.shijianjs.ktjs.docusaurus")
}

docusaurus {
    sidebarsFunction = "sidebars"
    clientModuleFunction = "onRouteDidUpdate"
    pageFunctions = mapOf("index" to "homePage")
}
```

## What it generates

| Output | From |
| --- | --- |
| `docusaurus.config.mjs` | the exported `configFunction` |
| `sidebars.mjs` | the exported `sidebarsFunction` |
| `client-module.mjs` | the exported `clientModuleFunction` |
| `src/pages/<name>.jsx` | the exported component named in `pageFunctions` |
| `docs/`, `blog/`, `src/`, `static/`, `i18n/` | the module's resources |
| `generated/` | the compiled Kotlin/JS output |

`client-module.mjs` is what `clientModules` points at. It reads the generated site config and calls
the exported function with the `baseUrl` Docusaurus resolved for the bundle's locale — `/my-site/`
for the default locale, `/my-site/zh-Hans/` for the others, in the built site and in
`docusaurus start --locale` alike — followed by Docusaurus' own arguments. An absolute URL for a file
under `static/` has to be built from that value: the locale prefix is not knowable at compile time.

## Tasks

| Task | Effect |
| --- | --- |
| `docusaurusSite` | assembles the site directory |
| `docusaurusBuild` | writes the static site under `<site>/build` |
| `docusaurusStart` | dev server, watching the site directory |
| `docusaurusServe` | serves the last build |
| `docusaurusClear` | drops the Docusaurus caches |

## Where the site lives

`<root>/build/js/docusaurus/<module>`. It has to sit inside the Kotlin/JS npm workspace: that is
what Node walks up to when it resolves `@docusaurus/core`, so there is no second `npm install` and
no symlink.

Two consequences worth knowing:

- the generated `package.json` has **no** `"type": "module"` — Docusaurus writes CommonJS bundles
  and loads them as CJS; the `.mjs` shims are ES modules on their own;
- the site task is not a Gradle output (it is declared `@Internal`), because everything under
  `build/js` belongs to the npm workspace.

## Pages need a second module

A page component imports `@theme/Layout`, and those aliases only exist inside Docusaurus' webpack
build. The configuration is loaded by Node, so a page component in the same bundle would break the
build while the config loads. Keep the pages in their own Kotlin/JS module and point at it:

```kotlin
docusaurus {
    pageFunctions = mapOf("index" to "homePage")
    pageBundle = "ktjs-docusaurus-site-ui.mjs"
    additionalJs.from(
        project(":site-ui").layout.buildDirectory.dir("compileSync/js/main/productionLibrary/kotlin"),
    )
}

tasks.named("docusaurusSite") {
    dependsOn(":site-ui:jsProductionLibraryCompileSync")
}
```
