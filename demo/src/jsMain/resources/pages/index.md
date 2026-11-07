---
slug: /
title: ktjs-docusaurus
description: Documentation sites as Kotlin/JS projects
---

# ktjs-docusaurus

Write Docusaurus sites as Kotlin/JS projects.

- The site configuration is Kotlin: `src/jsMain/kotlin/site/Config.kt`.
- The pages are Markdown in `src/jsMain/resources`.
- Gradle assembles the site and runs Docusaurus on it.

```bash
./gradlew :demo:docusaurusBuild
```

Start with the [introduction](/docs/intro).
