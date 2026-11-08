---
slug: /
title: ktjs-docusaurus
description: Documentation sites as Kotlin/JS projects
---

# ktjs-docusaurus

Write Docusaurus sites as Kotlin/JS projects.

- The site configuration is Kotlin: `src/jsMain/kotlin`.
- The pages are Markdown in `src/jsMain/resources`.
- The table of contents gets a collapse control, wired from Kotlin through a client module.

```bash
./gradlew :demo:docusaurusBuild
```

Start with the [introduction](/docs/intro).
