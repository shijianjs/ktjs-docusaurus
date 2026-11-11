# 用类型化的规则对象写 `CSSStyleSheet`

本文件是 `ktjs-dom-kit` 的内部说明：`cssSheet { … }` 怎么用、为什么这样实现、哪里会踩坑。
模块将来会搬到独立仓库，这份文档跟着它走（届时要翻译/整理）。

可运行的对照实验在 `ktjs-docusaurus` 仓库的 `probes/web-components`：`example.CssSheetExperiment`
（三种写法并排 + 一张 adopt 了 sheet 的卡片 + 字符数报表），`./gradlew :probes-web-components:jsBrowserDevelopmentRun`。

## 1. 它解决什么

样式最终要变成 `CSSStyleSheet` 里的一段文本。手写文本的问题不是长，而是**魔法字符串的范围**：
属性名、数值单位、伪类结构都靠人拼，写错只在下一次肉眼看样式时才发现。

`cssSheet` 收的是 csstype 的规则对象（`PropertiesBuilder`，和 `inlineStyle` 用的是同一个 `Properties`），
序列化交给 **emotion 自己的文本管线**：

```kotlin
internal val CARD_SHEET: CSSStyleSheet = cssSheet {
    cardClass {                                   // 选择器用 ClassName，见 3.1
        border = Border(1.px, LineStyle.solid, var(cardBorderHook, NamedColor.black))
        transition = many(Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease))
        "&:hover" { borderColor = var(accentHook, var(primaryColorHook)) }
    }
}
```

同一段规则手写约 455 个字符的 CSS；上面这种写法里手写的 CSS 文本是 0，剩下的字符串只有自定义属性名
（主题契约）和一个 `"&:hover"` 键（等上游 issue 修复，见 4.1）。

## 2. 管线与契约

```
PropertiesBuilder (= JSO)
    │  serializeStyles(jsArrayOf(rules))            @emotion/serialize：声明 + 嵌套 key{…}
    ▼
"border:1px solid …;transition:…;:hover{…}"        （还不是扁平规则）
    │  serialize(compile("selector{…}"), middleware(jsArrayOf(stringify)))   stylis
    ▼
".card{…}.card:hover{…}"                            → CSSStyleSheet.replaceSync(…)
```

要点：

- **顶层是选择器映射**，不是声明集合。`cardClass { … }` / `"&:hover" { … }` / `":host" { … }` /
  `"@media …" { … }` 是 key；写在列表里的属性属于最近一次 `{}` 的那条规则。
- `serializeStyles` 的参数是**数组**（emotion 里是 tagged template 的 `strings`）：`serializeStyles([rules])`。
  传对象本身会去读 `args[0]`，结果是空串。
- 只挂 `stringify`，不挂 `prefixer`：后者会加 `-webkit-` / `-ms-` 前缀，emotion 11 已经不做这件事。
- 版本钉的是 `@emotion/cache` 自己的 pin：`@emotion/serialize` 1.3.3 + `stylis` 4.2.0。
- 序列化在**浏览器侧**发生（`CSSStyleSheet` 在 Node 里不存在），所以带 sheet 的模块只能进浏览器包。

## 3. 写法清单

### 3.1 选择器：`ClassName` + `invoke`

`RuleBuilder` 有 `operator fun ClassName.invoke(block)`，点号由它补：

```kotlin
private val cardClass = ClassName("card")   // 也可复用作 element.className

cssSheet { cardClass { … } }
```

坑：`ClassName("card") { … }` **编译不过**——会被解析成「调用工厂 + 尾随 lambda 当第二个实参」。
一次性用可以加括号 `(ClassName("card")) { … }`；要复用就存成 `val`。

伪类、伪元素没有类型化常量（上游的 `hover {}` 糖缺 `&`，见 4.1），直接写字符串 key 但**必须带 `&`**：

```kotlin
"&:hover" { … }
"&:hover, &:focus-visible" { … }        // 逗号列表 stylis 会展开
"&::before" { … }
```

**at-rule 有官方入口，不要手写 `"@media …"` / `"@supports …"` 字符串**：

```kotlin
`@media`(MediaType.screen and maxWidth(576.px)) { … }   // csstype.RuleBuilder.@media

// SupportsQuery 仿 web.cssom.MediaQuery：声明两侧都类型化，and/or/not 组合
supports(
    supportsDeclaration(webkitBackgroundClipName, BackgroundClip.text) or
        supportsDeclaration(PropertyName.backgroundClip, BackgroundClip.text),
) { … }
```

官方已经给的（IDE 里查符号时优先找这些，别再自己拼）：

| 入口 | 在哪 | 用途 |
| --- | --- | --- |
| `RuleBuilder.@media / @container / @font-face / @view-transition` | `kotlin-csstype/.../RuleBuilder.kt` | at-rule 块，`@media(query)` 收 `MediaQuery` |
| `AtRules.media / supports / layer / page / scope / …` | `web.cssom.AtRules` | 所有 at-rule 关键字常量（`@JsValue("@media")` 等） |
| `MediaQuery.and / or / not` | `web.cssom.MediaQuery.kt` | 组合条件 |
| `MediaType.screen / print / all` | `web.cssom.atrule.MediaType` | 媒体类型 |
| `maxWidth / minWidth / width / height / orientation / prefersColorScheme / prefersReducedMotion / …` | `web.cssom.atrule/*.fun.kt` | 媒体特性（一整个目录，约 30 个） |
| `var / env / rgb / many / array / important / linearGradient / stop / Content() / NamedColor / PropertyName / translatex` | `web.cssom` 的 `*.functions.kt`、`*.ext.kt`、`*.fun.kt` | 取值与函数 |

**目前确实还是字符串的**：伪类/伪元素键（等 #2933）、`:host` 与 `*` 这类选择器、应用级自定义属性名。
`@supports` 的条件已经不再是字符串——cssom 只缺类型，我们在 `css/Supports.kt` 里照 `MediaQuery` 补了一层
（见 3.4）。

### 3.2 自定义属性：具名常量 + `var(name, fallback)`

```kotlin
private val accentHook = CustomPropertyName<Color>("--ktjs-accent")
private val shadowHook = CustomPropertyName<BoxShadow>("--ktjs-shadow")

color = var(accentHook, NamedColor.white)                       // var(--ktjs-accent,#FFFFFF)
boxShadow = var(shadowHook, BoxShadow(0.px, 10.px, 24.px, …))
```

类型参数是 hook 承载的值类型，`var` 的兜底值因此也必须同类型——兜底值是**类型化的值**，不是文本。
cssom 没有应用级属性名的常量，字符串在这里出现一次；具名常量避免它散落多处。

### 3.3 命名色、多值、过渡

| 需要 | 写法 |
| --- | --- |
| 命名色 | `NamedColor.white`（`@JsValue("#FFFFFF")`，类型就是 `Color`） |
| 任意色 | `rgb(127, 127, 127, 0.24)`、`oklch(…)`、`Color("var(--x)")`（最后手段） |
| 逗号多值 | `many(a, b)`；空格多值 `array(…)`；`!important` 用 `important(x)` |
| 过渡 | `Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease)`，多个用 `many(…)` |
| 回退阶梯 | `fallback(Display.grid, Display.flex)`（序列化成两条同名声明） |
| 阴影/边框/内边距 | `BoxShadow(0.px, 10.px, 24.px, rgb(…))`、`Border(1.px, LineStyle.solid, …)`、`Padding(…)` |

`PropertyName.borderColor` 能当过渡属性名，是因为 `PropertyName<T> : Ident`，而 `IdentType` 里包含
`TransitionProperty`——所以 `TransitionProperty` 只暴露 `all` 并不妨碍使用，**不需要 cast**。

### 3.4 cssom 没覆盖的部分：在 `io.github.shijianjs.ktjs.dom.css` 里补

不要因为「cssom 没有」就把整条规则退回文本，也不要让 `unsafeCast` 散到调用点。缺什么就在本模块的
`css` 包里按官方风格（`unsafeCast` 拼一次、值仍然类型化）补一个，调用点照常写类型：

| 缺的东西 | 补在哪 | 调用点形态 |
| --- | --- | --- |
| `radial-gradient(…)` | `css/Gradients.kt` | `radialGradient(RadialGradientShape.Circle, colorStop(glow), stop(Color.transparent, 70.pct))` |
| 无位置的颜色停靠点 | `css/Gradients.kt` | `colorStop(color)`——cssom 的 `stop` 总要带位置 |
| `-webkit-background-clip`（属性 + 名字） | `css/VendorProperties.kt` | `webkitBackgroundClip(BackgroundClip.text)`、`webkitBackgroundClipName` 给 supports 条件用 |
| `pointer-events: none` | `css/Values.kt` | `pointerEventsNone`（`PointerEvents` 生成出来只有 SVG 那批值） |
| `@supports` 条件类型 | `css/Supports.kt` | `SupportsQuery` + `supportsDeclaration(PropertyName<T>, T)` + `and/or/not`，照 `web.cssom.MediaQuery` 的形态 |
| `@supports` 块（唯一缺的 at-rule helper） | `css/AtRules.kt` | `supports(query) { … }`，关键字取 `AtRules.supports` |

补的规矩：**值仍然类型化**（枚举、工具类型、`Length`），只有「属性名」这种没法类型化的东西才写成字符串；
一个缺口一个函数或常量，不要提供 `putRaw(name, value)` 这类通用逃生口——那等于把字符串又请回来。

## 4. 坑（都是实测过的）

### 4.1 伪类糖缺 `&`，不要用

`SimplePseudosRuleBuilder.hover` / `before` / `after` 写出的 key 是 `":hover"`、`"::before"`，没有 `&`。
stylis 会把它当成**后代**选择器：

```
hover {}      -> .probe-sugar :hover{…}     ← 不是 .probe-sugar:hover
"&:hover" {}  -> .probe-ampersand:hover{…}
```

根因在 kotlin-wrappers 的生成器：`karakum/csstype/Pseudos.kt` 的 `convertSimplePseudo` 把 csstype TS
联合里的字符串原样写进方法体，而同一套 API 里手写的 `RuleBuilder.and` 会加 `&`、`kotlin-css` 的
`CssBuilder.hover` 也一直写 `"&:hover"`。已报 <https://github.com/JetBrains/kotlin-wrappers/issues/2933>，
修好后这里的 `"&:hover"` 可以换成 `hover {}`。

### 4.2 不要用 CSSOM 逐条 `insertRule` 代替序列化

那条路（`insertRule(".x {}")` + `rule.style` 赋值）也能拿到类型检查，但 CSSOM 对**同一规则里同一属性
只保留最后一个声明**：`fallback(a, b)` 阶梯会只剩最后一条。文本路径能保留阶梯，由浏览器解析时取舍。

### 4.3 产物与手写文本不是逐字相同

比对时先归一化，别断言整串：`#fff` → `#FFFFFF`、`0` → `0px`、逗号后可能没有空格、
`-webkit-background-clip` 与 `background-clip` 在 Chrome 里是同一属性的别名，文本里两条都在、从 CSSOM
读回来只剩一条、
`transition: border-color 0.2s ease` 里的 `ease` 在 CSSOM 回读时会消失（文本路径不会）。
等价即可；要断言就断语义片段（见 `ktjs-docusaurus-probes` 里的用例）。

### 4.4 一份 sheet 服务所有实例

`cssSheet` 返回的 `CSSStyleSheet` 放进**组件类的 companion**，所有实例 `attachStyledShadow(HOME_RESET, SHEET)`。
不要在构造函数里现建：规则是静态的，一份就够。

## 5. 落地状态（2026-11-11）

| 文件 | 状态 |
| --- | --- |
| `HomeReset.kt` | ✅ `cssSheet`（`*` / `*::before` / `*::after` / `:host`） |
| `KtjsFeature.kt`、`KtjsStep.kt` | ✅ `cssSheet`（卡片表面 + `&:hover` / `&:focus-visible`） |
| `KtjsAction.kt` | ✅ `cssSheet`（`:host > a` + 两个 pill 类 + 悬停态） |
| `KtjsHero.kt` | ✅ `cssSheet`（伪元素光斑 + `@supports` 渐变字 + `@media`；用到 `css` 包的 `radialGradient` / `webkitBackgroundClip`） |

迁移一个组件时的步骤：先把内联表达不了的那几条规则搬进 `cssSheet { }`，跑一次
`jsBrowserTest`，再把 sheet 读出来（`element.shadowRoot.adoptedStyleSheets`）与原来的文本做语义比对
（注意 4.3 的规范化差异），确认等价后删掉文本。

## 6. 参考

- csstype 规则 DSL：`kotlin-csstype/src/webMain/kotlin/csstype/RuleBuilder.kt`（`Selector`/`ClassName`/`and`）、
  `.../generated/csstype/Simple*Pseudo*RuleBuilder.kt`（伪类糖，注意 4.1）
- cssom 取值类型：`kotlin-cssom-core/src/webMain/generated/web/cssom/`（`*Property.kt` 的类型、
  `*.functions.kt` 的工厂：`Border`、`BoxShadow`、`Transition`、`Gradient`、`Color`、`NamedColor`）
- cssom 手写辅助：`Reference.functions.kt`（`var`/`env`）、`many.kt`、`array.kt`、`important.kt`
- 序列化：`@emotion/serialize` 的 `serializeStyles`，`stylis` 的 `compile`/`serialize`/`middleware`/`stringify`
- 对照实验与结论：`ktjs-docusaurus/probes/web-components`（`CssSheetExperiment`）
