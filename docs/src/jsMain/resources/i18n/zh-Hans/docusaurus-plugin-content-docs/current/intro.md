---
sidebar_position: 1
title: 简介
---

# 简介

本站本身就是一个 Kotlin/JS 项目，没有任何手写的 npm 工程：

- 站点配置是一个 Kotlin 函数（`src/jsMain/kotlin/io/github/shijianjs/ktjs/docusaurus/docs/Config.kt`），
- 下面的页面是 `src/jsMain/resources/docs` 里的 Markdown，
- `./gradlew :docs:docusaurusBuild` 组装站点并运行 Docusaurus。

```kotlin
@JsExport
fun docusaurusConfig(): Config = unsafeJso {
    title = "ktjs-docusaurus"
    url = "https://shijianjs.github.io"
    baseUrl = "/ktjs-docusaurus/"
}
```

Gradle 插件会写出 `docusaurus.config.mjs`，从编译产物里 import 那个导出函数，并在 Gradle 已经装好
`node_modules` 的目录里调用 Docusaurus CLI。

## 各部分如何协作

```mermaid
flowchart LR
  K["Config.kt<br/>（Kotlin）"] --> P["Gradle 插件"]
  M["*.md / *.mdx<br/>（src/jsMain/resources）"] --> P
  P --> S["docusaurus.config.mjs<br/>+ 静态站点"]
  S --> B["Docusaurus 构建"]
  B --> H["GitHub Pages"]
```

下面这段围栏就是一张图，不是代码——mermaid 在全站启用（`markdown.mermaid` 和
`@docusaurus/theme-mermaid` 主题），配色在 `themeConfig.mermaid` 里配置。

所有图型都用这份配色，但 `options.look` 不是通用的：mermaid 的时序图渲染器完全不读 `look`，
流程图和类图才读。所以下面的时序图只体现配色，类图两者都体现。

```mermaid
sequenceDiagram
  autonumber
  participant B as 浏览器
  participant D as Docusaurus（React）
  participant C as client-module.mjs
  participant K as ktjs-client 包
  participant E as ktjs-* 元素
  B->>D: 打开一个路由
  D->>C: onRouteDidUpdate
  C->>K: 插入 script[type=module]
  K->>E: customElements.define
  E-->>B: 渲染 shadow root
```

```mermaid
classDiagram
  direction LR
  class Config {
    +String title
    +String baseUrl
    +ThemeConfig themeConfig
  }
  class ThemeConfig {
    +Navbar navbar
    +Footer footer
    +MermaidThemeConfig mermaid
  }
  class MermaidThemeConfig {
    +MermaidThemeByColorMode theme
    +MermaidOptions options
  }
  Config --> ThemeConfig
  ThemeConfig --> MermaidThemeConfig
```
