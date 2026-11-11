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

## 属性是响应式的

表里每个属性都是一个 Kotlin 委托（`ktjs-dom-kit` 的 `attr`），不是只在连接时读一次的字段：

```kotlin
class KtjsHero : CustomElementAdapter() {
    @JsName("logoSrc")
    var logoSrc: String by attr("logo-src") { change -> /* 用 change.newValue 重绘 */ }

    @JsName("titleText")
    var titleText: String by attr("title") { change -> headingElement.textContent = change.newValue }
}
```

- getter 读属性、setter 写属性，所以没有第二份状态要同步：`hero.titleText = "x"` 与
  `hero.setAttribute("title", "x")` 落到同一处，外部写入后 getter 立刻能读到。
- 值确实变了才回调——不管变化来自属性、`setAttribute` 还是 devtools。写回同样的值不算变化，删除属性算。
- 在 `connectedCallback()` 里调一次 `applyInitialAttributes()`：标签上原本就有的标记解析不会经过 setter，
  这是回调第一次得知它们的途径。它是幂等的，元素重新连接不会重绘两遍。
- 编解码器跟着属性类型走：`String`、`Int`、`Double`、`Boolean`（存在性语义，和 `hidden` 一样）是内置的；
  `kind` 用 `enumAttributeCodec(ActionKind.entries)` 演示自定义类型的扩展点。`default = …` 是属性缺失时
  getter 报告的值，**不会**写进标记——除非 `applyInitialAttributes(writeDefaults = true)` 明确要求。
- 属性名默认是属性名的中划线形式（`logoSrc` → `logo-src`）；两者不同时显式指定（`titleText` → `title`，
  因为 `HTMLElement` 已经占用了 `title`）。这些属性都带 `@JsName`：不加的话 Kotlin/JS 只会把委托属性编译成
  名字被混淆的访问器方法，JS 侧根本看不到这个属性；加上才有稳定的 `titleText` / `logoSrc`。

两个连带结论：

- **不要在构造函数里给元素自己设属性。** 构造完就带属性时 `document.createElement("ktjs-hero")` 会抛
  `NotSupportedError`（解析器和升级路径不会）。hero 的 `display: block` 因此写成 `:host` 规则。
- 外部写入在微任务里上报——元素用 `MutationObserver` 观察自己，因为规范里的 `observedAttributes` 得在
  `customElements.define` 时重复列出每个属性，那时委托还没创建。Kotlin 侧的写入是同步上报的。

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
| `ktjs-dom-kit` | 否（以后搬到独立仓库） | `CustomElementAdapter`、`attr` 与 `AttributeCodec`、`defineIfMissing`、`createElement`、`attachShadowRoot`、`attachStyledShadow`、`inlineStyle`、`cssSheet` / `cssText` |

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
| 该组件自己的一份 sheet（`cssSheet { … }`，放在它的 `companion object` 里） | 内联表达不了的：`:hover`、`::before`、`@media`、`@supports` | 按钮的悬停配色、hero 的两个光斑 |
| 所有元素都 adopt 的 sheet | 所有元素都需要、或好几个确实共享的 | `HomeReset.kt` 里的 `box-sizing` 与 `:host { display: block }` |

取值都是有类型的（`Display.flex`、`1.5.rem`、`Color("var(--ktjs-accent, …)")`、`clamp(…)`），不手拼字符串。
`ktjs-dom-kit` 提供两个机制：`HTMLElement.inlineStyle`，把元素自己的 `CSSStyleDeclaration` 看成
`csstype.Properties`；`attachStyledShadow(sheet…)`，挂 shadow root 并把给定的 sheet adopt 掉。

组件自己的规则是**每个类一份 `CSSStyleSheet`**，建在该类的 `companion object` 里、该组件的所有实例共用——
就是 Lit 的 `static styles` 那套，只是放在 Kotlin 存类静态成员的地方。所以永远不会创建 `<style>` 节点：
一百张卡片共用一份已解析的 sheet，而不是各自背一份同样的文本。组件的规则因此不会飘进公共文件，也没有哪个元素背着它并不使用的样式表。

规则本身写成类型化对象，而不是 CSS 文本：

```kotlin
private val SHEET: CSSStyleSheet = cssSheet {
    cardClass {
        border = Border(1.px, LineStyle.solid, var(cardBorderHook, rgb(127, 127, 127, 0.24)))
        transition = many(Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease))
        Pseudos.hover { borderColor = var(accentHook, var(primaryColorHook)) }
    }
}
```

`ktjs-dom-kit` 用 emotion 缓存自己那条管线（`@emotion/serialize` + `stylis`）把它序列化，所以进 sheet 的
就是 emotion 会产出的东西：属性名、取值、单位、伪类结构都来自类型，剩下的字符串只有主题的自定义属性名；
伪类变体走同一个 `css` 包的 `Pseudos` + `pseudo` / `pseudos`。cssom 没建模的东西也放在这个包里，
而不是退回字符串：
`radialGradient` / `colorStop`（cssom 只有线性与角度渐变）、`webkitBackgroundClip`（前缀属性缺失）、
`pointerEventsNone`（取值联合里只有 SVG 那批）。包括 hero 在内，每个元素都用同一种方式写 sheet。

## 从 Kotlin 用

在代码里搭一个分区用的是同一批元素——创建、设属性、append：

```kotlin
val hero = KtjsHero.NAME.createElement {
    logoSrc = "img/ktjs-docusaurus-logo.png"
    titleText = "my-project"
}
hero.appendChild(
    KtjsAction.NAME.createElement {
        kind = ActionKind.Primary
        href = "docs/intro"
        textContent = "Get started"
    },
)
host.appendChild(hero)
```

属性写入的就是同名 attribute，所以这样得到的就是 MDX 产出的那个元素。`TagName<T>.createElement { }` 是
`document.createElement(TagName<T>)` 的类型安全包装；回调拿到的是具体的元素类型。
