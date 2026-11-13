# ktjs-docusaurus

用 Kotlin/JS 写 Docusaurus 站点：配置是 Kotlin，页面是 Markdown，构建是 Gradle。

```kotlin
@JsExport
fun docusaurusConfig(): Config = unsafeJso {
    title = "my-project"
    url = "https://shijianjs.github.io"
    baseUrl = "/my-project/"
    presets = arrayOf(classicPreset(unsafeJso {
        docs = DocsOptionsOrFalse(unsafeJso<DocsOptions> { sidebarPath = StringOrFalse("sidebars.mjs") })
        blog = BlogOptionsOrFalse(false)
    }))
}
```

## 它解决什么

Docusaurus 本身是一套 npm 项目：`package.json`、`docusaurus.config.ts`、`sidebars.ts`、`src/` 里的
TypeScript。这个项目把前两层换成 Kotlin/JS：

- **配置和侧边栏是 Kotlin**：`Config`、`ThemeConfig`、`NavbarItem`、`SidebarsConfig` 等上游类型都在
  Kotlin 侧有声明，拼错的字段是编译错误，不是构建期才发现的运行时错误。
- **内容仍然是 Markdown**：放在 `src/jsMain/resources/` 下，跟着 Gradle 资源走。
- **没有手写的 npm 项目**：`package.json` 只作为版本清单（Dependabot 维护），`node_modules` 由 Kotlin/JS
  的 npm workspace 安装，`docusaurus` CLI 由 Gradle 任务调用。

## 组成

| 部分 | 作用 |
| --- | --- |
| `ktjs-docusaurus` | Kotlin/JS wrapper：站点配置、主题配置、侧边栏、preset 与插件选项的类型声明 |
| `ktjs-docusaurus-gradle-plugin` | Gradle 插件 `io.github.shijianjs.ktjs.docusaurus`：把 Kotlin/JS 模块组装成 Docusaurus 站点并运行它 |
| `ktjs-docusaurus-kit` | 可选的构建块：首页分区与目录折叠（连同它用到的浏览器 bundle） |
| [`ktjs-dom-kit`](https://github.com/shijianjs/ktjs-dom-kit) | 与 Docusaurus 无关的通用 DOM 辅助，2026-11 起独立成仓库并从 Maven Central 依赖：自定义元素基类、属性委托、shadow root 样式、类型化样式表、`loadjs` 封装 |
| `docs` | 本项目自己的文档站：`src/jsMain/resources` 下的 Markdown + Kotlin 配置，也是 GitHub Pages 的内容 |

## 用法

```kotlin
plugins {
    kotlin("multiplatform")
    id("io.github.shijianjs.ktjs.docusaurus")
}

kotlin {
    js {
        useEsModules()
        nodejs()
        binaries.library()
    }
}

docusaurus {
    sidebarsFunction = "sidebars" // 可选：Kotlin 里导出的侧边栏函数
}
```

| 任务 | 作用 |
| --- | --- |
| `docusaurusSite` | 组装站点目录（Markdown、静态资源、编译产物、配置 shim） |
| `docusaurusBuild` | 生成静态站点 |
| `docusaurusStart` | 启动 dev server |
| `docusaurusServe` | 预览上一次构建 |
| `docusaurusClear` | 清理 Docusaurus 缓存 |

站点目录默认是 `<root>/build/js/docusaurus/<模块名>`。它刻意放在 npm workspace 里面：Docusaurus 从这里
往上解析它自己的依赖，不需要第二次 `npm install`，也不需要符号链接。

Gradle 插件与配置一起发布，版本跟随上游 Docusaurus（`{上游版本}-kt.{修订号}`）。

## 安装

`release-1` 起都在 Maven Central：

```kotlin
plugins {
    kotlin("multiplatform")
    id("io.github.shijianjs.ktjs.docusaurus") version "3.10.2-kt.1"
}

kotlin {
    sourceSets.jsMain.dependencies {
        implementation("io.github.shijianjs:ktjs-docusaurus:3.10.2-kt.2")
        // 可选：首页分区与目录折叠
        implementation("io.github.shijianjs:ktjs-docusaurus-kit:0.1.0")
    }
}
```

## 开发

```bash
./gradlew build                 # wrapper、detekt、Node 测试
./gradlew :docs:docusaurusBuild # 用本站验证整条链路
./gradlew pagesSite             # docs 站点 + Dokka，GitHub Pages 的产物
```

## 文档

- `AGENTS.md`：构建约定、模块结构、上游 API 建模规则与已知取舍。
- `.agents/skills/ktjs-type-mapping/`：上游 TypeScript 到 Kotlin 类型的对照与写法，由
  [`shijianjs/dev-agent-skills`](https://github.com/shijianjs/dev-agent-skills) 分发。
