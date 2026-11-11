---
sidebar_position: 4
---

# Gradle 插件

wrapper 与这个插件是同一件事的两半：`ktjs-docusaurus` 让站点配置是 Kotlin，`io.github.shijianjs.ktjs.docusaurus`
把一个 Kotlin/JS 模块组装成 Docusaurus 站点并在上面运行 CLI。两者合起来替代整个 npm 工程——`package.json`、
`docusaurus.config.ts`、`sidebars.ts` 以及围着它们的脚本。

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

## 它生成什么

| 产物 | 来自 |
| --- | --- |
| `docusaurus.config.mjs` | 导出的 `configFunction` |
| `sidebars.mjs` | 导出的 `sidebarsFunction` |
| `client-module.mjs` | 导出的 `clientModuleFunction` |
| `src/pages/<name>.jsx` | `pageFunctions` 里点名的导出组件 |
| `docs/`、`blog/`、`src/`、`static/`、`i18n/` | 模块的资源 |
| `generated/` | 编译出的 Kotlin/JS 产物 |

`client-module.mjs` 就是 `clientModules` 指向的文件。它会读取生成的站点配置，把 Docusaurus 为该 bundle
所属 locale 解析出的 `baseUrl` 作为第一个参数传给导出的函数（默认语言是 `/my-site/`，其他语言是
`/my-site/zh-Hans/`，构建产物与 `docusaurus start --locale` 都一样），后面再跟 Docusaurus 自己的参数。
`static/` 下的文件要拼绝对 URL 时必须用这个值：locale 前缀在编译期是不知道的。

## 任务

| 任务 | 作用 |
| --- | --- |
| `docusaurusSite` | 组装站点目录 |
| `docusaurusBuild` | 把静态站点写到 `<site>/build` |
| `docusaurusStart` | 开发服务器，监听站点目录 |
| `docusaurusServe` | 伺服上一次构建的结果 |
| `docusaurusClear` | 清掉 Docusaurus 的缓存 |

## 站点目录的位置

`<root>/build/js/docusaurus/<module>`。它必须待在 Kotlin/JS 的 npm workspace 里：Node 解析
`@docusaurus/core` 时就是往上走这些目录，所以不需要第二次 `npm install`，也不需要符号链接。

两个值得知道的后果：

- 生成的 `package.json` **没有** `"type": "module"`——Docusaurus 写出的是 CommonJS bundle 并按 CJS
  加载；`.mjs` shim 自身就是 ES module；
- 站点任务不是 Gradle 的输出（声明为 `@Internal`），因为 `build/js` 下的一切都属于 npm workspace。

## 页面需要第二个模块

页面组件会 import `@theme/Layout`，而这些别名只存在于 Docusaurus 的 webpack 构建里。配置是由 Node 加载的，
所以同一个 bundle 里放页面组件会在配置加载阶段就把构建弄坏。把页面放到自己的 Kotlin/JS 模块里，然后指过去：

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
