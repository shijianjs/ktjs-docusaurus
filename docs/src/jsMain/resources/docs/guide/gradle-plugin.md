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

## Per-run configuration

A site's committed `startArguments` is its default; a machine or a run that needs something else
does not edit the build file:

```bash
# one invocation: typed task options
./gradlew :docs:docusaurusStart --locale en --port 3001

# the whole shell, or one line in gradle.properties
./gradlew -Pdocusaurus.locale=en -Pdocusaurus.port=3001 :docs:docusaurusStart

# flags the plugin does not model
./gradlew -Pdocusaurus.arguments="--host 0.0.0.0 --poll 1000" :docs:docusaurusStart
```

- `--locale` / `--port` **replace** the committed value: both `--locale zh-Hans` and
  `--locale=zh-Hans` are dropped before the new pair is appended, so the command line never carries
  two contradicting flags. A task option wins over `-P`, which wins over `startArguments`.
- `docusaurus.arguments` is appended as written (`--host`, `--poll`, anything else Docusaurus takes)
  and comes before the typed values, which can therefore still override it.
- `-P`, not `-D`: a project property is part of Gradle's input model (configuration cache keys,
  build scans), while a system property is not — and `-D` competes with the JVM's own options.

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
