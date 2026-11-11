---
sidebar_position: 1
---

# Getting started

## Requirements

- JDK 21 or newer
- Gradle (the wrapper is checked in)

Node is provided by the Kotlin/JS plugin; the Docusaurus packages come from the npm workspace the
same plugin installs.

## Build the site

```bash
./gradlew :docs:docusaurusBuild
```

The static site is written to `build/js/docusaurus/docs/build`.

## Develop with a dev server

```bash
./gradlew :docs:docusaurusStart
```

Docusaurus watches the site directory, so Markdown edits reload the page. Kotlin changes need a new
build of the module first:

```bash
./gradlew :docs:docusaurusSite :docs:docusaurusStart
```

## Where things live

| Path | Contents |
| --- | --- |
| `src/jsMain/kotlin/io/github/shijianjs/ktjs/docusaurus/docs/Config.kt` | Site configuration and sidebar |
| `src/jsMain/resources/docs` | Markdown pages |
| `src/jsMain/resources/css` | Extra stylesheets |
| `src/jsMain/resources/static` | Files copied as-is into the build |
