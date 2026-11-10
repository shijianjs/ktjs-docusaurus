---
sidebar_position: 1
---

# 首页分区

落地页用 MDX 写：下面的分区标签读起来就是它产出的标记，每一个都是真正的自定义元素。没有 React 页面，
也没有 `.tsx` 文件。

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

| 标签 | 属性 | 子元素 |
| --- | --- | --- |
| `ktjs-hero` | `logo-src`、`title`、`tagline` | `ktjs-action`（`kind`、`href`、`icon`）、`ktjs-badge`（`href`、`src`、`alt`） |
| `ktjs-features` | `section-title`、`tinted` | `ktjs-feature`（`icon`、`title`），正文即详情 |
| `ktjs-next-steps` | `section-title` | `ktjs-step`（`href`、`title`），正文即详情 |

## 组件之间如何配合

每个标签都是自己的自定义元素，有自己的 shadow root——父级也一样。父级渲染框架（标题、网格、hero 外壳）
并暴露一个 `<slot>`；子级渲染自己的卡片。`ktjs-hero` 按标签名把子元素分发进具名 slot，所以 MDX 里不需要
写 `slot` 属性。

也就是说，卡片不是交给某个分区对象的数据：`<ktjs-feature>` 是一个由浏览器自行升级的节点，网格是对 slot
分配元素生效的普通 CSS。

元素包用的是官方自定义元素 API：`class KtjsFeature : CustomElementAdapter()`（它继承 `HTMLElement`），
外加一个 `CustomElementReference` companion 和 `customElements.define`。`CustomElementAdapter` 在
`ktjs-dom-kit` 里，它完全不知道 Docusaurus：`connectedCallback()` 只按原生名字声明一次——子类直接
override 它。定义标签之前，`defineIfMissing` 会调用 `stripBridgesFrom`，把 Kotlin/JS 挂在原型链每层上的
自递归桥删掉；之后原生写法（`querySelectorAll`、`append`……）直接可用，不需要 `…Super` 转发（见
`AGENTS.md`）。官方的 `CustomElement` 接口是刻意不实现的：它的 `val connectedCallback` 会编译成
浏览器永远不会调用的 getter。

## 包

| 包 | 是否发布 | 内容 |
| --- | --- | --- |
| `ktjs-docusaurus-kit` | 是，带 UI bundle | Node 安全的辅助（`TocToggle`）+ 一个 `browser` 构件 |
| `ktjs-docusaurus-kit-elements` | 否 | 全部首页元素及其 shadow |
| `ktjs-dom-kit` | 否（以后搬到独立仓库） | `CustomElementAdapter`、`defineIfMissing`、`createElement`、`attachShadowRoot`、`attachStyledShadow`、`inlineStyle`、`appendStyle` |

首页 UI 跟着 kit 走：webpack bundle 以 `browser` 构件的身份挂在 kit 的坐标上，所以另一个项目只要一个依赖、
一份拷贝：

```kotlin
val browserUi by configurations.creating
dependencies {
    browserUi("io.github.shijianjs:ktjs-docusaurus-kit:<version>") {
        artifact {
            classifier = "browser"
            type = "js"
        }
    }
}

tasks.register<Copy>("stageHomeUi") {
    from(browserUi)
    into("static/ktjs-client")
}
```

元素模块本身不发布：它是本站的 UI，不是可复用的契约。

因为这些类在模块加载时就会求值 `HTMLElement`，元素 bundle 绝不能被 Node 侧 import。Gradle 插件把它发布到
`static/ktjs-client/` 下，client module 在 **hydration 之后**插入它的 `<script type="module">`：

```kotlin
// build.gradle.kts
docusaurus {
    clientModuleFunction = "onRouteDidUpdate"
    browserBundle = "ktjs-docusaurus-ktjs-docusaurus-kit-elements.mjs"
    browserBundleEntry = "registerHomeElements"
    browserBundleFiles.from(/* 元素模块的 productionLibrary/kotlin 产物 */)
}
```

hydration 之后加载意味着 React 已经渲染完这些标签，所以不会出现 hydration 不匹配。

js target 仍然按 ES2015 编译（在根构建里设置一次）。Kotlin/JS 2.4 只接受 `es5` / `es2015`；`es2020`
随 2.5 到来。

## 链接

`href` 写**相对页面**的路径——`docs/intro`，不是 `/docs/intro`。这样同一份 MDX 在每种语言前缀下
（`/my-site/` 与 `/my-site/zh-Hans/`）都能用，不必去解析 base URL。

## 图标

`icon` 的值是 Iconify 名字（`lucide:sparkles`、`simple-icons:github`）。元素通过
[`ktjs-iconify`](https://github.com/shijianjs/ktjs-iconify) 包装 `<iconify-icon>`——`registerIconifyIcon()`
引入官方包，字形在运行时拉取。

库 bundle 会把 `import 'iconify-icon'` 留成裸模块名，所以站点在 bundle 旁边放上 npm 的 ESM 文件，并在
`headTags` 里声明一份 import map：

```kotlin
// docusaurus.config.kt
headTags = arrayOf(
    unsafeJso<HtmlTagObject> {
        tagName = "script"
        attributes = /* type = "importmap" */
        innerHTML = """{"imports":{"iconify-icon":"/my-site/ktjs-client/iconify-icon.mjs"}}"""
    },
)
```

## 样式

每个元素都渲染进 shadow root，所以页面样式表进不来，这些规则也出不去。主题变化靠继承的自定义属性穿过
边界：

```css
:root {
  --ktjs-accent: #1f6feb;
  --ktjs-accent-strong: #1a5fd0;
  --ktjs-card-bg: var(--ifm-background-surface-color);
}
```

每个 `--ktjs-*` 都回退到 Infima 的 `--ifm-*` 变量，所以亮色与暗色模式都可用，而 shadow 树里一个
`[data-theme]` 选择器都不需要。

样式写在元素自己身上，只有三个层级：

| 位置 | 放什么 | 例子 |
| --- | --- | --- |
| 元素构造函数里的 `inlineStyle.apply { … }` | 一条普通声明能表达的全部 | `display = Display.flex`、`padding = Padding(1.5.rem)`、`color = accent` |
| 该元素自己 shadow root 里的 `<style>` | 内联表达不了的：`:hover`、`::before`、`@media`、`@supports` | 按钮的悬停配色、hero 的两个光斑 |
| 被 adopt 的 sheet（`attachStyledShadow`） | 多个元素确实共享的 | `HomeReset.kt` 里的 `box-sizing` reset |

取值都是有类型的（`Display.flex`、`1.5.rem`、`Color("var(--ktjs-accent, …)")`、`clamp(…)`），不手拼字符串。
`ktjs-dom-kit` 提供两个机制：`HTMLElement.inlineStyle`，把元素自己的 `CSSStyleDeclaration` 看成
`csstype.Properties`；`ShadowRoot.appendStyle`，往该元素的 shadow 树追加一个 `<style>`。因此组件的规则不会
飘进公共文件，也没有哪个元素背着它并不使用的样式表。

## 从 Kotlin 用

在代码里搭一个分区用的是同一批元素——创建、设属性、append：

```kotlin
val hero = KtjsHero.NAME.createElement {
    setAttribute("logo-src", "img/ktjs-docusaurus-logo.png")
    setAttribute("title", "my-project")
}
hero.appendChild(
    KtjsAction.NAME.createElement {
        setAttribute("kind", "primary")
        setAttribute("href", "docs/intro")
        textContent = "Get started"
    },
)
host.appendChild(hero)
```

`TagName<T>.createElement { }` 是 `document.createElement(TagName<T>)` 的类型安全包装；回调拿到的是具体的
元素类型。
