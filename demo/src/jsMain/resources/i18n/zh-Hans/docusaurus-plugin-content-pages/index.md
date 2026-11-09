---
slug: /
title: ktjs-docusaurus
description: 用 Kotlin/JS 写 Docusaurus 站点
---

# ktjs-docusaurus

用 Kotlin/JS 写 Docusaurus 站点。

- 站点配置就是 Kotlin：`src/jsMain/kotlin`。
- 页面是 `src/jsMain/resources` 里的 Markdown。
- 目录（TOC）上多一个折叠按钮，由 Kotlin 通过 client module 接上。

```bash
./gradlew :demo:docusaurusBuild
```

从[简介](/docs/intro)开始。
