# AGENTS.md

本文件记录跨会话需要保留的项目约定：构建方式、模块结构、发布流程和容易踩的坑。只与某个目录强相关的
说明放在该目录下的 `AGENTS.md`，这里保留指向它的简短索引。

## 本地开发环境

- 仓库按 `<workspace>/<owner>/<repo>` 存放；目录约定、自有项目清单与可参考的第三方克隆见上级目录的
  `AGENTS.md`，查资料前先读它。
- 上游资料一律读本地克隆，**不要访问 `raw.githubusercontent.com`，也不要用网页抓取 GitHub**：本机所在的
  国内网络访问这些域名经常超时。
- 本地还没有需要的仓库时，按 `gh repo clone <owner>/<repo> <workspace>/<owner>/<repo>` 克隆后再读，
  不要逐个抓网页。

## 项目概况与模块结构

`ktjs-docusaurus` 用 Kotlin/JS 写 Docusaurus 站点：配置与侧边栏是 Kotlin，页面是 Markdown，构建由
Gradle 插件驱动。Kotlin/JS 模块名、`wrapper-revisions.properties` 的 key 与目录名三者必须一致。

- `ktjs-docusaurus`：发布模块。Docusaurus 的每个 npm 包在这里只是一个 Kotlin 包（`config`、`theme`、
  `sidebars`、`presets`、`plugins/contentdocs` …），不按 npm 包拆 Gradle 模块。
- `ktjs-docusaurus-gradle-plugin`：Gradle 插件 `io.github.shijianjs.ktjs.docusaurus`，是**独立构建**，由
  `settings.gradle.kts` 的 `pluginManagement { includeBuild(...) }` 接入，所以 `docs` 能把它当普通插件用。
  它不在 root 构建的子项目里：`./gradlew build` 不编译它，`includeBuild` 在配置阶段按需编译。
- `docs`：本项目的文档站（Markdown + Kotlin 配置），不发布、不进 Dokka，是 GitHub Pages 的内容。
  站点内容在 `src/jsMain/resources/docs`，分四块：用户指南、文档工具箱（kit）、DOM 工具箱（dom-kit）、
  开发文档（本仓库自己的）。
- `scaffold/module`：模板骨架，保留占位符，不参与构建。

Docusaurus 全部包 lockstep 发版（`3.10.2` 一次发齐），所以整个生态只产生一个 Maven 版本；这也是不按
npm 包拆模块的原因。

### 上游版本与版本号

上游版本只写根 `package.json`（`@docusaurus/core`、`@docusaurus/preset-classic`、`@docusaurus/types`、
`prism-react-renderer`）。wrapper 与插件的版本都由它推导：`{版本}-kt.{修订号}`。插件自己的
`build.gradle.kts` 直接读 `../package.json` 与 `../wrapper-revisions.properties`，因为它是独立构建，
拿不到 root 的 `extra[...]`。

本地克隆 `meta/docusaurus` 是 **4.0.0（未发布）**，npm `latest` 是 3.10.2。写声明前以 `npm pack` 取回的
3.10.2 `.d.ts` 为准，不要把源码树的 4.0 行为写成断言（`NavbarItem` 的类型列表、`future` 的字段名都有
差异）。

### Gradle 插件与站点组装

- 站点目录固定放在 `<root>/build/js/docusaurus/<模块名>`，**不要**挪到模块自己的 `build/` 下：
  Docusaurus 从站点目录向上解析 `@docusaurus/*`，而 npm workspace 的 `node_modules` 在
  `<root>/build/js/`。放在别处就得第二次 `npm install` 或建符号链接。
- 生成的 `docusaurus.config.mjs` / `sidebars.mjs` 只 import 相对路径的 Kotlin 产物
  （`./generated/<模块>.mjs`）；Kotlin 运行时与其它 klib 也是相对 import，不依赖 node_modules。
- 生成的站点 `package.json` **不能**写 `"type": "module"`：Docusaurus 往 `.docusaurus/` 写 CJS bundle 并按
  CJS 加载，加上它会让 SSR 报 `require.resolveWeak is not a function`。shim 用 `.mjs` 扩展名，本来就是 ESM。
- `docusaurusSite` 只依赖 `jsProductionLibraryCompileSync`（以及 executable 的对应任务）：只依赖
  `compileKotlinJs` 拿到的是 klib，没有 `.mjs`；同时依赖 development 链接任务会让 Kotlin 插件报
  `jsNodeProductionLibraryDistribution` 读了另一个任务的输出。输入目录也只列 `compileSync` 下的产物，
  不要列 `build/dist/js/...`（那是 `jsNode*Distribution` 的输出）。依赖用 `tasks.matching {}` 挂在注册块里，
  不要用 `configureEach` 去 configure 另一个任务（Gradle 会报 mutation guard）。
- 导出函数名可配：`docusaurus.configFunction`（默认 `docusaurusConfig`）与
  `docusaurus.sidebarsFunction`（默认不生成 `sidebars.mjs`）。Kotlin 侧用 `@JsExport` 导出，返回
  `Config` / `SidebarsConfig`。

### 已覆盖的范围与明确不做的部分

当前覆盖「写一个文档站需要的配置面」，并且**官方插件与主题全部覆盖**（口径见
<https://docusaurus.io/docs/api/plugins> 与 <https://docusaurus.io/docs/api/themes>）：

- `Config` 全字段（含 `future`、`i18n`、`storage`、`markdown`、`webpack`）；
- `preset-classic` 的 `ClassicOptions`；
- `ThemeConfig`（导航栏、页脚、代码高亮、颜色模式、目录、Algolia、Mermaid、live code block）；
- `SidebarsConfig` 与全部侧边栏条目类型；
- `prism-react-renderer` 的 `themes`（`PrismThemes`）。

官方插件（npm 包 → Kotlin 包）：`plugin-content-docs` → `plugins.contentdocs`、`content-blog` →
`contentblog`、`content-pages` → `contentpages`、`sitemap` → `sitemap`、`svgr` → `svgr`、
`google-analytics` → `googleanalytics`、`google-gtag` → `gtag`、`google-tag-manager` → `gtm`、
`client-redirects` → `clientredirects`、`ideal-image` → `idealimage`、`pwa` → `pwa`、
`rsdoctor` → `rsdoctor`、`css-cascade-layers` → `csscascadelayers`、`vercel-analytics` →
`vercelanalytics`。`plugin-debug` 没有选项（上游签名只收 `LoadContext`），所以只有一个
`DebugPlugin.NAME` 常量，注册时直接 `PluginConfig(DebugPlugin.NAME)`。

官方主题：`theme-classic` → `plugins.themeclassic`（`customCss`）＋ `ThemeConfig` 里 classic 与
`theme-common` 的字段；`theme-search-algolia` → `ThemeConfig.algolia`；`theme-live-codeblock` →
`ThemeConfig.liveCodeBlock`；`theme-mermaid` → `ThemeConfig.mermaid`。主题的配置面就是 `themeConfig`
里的字段，所以只有 classic 需要额外一个插件选项类型。

`vercel-analytics` 不在上面两个索引页里，但包是官方的（3.6 起发布），一并包了。上游标为 legacy 的
`plugin-google-analytics`（Universal Analytics，Google 已停）仍然包着——它还在发布、preset 仍然接受它。

尚未覆盖，需要时按需补：

- `@docusaurus/core` 的运行时导出（`build` / `start` / `serve` / `swizzle` …）：Gradle 插件直接调 CLI，
  Kotlin 侧暂时用不到；
- 主题组件与 `@theme/…` 别名（React 组件与 hooks）：wrapper 的 `client` 包已经把 `Layout`、`Heading`、
  `Link`、`Translate`、`useDocusaurusContext` 等声明好了（kotlin-react），但**站点默认不写 tsx 页面**。
  首页这类「静态为主 + 少量动态」的页面用 MDX：页面里写 `<ktjs-…>` 标签，由 client module 升级成
  kit 的分区（见下文「页面用 MDX」）。`client` 包留给确实需要组件树的人；
- 第三方主题与插件（`@easyops-cn/docusaurus-search-local` 等）的选项类型：各自在自己的 Kotlin 包里加
  （`plugins.searchlocal` 就是一份完整示例）。这类包由生成的配置**按名字**解析，Kotlin 侧不 import 它们，
  所以还要在 `ktjs-docusaurus/build.gradle.kts` 里 `implementation(npm(...))`，否则站点在 `build/js` 下
  找不到它。注意搜索那个包是**主题**（`@theme/SearchBar` / `@theme/SearchPage`），要放进 `Config.themes`；
  Docusaurus 3.10 在没有 `type: 'search'` 导航条目时会自己渲染 `@theme/SearchBar`。索引是 `postBuild`
  产物，落在 `<site>/build/search-index.json` 与 `<site>/build/<locale>/search-index.json`。
- Gradle 插件的 Maven 发布尚未接进 `release.yml`（它是独立构建，需要单独一条发布命令）。

### 页面用 MDX，不用 tsx

首页这类「静态为主 + 少量动态」的页面用 `src/jsMain/resources/pages/index.mdx`，标签是**真正的自定义
元素**：`<ktjs-hero>` / `<ktjs-features>` / `<ktjs-next-steps>`，类在 `ktjs-docusaurus-kit-browser` 里，
`connectedCallback` 读属性与子标签、建一次分区、丢掉声明式子节点（`data-ktjs-read` 防重）。做法与
duckfn 站点一致。

现在的实现是「JS 桥接 + client module 注册」，两条硬约束都有实测依据：

1. **Kotlin 类不能继承 `HTMLElement`，两级都会炸**：

   - 默认 ES5 target 下编译成 `function X() { HTMLElement.call(this); }`，DOM 构造函数只接受 `new`，
     浏览器抛 `Please use the 'new' operator`。根构建已给所有 js target 设了
     `compilerOptions { target = "es2015" }`（只有 `js { compilerOptions { target = "es2015" } }` 这种
     写法有效；`freeCompilerArgs.addAll("-target", "es2015")` 放 extension 上无效）。
   - 到 es2015 类是真的了，但 Kotlin 会给**继承来的 external 成员生成自递归转发方法**：

     ```js
     querySelector(selectors) { return this.querySelector(selectors); }
     ```

     `connectedCallback` 里调 `querySelector` / `querySelectorAll` 就 `Maximum call stack size exceeded`
     （`getAttribute` 之类没被覆盖，所以症状取决于回调里第一句调的是什么）。2026-11 在 Kotlin 2.4.21 +
     es2015 上实测。

     官方示例原样（`override val connectedCallback`）其实连回调都不会被调用——Kotlin 把它编译成
     `get connectedCallback()` 访问器，浏览器只认方法，于是元素渲染出来但回调静默不执行。改成
     `@JsName("connectedCallback") fun` 后才会触发上面那条栈溢出。两种失败都在
     `probes/web-components` 里可复现（`<my-panel>` 与 `<my-panel-fn>`）。

   **本仓库的解法**：`ktjs-dom-kit` 里 `CustomElementAdapter : HTMLElement()` 用官方写法，注册用
   `defineIfMissing(name, reference)`——它在 `customElements.define` 之前调用 `stripBridgesFrom`，沿
   原型链删掉编译器生成的自递归桥（`Reflect.deleteProperty`，删到 `HTMLElement.prototype` 之前为止；
   注意 Kotlin 给链上每个类都生成一层桥，只删父类不够），之后元素里直接用原生拼写
   `querySelectorAll` / `append` / `animate` / `focus`，不需要 `…Super` 转发。生命周期钩子声明成
   `connectedCallback()`——元素类只写 `override fun connectedCallback()`，不要再起 `onConnected`
   之类的别名，也不必每个类带 `@JsName`。**没有任何 JS 类字面量**。细节与实测在
   `probes/web-components` 的 `CustomElementAdapter.kt` / `MyPanelAdapter.kt`。编译器侧的解释在上游
   <https://github.com/JetBrains/kotlin-wrappers/issues/2932>：桥只为外部**接口**上 `= definedExternally`
   的成员生成，名字与目标同名，所以调用解析回自己；JVM 后端有 `isErroneousSpecialBridge`（KT-82651）
   防护，JS 后端还没有。

   **分层是硬约定**：kit 按「Node 是否加载」拆成两个模块（kit 发布、浏览器那一半不发布），另有一个
   通用的 DOM 模块：

   - `ktjs-docusaurus-kit`：Node 安全的辅助（目前是 TOC toggle 的组装），不知道 MDX、标签名或自定义
     元素；它自己只按字面量拼标签名，从不 import 浏览器模块。它也**承载浏览器那一半的产物**：`stageBrowserBundle`
     把 webpack 输出放到 `build/generated/browser-bundle/ktjs-client/`，该目录作为 `jsMain` 的 resource
     root，于是 bundle 以 `ktjs-client/ktjs-docusaurus-kit-browser.js` 出现在发布的 klib 里，和编译产物同在
     一个坐标、同一个版本。`srcDir(task)` 只贡献目录不建立依赖，`jsProcessResources` 必须显式
     `dependsOn(stageBrowserBundle)`；`check` 里再挂一个解压 klib 校验该条目的任务，因为「拷贝缺失的源」
     是静默无操作。**Kotlin 块注释可嵌套**：KDoc 里写路径通配（`ktjs-client/**`）会让 `/**` 再开一层注释，
     该注释之后到文件尾全被吃掉、任务静默不注册——症状是脚本末尾的 `throw` 都不执行，别再往注释里写 `/**`。
   - `ktjs-docusaurus-kit-browser`：浏览器专属——全部自定义元素：`ktjs-hero` / `ktjs-action` /
     `ktjs-badge` / `ktjs-features` / `ktjs-feature` / `ktjs-next-steps` / `ktjs-step`，以及
     `ktjs-toc-toggle`。每个元素都渲染进自己的 shadow root：父级负责框架和 `<slot>` 布局，子级自己
     渲染卡片，子标签是真元素，不再被解析成数据后丢弃。它是 `executable`（不是 library）：没人消费
     它的 Kotlin API，而 executable 的链接才会跑 webpack 把 npm 依赖（`iconify-icon`）内联进一个
     `.js` 文件。顶层 `class X extends HTMLElement` 会被 Node 求值，所以它**不能**进 `docs` 站的
     依赖图。**不发布**：这是本站的 UI，不是通用契约，其他项目从 kit 的 klib 里取那个 `.js` 资源使用。
   - `ktjs-dom-kit`：通用 DOM 基类（`CustomElementAdapter`）、挂 shadow root 的 `attachShadowRoot` /
     `attachStyledShadow(vararg CSSStyleSheet)`，以及 `HTMLElement.inlineStyle`（`csstype.Properties`
     视图）、`loadjs` 加载器的封装（`dom.loadjs` 包：四种调用形态写成 `invoke` 扩展重载 + bundle
     注册表），与 Docusaurus 无关。模块名不以 `ktjs-docusaurus` 开头，因此不参与发布和 Dokka；元素包用
     `implementation` 依赖它，产物作为独立 `.mjs` 一起进 `static/ktjs-client/`。**固定留在这个仓库**：
     它的文档要用它所帮助构建的那套框架来写，拆出去会让站点依赖这个库、库的文档又依赖站点，绕成环。
     文档在站点的 `dom-kit` 分类下（`docs/src/jsMain/resources/docs/dom-kit/`）。

    **组件的属性用 `attr` 委托**（`ktjs-dom-kit`）：`var titleText: String by attr("title") { change -> … }`。
    属性是唯一状态——getter 读 attribute、setter 写 attribute，不另存字段；外部 `setAttribute` 由
    `CustomElementAdapter` 的 `MutationObserver` 在微任务里上报，`connectedCallback` 里调一次
    `applyInitialAttributes()` 把解析好的标记喂给回调（幂等）。默认值只读、不自动写进 HTML，需要写入时
    显式 `applyInitialAttributes(writeDefaults = true)`。属性名默认字段名转小写中划线，原生拼写
    （`tabindex`、`title`）要显式给名。四条实测约束：委托属性**必须**带 `@JsName`，否则 Kotlin/JS 只生成
    混淆名的访问器方法，JS 侧根本看不到该属性；回调里只改 shadow 内容、不要再写属性；构造函数里不要给
    元素自己写 attribute（会让 `document.createElement` 抛 `NotSupportedError`，宿主样式写 `:host` 规则）；
    **setter 必须容忍字符串**——React 19 把自定义元素的每个同名 JSX prop 直接赋给 property
    （`<ktjs-action kind="secondary">` → `element.kind = "secondary"`），所以委托把字符串按 attribute 原文
    写入，只有属性自身类型的值才过 codec。漏掉这条的症状很隐蔽：整页加载（Hydrate 只对账、不重设 prop）
    正常，**SPA 路由切回首页**（React 客户端建 `<ktjs-*>`）时在 `enumAttributeCodec.encode` 抛
    `ClassCastException`，错误边界把整页换成 "This page crashed."；`SiteE2eTest` 用客户端导航覆盖它。

    **样式按组件拆分，就近写**（2026-11 重构，别再改回集中式 CSS）：元素自己的样式用
    `inlineStyle.apply { display = Display.flex; padding = Padding(1.5.rem) }` 写在渲染它的那段代码旁，
    类型是 kotlin-csstype/cssom 的值（`Display.flex`、`1.5.rem`、`Color("var(--ktjs-accent, …)")`、
    `clamp(…)`），不拼字符串。只有内联表达不了的才写 CSS——`:hover` / `:focus-visible`、`::before` /
    `::after`、`@media`、`@supports`、`@keyframes`。

    这部分规则写成**该组件类的一份 `CSSStyleSheet`**，放在**该类的 `companion object` 里**
    （统一叫 `private val CSS` + `private val SHEET`，与 `NAME` 并列，第一次用到时 `replaceSync` 一次），
    由该组件的**所有实例** adopt：`attachStyledShadow(HOME_RESET, SHEET)`。因为名字在各自 companion 里，
    每个元素类都可以用同一对名字，不必带 `FEATURE_` / `HERO_` 之类前缀。这是 Lit `static styles` 的做法（Kotlin 没有
    `static`，companion 就是类的静态成员所在处），也是唯一该用的做法——**不要每个实例建一个 `<style>`
    节点**（20×20 的单元格就是 400 份重复文本 + 400 次解析）。CSS 文本用缩进写的 `"""` 加 `.trimIndent()`
    （companion 里的 `const val` 不能调 `trimIndent()`，所以用 `private val`）。类名只留给这些规则选中的节点。

    跨组件真正共享的也走同一份 sheet：目前是 `HomeReset.kt` 的 `box-sizing` 与 `:host { display: block }`
    （`display` 不能写在构造函数里——给 `this` 赋 `style` 会加属性，`document.createElement("ktjs-…")` 会抛错），
    主题色这类跨组件常量放 `HomeTheme.kt`。`ktjs-features` / `ktjs-next-steps` 共用的分区外壳
    （`HomeSection.kt`）属于「父容器统一控制子组件布局」，是允许提取的那一类。

    **组件自己的 CSS 用 `ktjs-dom-kit` 的 `cssSheet { … }` 写**：类型化的规则对象交给 emotion 的文本管线
    序列化。写法、坑、参考与示例都在站点的 DOM 工具箱分类里
    （`docs/src/jsMain/resources/docs/dom-kit/css-sheet.md`，中文版在对应的 `i18n/zh-Hans/` 下）。典型形态：

    ```kotlin
    private val cardClass = ClassName("ktjs-feature-card")   // 与 shadow 里的 classList 共用

    private val SHEET: CSSStyleSheet = cssSheet {
        cardClass {
            border = Border(1.px, LineStyle.solid, `var`(cardBorderHook, rgb(127, 127, 127, 0.24)))
            transition = many(
                Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease),
                Transition(PropertyName.boxShadow, 0.2.s, TransitionTimingFunction.ease),
            )
            Pseudos.hover { borderColor = `var`(accentHook, `var`(primaryColorHook)) }
        }
    }
    ```

    四条规矩（都是实测踩出来的）：

    ① 选择器用 `ClassName` + `invoke`（点号由它补）。Kotlin 的解析规则要注意：`ClassName("x") { … }`
      会被读成「调用工厂 + 尾随 lambda 当第二个实参」，一次性用加括号 `(ClassName("x")) { … }`，
      要复用就存 `val` 再 `cardClass { … }`。
    ② 伪类 / 伪元素用 `ktjs-dom-kit` 的 `css/Pseudos.kt`：`Pseudos` 列全 64 个选择器（每个带 `&`），
      `Pseudos.hover { … }` 或 `pseudos(Pseudos.hover, Pseudos.focusVisible) { … }`（后者用
      `many` 拼成一个块）。前者靠 `context(RuleBuilder<T>)` + `operator Pseudo.invoke`（Kotlin 2.2+
      context parameters，本仓库 2.4 无需开关；调用点要 `import …dom.css.invoke`），效果与官方
      `hover {}` 相同但带 `&`。不要用 `hover {}` 糖：它缺 `&` 会被 stylis 当成后代选择器，而且就算上游
      <https://github.com/JetBrains/kotlin-wrappers/issues/2933> 修好，也表达不了「多个状态共用一个块」；
      所以不等它，自建一层（`TagName` + `HtmlTagName` 的形态）。at-rule 别写字符串：
      `` `@media`(MediaType.screen and maxWidth(576.px)) `` 是 csstype `RuleBuilder` 自带的 helper，
      `supports(query)` 取 `AtRules.supports` 关键字（dom-kit）；条件里 `MediaQuery.and/or/not`、
      `MediaType`、`web.cssom.atrule` 里约 30 个媒体特性工厂都是类型化的。`@supports` 的条件同样类型化：
      `SupportsQuery`（dom-kit `css/Supports.kt` 仿 `MediaQuery` 补的）配
      `supportsDeclaration(PropertyName<T>, T)` 与 `and/or/not`，别再传条件字符串。
    ③ 值全走类型：`NamedColor.white`（命名色就是 `Color`，别写 `"#fff"`）、`rgb(r, g, b, a)`、
      `1.5.rem`、`BoxShadow(0.px, 10.px, 24.px, …)`、`var(hook, fallback)`、`fallback(grid, flex)`、
      `Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease)` 配 `many(…)` 拼多值。
      过渡属性名不需要 cast：`PropertyName<T>` 继承 `Ident`，而 `IdentType` 里包含 `TransitionProperty`。
      自定义属性名（`--ktjs-*`、`--ifm-*`）在 `HomeTheme.kt` 里各是一个具名 `CustomPropertyName<T>` 常量，
      字符串每处只写一次。
    ④ cssom 没覆盖的部分**在 `ktjs-dom-kit` 的 `io.github.shijianjs.ktjs.dom.css` 包里补**，不要退回
      文本，也不要把 `unsafeCast` 撒到调用点。已有的补丁：`radialGradient` + `colorStop`（cssom 只有
      线性/角度渐变）、`webkitBackgroundClip`（`-webkit-` 属性没有）、`pointerEventsNone`（值联合缺
      `none`）。补的时候值仍然类型化，一个缺口一个函数，不要提供通用的 `putRaw(name, value)`。

    **通栏要按页宽算，并在页面根上夹掉横向溢出**：Docusaurus 在 ≥1440px 时把 `.container` 封顶在
    `--ifm-container-width-xl`（`.container--fluid` 的 `max-width: inherit` 被后面的媒体查询覆盖），
    所以按内容容器撑开的通栏在宽屏上会停在 1320px、铺不满页面——必须写 `width = 100.vw`、
    `marginInline = 50.pct - 50.vw`。代价是 `100vw` 含桌面端竖向滚动条的槽，比可见区域宽十几像素，
    站点 `custom.css` 因此有 `html { overflow-x: clip }`（用 `clip` 不用 `hidden`：`hidden` 会建滚动
    容器，粘性导航栏与目录就废了）。把 `overflow-x: clip` 写在元素自己身上没用——溢出的是盒子本身。
    回归用例：`fullBleedSectionsSpanTheViewportNotTheContentColumn`，以及 `SiteE2eTest` 的
    `the home page fills the window and does not overflow horizontally`（1600px 视口，覆盖容器封顶
    那种情况）。

    同一段卡片规则：手写约 455 个字符的 CSS，这套写法里手写 CSS 文本 0、cast 0，只剩自定义属性名和
    零个伪类键（走 `Pseudos`）。判断值不值得用这套，看的是「还剩多少 CSS 文本要手写」，不是源码行数。
    对照实验（三条写法并排 + 字符数报表）留在 `probes/web-components` 的 `example.CssSheetExperiment`：
    `./gradlew :probes-web-components:jsBrowserDevelopmentRun`。另两套已否决：`kotlin-css`
    （`kotlinx.css`）的旧 DSL（值不校验、`transition` 补 `0s`、`&` 不解析）、逐条 `insertRule` 灌 CSSOM
    （`fallback()` 阶梯会被压成最后一条）。


   不要往 kit 里加 `fillFrom` 这类解析方法，也不要为它引入 DOM 抽象接口。

   自查：`grep 'HTMLElement.call' <bundle>.mjs` 应为 0；`grep 'querySelector(selectors) {' <bundle>`
   不应出现。

2. **注册放在 hydration 之后**：静态 `<script>` 写在 `<head>` 会早于 hydration，元素先升级就让 React 报
   hydration error #418。现在由 `ClientModule.onRouteDidUpdate` 动态插入一次
   `<script type="module" src="<baseUrl>ktjs-client/index.mjs">`，脚本延迟到 hydration 后才加载并调用
   `registerKtjsElements()`。这里的 `<baseUrl>` 必须是 **Docusaurus 为当前 locale 解析出来的那个**
   （默认语言 `/my-site/`、中文 `/my-site/zh-Hans/`）：插件生成的 `client-module.mjs` 从
   `@generated/docusaurus.config` 读它并作为第一个参数传给 `clientModuleFunction`。写成编译期常量时
   `docusaurus start --locale zh-Hans` 会踩坑——dev server 只按该 locale 的前缀伺服，脚本 404，整页
   元素都不升级（构建产物里反而不炸，因为 `static/` 同时也拷在站点根）。

插件两条通道：

| 通道 | 生成 | 谁加载 | 能放什么 |
| --- | --- | --- | --- |
| `clientModuleFunction` | `client-module.mjs` | `clientModules`（浏览器 + Node prerender） | 不继承 DOM 的代码；第一个参数是当前 locale 的 `baseUrl` |
| `browserBundle` + `browserBundleEntry` + `browserBundleFiles` | `static/ktjs-client/index.mjs` | hydration 后动态插入的 `<script>` | 自定义元素等浏览器专属代码 |

- `browserBundleFiles` 必须与 `additionalJs` 分开：后者也会拷进 `generated/`，若浏览器包里带了共享模块
  的副本，会覆盖 config bundle 的那份，webpack 报 `'X…' is not exported from …`。
- `docusaurusSite` 必须显式 `dependsOn(":ktjs-docusaurus-kit-browser:jsBrowserDistribution")`：那是
  executable 的 webpack 产物目录，库的 `jsProductionLibraryCompileSync` 不含内联后的 npm 依赖。
- 浏览器入口的 `<script src>` 要写**带 baseUrl 的绝对路径**（`/my-site/ktjs-client/index.mjs`），
  相对路径在深页面上会解析到 `.../docs/intro/ktjs-client/...`；这个 baseUrl 取
  `clientModuleFunction` 的第一个参数，不要用编译期常量。
- `href` 写**相对页面**的路径（`docs/intro`），中英文 locale 前缀都能解析；不要写 `/docs/intro`。
- 文案在 MDX 里，翻译放 `i18n/<locale>/docusaurus-plugin-content-pages/index.mdx`；不要为了 i18n 把
  文案塞回 Kotlin。
- 确实需要组件树时（交互复杂、props 要计算），再引入第二个模块 + `pageFunctions`。

### 等 Kotlin 2.5 再改的两件事

两件都在 2.5.0-Beta2 的 EAP 里，正式版发布后统一处理，涉及 `ktjs-*` 全系列（本仓库、vaadin、echarts、
iconify、wrapper-template）：

- **`target = "es2020"`**：2.4 只接受 `es5` / `es2015`（写 `es2020` 直接报 `Unsupported ECMA version`）。
  2.5 新增 es2020，还顺带打开 `BigInt` 这类此前需要额外开关的特性。现在五个仓库里写死的
  `target = "es2015"` 到时候一起换 —— 它只是「能表达 `class extends HTMLElement` 的最低档」，不是终点。
- **`KotlinPublicationFormat.KOTLIN_ARCHIVE`**：KMP 库的平台构件打成单个 `.kar`，正对着 Maven Central 的
  发布体积限制（官方例子：7539 文件 / 91 MB → 1036 文件 / 23 MB）。发布侧仍是 Experimental，消费侧已
  对所有项目可用。开启方式：模块里 `kotlin { publishing { publicationFormat.set(...) } }`，或全局
  `gradle.properties` 写 `kotlin.publicationFormat=KOTLIN_ARCHIVE`。

在那之前不要为了「用新语法」去调 target：es2015 之上的收益只有语法糖，而 `es2020` 会直接让构建失败。

### Kotlin/JS 的 DCE 与跨模块代码

Kotlin/JS 的 library 链接**默认开 IR DCE，入口只有该模块自己的 `@JsExport`**。没有任何 `@JsExport` 的
模块（wrapper 正是这种）产出的 `.mjs` 会被削成一百多字节，别的模块对它的任何引用在运行时都是
`undefined`（症状：`... is not a function`、`Cannot read properties of undefined`、webpack 报
`'X…' is not exported from …`）。

实测（临时探针函数 `dceProbe`）：

| 场景 | 产物大小 | 探针是否保留 |
| --- | --- | --- |
| 无 `@JsExport` | 106 B | 否 |
| 有 `@JsExport fun x() = dceProbe(...)` | 303 B | 是（被导出根引用到） |
| `-Xir-dce=false` | 45,725 B | 是 |

所以根构建给**每个 KMP 子模块**统一加 `freeCompilerArgs.add("-Xir-dce=false")`。必须逐个模块加：每个模块
独立链接，只给 wrapper 加而 `docs` 不加，`docs` 自己的 public 声明仍会被削掉。库无法保证自己一定被打进
bundle，而消费方打包时本来就会摇树，所以这个开关对 `executable` 消费方没有体积代价。

两个连带约束：

- 依赖模块的 `.mjs` 由它自己的链接任务产出，站点组装前必须先链接它们。`docusaurusSite` 只依赖本模块的
  链接任务，跨项目的依赖要显式
  `dependsOn(":ktjs-docusaurus:jsProductionLibraryCompileSync", ":ktjs-docusaurus-kit:jsProductionLibraryCompileSync")`，
  否则站点目录里会拷到上一次的空产物。
- 页面组件 import 的 `@theme/…` 只在 webpack 里可解析，所以它们必须待在另一个模块里，不能进 config
  bundle（`docusaurus.config.mjs` 由 Node 加载）。插件的 `clientModuleFunction` / `pageFunctions` /
  `pageBundle` / `additionalJs` 就是给这种「第二个 bundle」准备的。

### 上游 API 建模约定

wrapper 的目标是让上游 npm 包暴露的公开 API 在 Kotlin/JS 侧都能用，而不是只覆盖几个常用组件。动手前先
看上游源码仓库（本地克隆优先，不要反复用网络请求抓源码）：包列表、每个包的入口文件、导出列表，以及
`custom-elements.json` 一类的 manifest（若有）就是范围清单。按项目约定排除收费/私有包后，其余全部要。

- 完整度：组件、mixin、静态成员、模块级函数与变量、事件、属性名常量、主题入口（CSS 文件路径、
  `registerStyles` / `unsafeCSS` 等）和图标集注册函数都要在 Kotlin 侧可用。
- 每个 npm 包对应一个同名 Kotlin 包，文件放在 `src/jsMain/kotlin/<包路径>/`。这是包划分，不产生新的
  Gradle 模块。包很大时继续按上游目录拆分文件，不要把所有声明堆进一个文件。
- mixin 用 `external interface` 建模，继承关系照抄上游；元素类声明为
  `abstract external class XxxElement : web.html.HTMLElement, MixinA, MixinB`，类体只重复自己声明的
  成员。上游官方框架绑定（如 Java/Flow 组件）如何组合这些能力，可以作为建模参考。
- 调用方应能把实例赋给任一能力接口（如 `val field: InputFieldMixin = textField`），因此不要把 mixin
  成员摊平进元素类，也不要退化成 `dynamic`。
- 元素类不能 `final`：Kotlin/JS 不允许外部类实现接口。静态成员放进
  `private external interface XxxStaticMembers`，再用同名 `object` 暴露为普通函数。
- 成员与 `HTMLElement` 同名且类型一致时省略（由 DOM 提供）；同名但类型不同（如 `hidden`、
  `autocapitalize`、`inputMode`）加 `Property` 后缀并用 `@JsName` 映射回原名。只有来自 mixin 或生成
  父类的成员才加 `override`。
- 模块级函数/变量导出拆成两个文件：`XxxExports.kt`（`@file:JsModule` + 只含 external 声明）与
  `XxxValues.kt`（公开 object 的属性访问器），避免同名变量在同一包内冲突。
- 有 manifest 时优先生成而不是手抄。生成文件加 `// Generated by ... Do not edit.` 标记，重跑脚本时
  删除带标记的旧文件；没有 manifest、只能手写的包在脚本里列成专门集合，重跑时不覆盖。
- 修改生成器后重新生成并跑浏览器测试；升级上游版本先刷新 npm 依赖（如 `./gradlew kotlinNpmInstall`），
  让 manifest 更新后再生成。

### 类型映射与 `dynamic`

上游 TypeScript 声明了类型的成员必须映射成 Kotlin 类型，`dynamic` 只保留给 Kotlin 无法表达语义的情况：

- `any`、`unknown`、`object`、manifest 里缺类型的成员：`kotlin.js.JsAny`，可能为 `null` 时用
  `kotlin.js.JsAny?`。用 `JsAny` 而不是 `dynamic`，调用与赋值才会被编译器检查。
- 数组、集合、字典、日期、Promise 映射到对应类型：`Array<...>`、`js.collections.JsSet<...>`、
  `js.objects.Record<...>`、`kotlin.js.Date`、`kotlin.js.Promise<T>`。
- 回调按上游签名展开成 Kotlin 函数类型；manifest 只写 `function` 而上游 `.d.ts` 有签名的，按 `.d.ts`
  手工补上。
- manifest 里只有名字、没有结构信息的具名类型，在对应包内手写 `XxxTypes.kt`（`external interface` /
  `typealias`，不带生成标记），生成器通过别名表引用，重跑生成不会丢。
- 联合类型映射为不透明 `external interface` + 每个成员一个同名工厂函数，照搬 kotlin-wrappers 的
  `web.http.BodyInit` 写法（`external interface BodyInit` + `BodyInit(value: String)` 等）。例如
  `string | number` → `StringOrDouble`，`HTMLElement | string` → `HTMLElementOrString`，调用方显式包装。
  同一个联合按 Kotlin 包去重，写进生成的 `XxxUnions.kt`；联合里出现 `JsAny` 时直接收敛为 `JsAny`。
- 只有确实没有任何类型信息时才保留 `dynamic`（例如上游只写裸 `Function`，且实现与 `.d.ts` 都无法推断
  签名）。能按实现补签名的，优先在生成器的签名覆盖表里补上，不要用 `dynamic` 表示「懒得写类型」。
- 除联合类型外，`external interface` 不要写 `: JsAny` 父类型：external 类型本身就属于 `JsAny`，多写
  一层只是噪声。字符串字面量用 `sealed external interface` + `Xxx(String)` 工厂 + `Xxxs` 常量对象；
  CSS 语义的字段优先用 `web.cssom` / `web.canvas` 现成类型；普通对象用 `js.objects.unsafeJso`，
  不要自造 builder 或用 `js("({})")`。

`kotlin.js.JsAny` 需要 `kotlin.js.ExperimentalWasmJsInterop` opt-in；在根构建脚本里对所有 KMP 子模块
统一添加 `compilerOptions { optIn.add("kotlin.js.ExperimentalWasmJsInterop") }`。

### 类型安全封装指南

上游类型到 Kotlin 类型的完整对照与写法整理在技能 `ktjs-type-mapping`，本仓库已安装到
[`.agents/skills/ktjs-type-mapping/`](.agents/skills/ktjs-type-mapping/SKILL.md)（入口 `SKILL.md`，
完整规范在其 `references/type-mapping.md`）：联合类型的同名工厂模式、字符串字面量的
`sealed external interface` + 常量对象、`web.cssom` / `web.canvas` 值类型、`js.objects.unsafeJso`、
上游 mixin 与 Kotlin 公共基接口的取舍、`JsAny` / `dynamic` 的边界、上游 `Omit<...> & { ... }` /
`data` 收窄 / 元组 / `T | "none"` 这些 Kotlin 表达不了的写法的固定处理方式、包与文件划分、落地检查
清单和反模式速查。新增或修改 wrapper 声明前先读它。

该技能由 [`shijianjs/dev-agent-skills`](https://github.com/shijianjs/dev-agent-skills) 统一维护，
所有 `ktjs-*` 仓库共用同一份。**不要在本仓库新建或手改副本**：要改内容就改 skill 仓库，再在本仓库执行
`npx skills update ktjs-type-mapping` 让副本与 `skills-lock.json` 同步；本机没有该技能时先执行
`npx skills add shijianjs/dev-agent-skills --skill ktjs-type-mapping`。

`dynamic` 只留给「上游自己也没写类型、实现也无法推断」的情况；上游写了 `any` / `unknown` 才用 `JsAny`，
且要在 KDoc 里注明「上游类型是 `unknown`」。退化到这两者之前，先按 `ktjs-type-mapping` 技能第 12 节过一遍。

### 新增 Kotlin 包或模块

上游新增一个 npm 包时，先判断它是否与 Docusaurus lockstep 发版：是就把它作为**新的 Kotlin 包**加进
`ktjs-docusaurus`（`plugins/<包名>` / `theme` / …），不要新建 Gradle 模块。只有上游出现独立发版节奏的
依赖时才走下面的模块流程。

1. 复制 `scaffold/module` 为 `ktjs-<name>`，替换文件里的所有占位符（包名、npm 包名、项目名）。
2. `settings.gradle.kts` 加 `include(":ktjs-<name>")`。
3. `package.json` 的 `dependencies` 加 npm 包版本；`wrapper-revisions.properties` 加 `ktjs-<name>=1`。
4. `docs/build.gradle.kts` 的 `jsMain.dependencies` 加 `implementation(project(":ktjs-<name>"))`，
   并在 `docs` 站里实际使用它。
5. 全局替换时同步检查 `detekt`、Dependabot 分组、README 模块表与 Dokka（Dokka 按前缀自动收集，无需改）。

模块名、`wrapper-revisions.properties` 的 key、目录名三者必须完全一致，否则配置阶段会失败。

## 浏览器 DOM 类型体系

统一使用 JetBrains `kotlin-browser`（`org.jetbrains.kotlin-wrappers:kotlin-browser`）的 `web.*` 类型，
不要在公开 API 中混用 `org.w3c.dom.*`（Kotlin/JS 自带）或 `kotlinx.browser.*`：

- DOM 元素：`web.html.HTMLElement`
- 全局 document：`web.dom.document`
- 创建元素：`document.createElement(<Project>TagName.xxx)`，无需 `unsafeCast`
- 所有自定义元素标签名集中在根包的单个对象里，每个元素一个
  `inline val xxx: TagName<XxxElement>`；不要在组件包里各自声明 `XxxTagName`，同一标签名只能有一处声明。

两套类型运行时指向同一个对象，但编译器不视为同一类型；wrapper 的公开 API 一旦用 `org.w3c.dom.*`，
kotlin-browser 项目就无法直接使用。

`build` / `check` 会执行 Detekt 的 `ForbiddenImport` 规则：根脚本为所有子模块应用 `dev.detekt` 并分析
`src`，根目录 `detekt.yml` 禁止 `org.w3c.dom.*` 与 `kotlinx.browser.*`。新增模块无需单独配置。该规则
只拦截 import，代码里直接写全限定名（如 `kotlinx.browser.window`）不会被发现。

Detekt 版本固定为 `2.0.0-alpha.6`：1.23.8 在 JDK 25 下直接失败，而 2.0 alpha 明确支持 Kotlin 2.4 /
Gradle 9 / JDK 25。升级时注意 2.0 的插件 ID 是 `dev.detekt`，规则配置项也已改名
（如 `imports` → `forbiddenImports`）。

## Kotlin/JS 模块格式与测试运行时

- 所有 `js` 目标统一 `useEsModules()`，新增模块沿用，不要退回 CommonJS/UMD。klib 不携带 moduleKind，
  消费方在链接时自行选择格式；这里的选择只影响本仓库的测试产物与 `docs` 站的 bundle。
- `@JsModule` 声明必须按 ESM 语义书写：`@file:JsModule` 的顶层函数/类会生成具名导入，安全；但顶层
  `external val` 配 `@JsName` 会生成 default import，对只有具名导出的包必须写成 `@JsModule` +
  `external object` + 具名属性。
- **可调用的模块导出（导出的是函数对象，属性挂在同一个函数上，如 `loadjs`）分两半写**：把 `operator fun
  invoke` 声明在 `external interface` 上**不会**编译成调用——Kotlin/JS 生成的是 `obj.invoke(...)`
  属性查找，运行时 `TypeError: ….invoke is not a function`（2026-11 在 Kotlin 2.4.21 + es2015 上实测）。
  正确写法：成员留在接口上（`@JsName("default") external val loadjs: Loadjs`），调用声明成
  `internal external fun`（同样 `@JsName("default")`；参数用 `Any?` 配 `definedExternally` 默认值原样
  透传，省略的参数才会是 `undefined` 而不是 `null`），对外的可调用性由**扩展** `operator fun
  Loadjs.invoke(...)` 提供，调用点照旧写 `loadjs(...)`。接口上不要再留 `invoke` 成员：成员优先于扩展，
  会又退回属性查找。
- 同一个 `@JsName("default")` 只保留**一份**声明：声明两份时编译器只给其中一份混淆后的别名
  （`default_0`），另一份引用成裸 `default`（保留字），webpack 直接报
  `Module parse failed: Unexpected token`。函数对象那种「成员 + 调用」的拆分正好落在两份声明里，所以调用
  声明要一份吃下所有形态，靠参数类型（`Any?`）透传。
- ESM-only 的 Node 包（`exports` 里没有 `require`/`default` 条件）要导入具体子路径，例如
  `@scope/package/lib/index.js`，不要写包名；子路径对 CommonJS 与 ESM 链接都可用。
- 浏览器测试使用新版测试 DSL：`@OptIn(ExperimentalJsTestDsl::class) browser { test { chromium() } }`，
  由 Playwright 驱动并按需安装浏览器。Karma 已废弃，不要新增 `testTask { useKarma { ... } }`。
- Playwright 默认从官方 CDN（`cdn.playwright.dev`）下载 Chrome / FFmpeg，国内经常 0 速度。本机可把下载源
  指到国内镜像：设环境变量 `PLAYWRIGHT_DOWNLOAD_HOST=https://npmmirror.com/mirrors/playwright/`
  （**就用这个地址**；`https://cdn.npmmirror.com/binaries/playwright` 看似可用、实测会直接报错，别用）。
  先 `./gradlew --stop` 让新 daemon 继承变量，再跑安装任务，实测 20 多秒装完。
  `kotlinInstallPlaywrightBrowsers` 是 `DefaultTask`、内部用 `ExecOperations` 起 node，任务本身不接受
  `environment` 配置，所以只能靠环境变量注入，不能用 `init.d` 脚本改任务。**只在本机生效，不要提交到仓库，
  也不要写进工作流——CI 走官方源。**
- `docs` 的端到端套件用 **JVM 版 Playwright**（照 `ktjs-vaadin`）：`docs` 加 `jvm()` target，
  `src/jvmTest` 里 `@UsePlaywright` + `OptionsFactory`，测试 JVM 内嵌静态服务器直接服务
  `:docs:docusaurusBuild` 的产物。**不引入 TS/Node 测试层，也不给 Playwright 写 TS wrapper**；
  「不同步 duckfn 的 playwright 测试」当初说的是不搬它那套 TS 封装、其它轻量思路可参考，不是不做测试。
- `e2eTest` 是 `verification` 组里的独立 `Test` 任务，`jvmTest` 置 `enabled = false`，所以
  `build` / `check` 与 CI 都不跑 e2e；改完站点本地按需 `./gradlew :docs:e2eTest` 核对。
- e2e 的静态服务器用 JDK `HttpServer` 自己控制 MIME：Docusaurus 的元素入口是 ES module，
  `.mjs` 返回 `application/octet-stream` 会被浏览器拒绝执行（症状是元素全部不升级、shadow 为空）。
  它同时剥掉 `/ktjs-docusaurus/` baseUrl 前缀，因此不必复制站点产物。
- Playwright Java 的版本必须与 KGP 内置 npm Playwright 一致（2.4.21 → `1.61.0`），才能共用
  `kotlinInstallPlaywrightBrowsers` 装的那份 Chromium；CI 的 `npx playwright install-deps chromium`
  只装系统依赖，浏览器仍由该任务下载。
- 大文件或网络盘上 Gradle 的文件监听可能漏掉改动，`compileTestKotlinJs` 会误报 UP-TO-DATE；本机加
  `-Dorg.gradle.vfs.watch=false` 强制重新检测即可（同样只是本机 workaround，不进仓库）。
- Node 侧的 `jsNodeTest` 同样按 ESM 产物运行。
- 根构建脚本用 `NpmExtension.override("webpack", ...)` 固定一份 webpack：npm workspace 会为
  `source-map-loader` 的 peer dependency 装第二份，测试包只引到其中一份时构建会报
  `The 'compilation' argument must be an instance of Compilation`。

## 测试覆盖

目标是「除明确不做的功能外，覆盖全部 wrapper 代码」，完整度按下面五条判定：

1. **覆盖全部 Kotlin 代码**：`src/jsMain` 里每个包都要有对应测试目录，覆盖公开 API——工厂/注册函数、
   标签名常量、attribute / event 常量、静态入口对象、mixin 类型归属、同名属性改名映射、不透明类型与
   联合类型边界、模块级导出与手写包。只有「明确放弃的功能」允许没有用例。
2. **优先翻译官方测试**：上游源码树里每个包的 `test/*.test.js` 是首选用例来源。Kotlin 测试目录与子模块、
   包名对齐（`src/jsTest/kotlin/<包>/`），文件名与上游文件对应（`<name>.test.js` → `<Name>Test.kt`），
   测试函数名对应上游 `it(...)`，KDoc 注明上游文件路径。上游的测试辅助库用模块内等价的辅助函数替代
   （挂载、下一帧、等待更新、等待 resize、超时、按键、事件监听、输入、点击、shadow 查询、计算样式等），
   不要在用例里重复实现。
3. **按锁定版本写断言**：上游源码树可能比 `package.json` 锁定的 npm 版本新（例如源码树是
   `25.4.0-alpha4`、锁的是 `25.3.4`），属性名与默认值会不一样（`headingLevel` vs `titleHeadingLevel`、
   哪些属性反射到 attribute、哪些属性默认是 `undefined`）。写用例前先用 `custom-elements.json` 核对锁定
   的版本，KDoc 里注明差异，不要把源码树的行为当成断言。
4. **上游没覆盖、或 wrapper 独有的部分必须补测**，数量不少于上游同类用例；**明确放弃的功能**要写进项目
   自己的说明，避免反复讨论。常见放弃项：视觉/截图测试、shadow DOM 结构快照、依赖私有成员（`_foo`、`$`）
   的内部测试、上游模板语法专用的入口（如 Lit 指令）、需要真实键鼠序列（`sendKeys`、指针拖拽）或真实
   文件上传的用例。
5. **异步用例**：Kotlin/JS 的 `@Test` 函数直接返回 `Promise<Unit>`，不要 sleep，也不要引入协程测试库。
6. **测试里的 DOM 访问走类型安全 API**：优先用 kotlin-browser 的 `web.dom` 类型（`element.shadowRoot`、
   `querySelector`、`getAttribute`、`textContent`）和测试辅助里的 `shadowQuery` / `shadowQueryAll` /
   `shadowQueryNode` / `query` / `queryAll`，不要在 `js(...)` 里手写 DOM 查询——测试代码同样受编译期检查，
   这是对 wrapper 类型的一次校验。`js(...)` 只留给确实没有类型包装的地方（浏览器 API、受保护成员、
   对象字面量 / 事件 init）。注意 `Element.id` 的类型是 `ElementId` 而非 `String`，且强转到
   `HTMLButtonElement` 之类的原生接口会在运行时校验（自定义元素会抛 CCE），要按需改用 `getAttribute` /
   `hasAttribute`。

**测试发现 bug 时修 wrapper，不改断言**：能定位到生成器的问题就修生成器并重新生成，提交信息里说明。
上游新增或修改测试时同步翻译，保持用例名可追溯。

## UI 编程约定

以下为维护者的通用 UI 编码偏好。新增任何 UI 代码（Web Component 包装、演示页）都按此执行。

### 保留模式：对原生 DOM 做面向对象封装

不要引入 React / Vue / Lit 的「状态 → render → 重建 DOM」模型。组件应表现为持有内部 DOM 对象的普通类，
按以下优先级裁决（上面的赢）：

1. 保留 DOM 节点：节点长期存在，不随状态重建。
2. 类/对象字段持有 DOM 引用。
3. 直接修改 DOM：property / attribute / text / class / 事件监听。
4. 只做局部更新。
5. 必要时才替换局部集合。
6. 禁止整组件 re-render。
7. 禁止用响应式 state 驱动 render。
8. 禁止引入 Lit / React / Vue 等 UI runtime 或其设计模式。

### 禁止拼 HTML 字符串

不许 `innerHTML = "..."`、`insertAdjacentHTML`，也不许任何「模板字符串生成标记再塞进 DOM」的写法。
节点用 `document.createElement`（Kotlin/JS 中优先用 kotlin-browser 的类型化 API）逐个构造并组装。

### DOM 用对象操作，字段持有引用

需要反复访问的节点一律存成字段，直接对字段调方法；不要用 `querySelector` 当组件内部的状态管理方式。
`querySelector` 只允许用于「从外部挂载点找目标」这类不属于组件自身结构的场景。

### 优先用类型化 DOM 属性，`setAttribute` 只作兜底

绑定提供了类型化属性时，直接给属性赋值，不要用 `setAttribute` 传字符串：

- 标准属性：`element.id = ElementId("status")`、`button.type = ButtonType.button`、
  `element.className = ClassName("chart")`、`element.hidden = true`、`element.title = "..."`。
- ARIA：`element.role = "img"`、`element.ariaLabel = "..."`、`element.ariaLive = "polite"`。
- 内联样式：`element.style.width = "400px"`、`element.style.height = "300px"`，不要拼一整条
  `style` 字符串。
- 只有绑定里没有类型化包装的属性（`data-*`、自定义 attribute、上游组件声明的 attribute）才用
  `setAttribute` / `removeAttribute`。

这样属性名和值类型都由编译器检查；`setAttribute` 里的字符串拼错不会报错。演示页和测试辅助代码同样
适用这条规则。

### 更新局部、直接、明确，不设通用刷新机制

状态变化只改相关的那几个节点，用命名的领域 setter 表达（如 `setResult` / `setLoading`），而不是把整个
UI 当成 data 的函数重算。用标准 DOM API 直说：`textContent`、类型化属性赋值、`classList.add/remove/toggle`、
`hidden`、`disabled`、`value`、`href` / `src` 赋值等；只有找不到类型化属性时才退回 `setAttribute` /
`removeAttribute`。

禁止实现 `render()` / `rebuild()` / `rerender()` 这类通用刷新入口；禁止因为一个字段变化就重建整个子树；
禁止用「替换子树」模拟响应式更新：

```ts
set data(value) { this.#data = value; this.render(); }        // 禁止
update(data) { this.replaceChildren(); this.render(data); }   // 禁止
```

### `replaceChildren()` 分级使用

它本身不是禁品，禁的是把它当整组件的通用 render 机制：

- 允许：确实需要整体更新的局部集合（一次性静态组装、整批新条目、固定的「图标 + 文本」组合）。
- 禁止：清空组件自己的根子树再重建。

变长列表能增量就增量：按数据长度增删条目、复用已有条目对象，只有条目语义整体失效时才整批替换。

### 内容入口：命名的领域 setter

组件不接受整份 data blob，也不提供通用 `render` / `apply` / `update` 入口。每个组件暴露一组领域 setter，
一个方法只碰它负责的那部分节点：

- setter 只做 mutation；变长集合按新长度增删条目、复用已有对象。
- setter 只写自己持有的节点，因此在元素尚未连接、甚至尚未插入文档时调用也安全。
- 不做 attribute reflection：不把 setter 的值镜像回 attribute。
- 新增字段就加对应 setter，不要回头引入 `setData()` / `update()`。

**例外（attribute 种子）**：由构建期插件生成、没有挂载点可调 setter 的元素，可在 `connectedCallback()`
里从 attribute 读一次作初始种子；这与「读取用于初始化」不冲突，仍属保留模式。手写挂载的元素仍走 setter。

### 生命周期：构造时建树

- 静态结构优先在 `constructor()` 中创建并组装完毕，`connectedCallback()` 只做必要的一次性初始化，
  不要把 `connectedCallback() → rebuild() → build()` 当标准生命周期。
- Custom Elements 规范限制：构造函数里不得给 `this` 加属性或子节点（`this.append(...)`、
  `this.setAttribute(...)` 都不行）；`attachShadow()` 合法，所以结构挂进 shadow root，light DOM 保持为空。
- 监听器按归属决定挂在哪：挂在自有节点上的写在构造函数里，无需清理；挂在 `document` / `window` /
  `matchMedia` 等外部对象上的，必须在 `disconnectedCallback()` 里移除，注册用幂等标志
  （元素移动或框架重挂载会再次触发 `connectedCallback()`）。

### 状态挂在宿主元素上，不用外部容器

「跟着某个 DOM 元素走」的状态（分区实例、一次性消费标记）直接存在元素自身：一个 `external
interface` 声明私有字段，`unsafeCast` 过去读写，元素被移除时状态随之释放。

外部 `MutableMap<HTMLElement, …>` 会把每个宿主都强引用住——文档站每次进首页都新建一批元素，导航几次
就攒下一堆永不回收的实例。`connectedCallback` 需要「找到自己上次建的那个分区」时，用元素上的字段而不是
全局索引（见 `ktjs-docusaurus-kit-browser/.../HomeElementRegistry.kt` 的 `HomeHostState`）。

### 组件用 class，但不建通用组件基类

一个组件一个类，自带字段、构造时组装、自己的 setter；不做 `create()` / `apply()` 之类的模板方法抽象，
那会把状态驱动刷新重新引进来。非元素的小部件（折叠控件、卡片等）同样是 class；函数式导出只留给真正的
纯工具。

### 默认 Shadow DOM

- 组件默认渲染进 `shadow root`（`mode: 'open'`），样式通过 `adoptedStyleSheets` 或 shadow 内样式注入，
  避免与宿主页面互相污染。
- 主题靠 CSS 自定义属性继承穿过 shadow 边界；这些变量必须声明在文档的 `:root` / `[data-theme]` 上。
  组件内部不要写 `[data-theme]` 选择器，也不要依赖宿主 class。
- 只有继承属性穿得过边界：`box-sizing` 不是继承属性，shadow 内必须在自己的作用域重新声明
  `box-sizing` 重置，不要依赖宿主的 `* { box-sizing: border-box }`。
- 万不得已才退回 light DOM（仅当必须直接复用宿主的 CSS 规则时），类名加项目前缀，新增例外要在评审里
  说明依赖了宿主的哪条规则。混合形态（部分 shadow、部分 light）要划清边界并记录原因。

### 无 document 环境安全

- 可能被 Node（SSR、`jsNodeTest`）加载的模块，类字段初始化器不要碰 `document` / `window` /
  `customElements`；这些入口要么带守卫，要么由调用方在浏览器环境触达。
- `CSSStyleSheet` 等浏览器对象必须惰性创建（在构造函数里），不要在模块级初始化。
- 浏览器专用的重依赖用动态 `import()` 按需加载，业务纯数据模块不要反向依赖重依赖。

### React 19 自定义元素

- 对象 prop 不会在 SSR/hydration 阶段设到自定义元素上，挂载用 callback ref 显式喂内容。
- `mount*()` 助手必须是纯 setter 调用（幂等）：StrictMode 双调用、元素移动后重挂载会重复喂同样的数据，
  重复执行必须无副作用。
- 预渲染 HTML 里自定义元素是空壳是已知取舍；light DOM 恒为空可以顺带消除 hydration mismatch。若控制台
  出现 hydration 报错指向自定义元素，说明有代码把节点挂回了 light DOM，修原因而不是用
  `suppressHydrationWarning` 掩盖。

## 版本约定

每个子模块独立版本，格式为 `{上游 npm 版本}-kt.{修订号}`。上游 npm 版本只在根目录 `package.json` 的
`dependencies` 中声明；每个模块的修订号在根目录 `wrapper-revisions.properties` 中维护。根
`build.gradle.kts` 解析这两份数据，分别放入 `extra["npmVersions"]` 与 `extra["wrapperRevisions"]`，
各子模块读取后声明 `npm(...)` 并推导版本。禁止在模块脚本里硬编码 npm 版本或 `kt.N`。

- 只发布带 `-kt.N` 后缀的版本，不存在不带后缀的版本。
- **工作树里的版本一律再带 `-SNAPSHOT`**：推导出的 `X-kt.N` 是「下一个要发的版本」，Central 上还没有，
  所以开发构建不冒用这个名字（本地发布也不会遮蔽真实版本，消费方能一眼分辨）。`-Pktjs.release=true`
  去掉后缀，`release.yml` 的两处 Gradle 调用都带着它；本地要发真版本时自己加。
  Gradle 不把 `-P` 传给 included build，`ktjs-docusaurus-gradle-plugin` 是独立构建，发布它时必须在它自己的
  调用里再传一次。
- 每个模块的修订号独立递增，永不重置为 `kt.1`。它表示该模块的第 N 次 wrapper 发布，与上游 npm 版本
  各自独立演进。
- wrapper 代码或该模块的已发布元数据变化时，只把 `wrapper-revisions.properties` 中对应模块的修订号加一；
  其他模块保持原版本，发布时命令里不列它们，因此不会为它们产生新版本、新文件。
- 仅上游 npm 版本变化、wrapper 代码没变时，修订号保持不变；Dependabot 改 `package.json` 后，受影响模块的
  基础版本自动跟随（如 `3.0.4-kt.3` → `3.0.5-kt.3`），该模块作为新版本发布。
- 模块间的 `api(project(...))` 会解析到依赖模块当前的版本。一次提交同时变更多个模块时，必须在同一条发布
  命令里上传所有缺失版本；不要分多次只发布其中一个，否则 POM 可能指向尚未上传的依赖版本。
- 新增发布模块时，在 `wrapper-revisions.properties` 里从 `1` 开始加一行；缺少条目会在配置阶段直接失败。
- 消费方不必为了上游补丁等 wrapper 发版：在根构建脚本里用 `NpmExtension.override(...)` 覆盖 klib 记录的
  npm 版本即可（根 `overrides` 对 workspace 内所有包生效）。不要用第二个 `npm(...)` 声明去改版本：它只改
  声明它的模块的 `package.json`，其他模块仍固定 wrapper 的版本，npm 可能装出两份。
- `override` 改完必须重建锁文件：Gradle 用的是 npm 11，`overrides` 变化不会让旧锁失效，`npm install` 会
  认为 `up to date` 并继续装旧版本。删掉 `kotlin-js-store/package-lock.json` 后跑
  `./gradlew kotlinNpmInstall`，再用 `./gradlew kotlinUpgradePackageLock` 把新锁写回。
- `-kt.N` 在 SemVer 中是预发布标识，优先级低于同基础版本的无后缀版本；因为永不发布无后缀版本，发布序列
  内部不会冲突。

## 仓库与本地镜像

仓库里提交的构建配置只使用官方源：`settings.gradle.kts` 用 Gradle Plugin Portal 与 Maven Central，
`gradle/wrapper/gradle-wrapper.properties` 用 `services.gradle.org`。这样 GitHub Actions 和任何克隆仓库的
人行为一致。

国内加速只在本机生效，通过 `~/.gradle/init.d/aliyun-mirror.gradle` 注入阿里云镜像，不提交到仓库；不要在
`settings.gradle.kts` 或 wrapper 配置里写镜像地址。用 Groovy 而不是 `.gradle.kts`：IDEA 同步时会对
`init.d` 下的 Kotlin DSL 脚本做独立编译，`beforeSettings` 等隐式接收者解析不了，会报一堆
`Unresolved reference`（命令行构建却正常）。

`kotlin-wrappers` 同步滞后，且有时只有 POM 没有 klib，而 Gradle 一旦在某个仓库找到元数据就不会回退到
下一个仓库。`settings.gradle.kts` 因此用 `exclusiveContent` 把 `org.jetbrains.kotlin-wrappers` 和
`kotlin-js-plain-objects` 固定到 Maven Central；新增 wrapper 依赖时如遇 `Could not find ... .klib`，把
对应 group/module 加进去。

## CI 与依赖更新

- `.github/workflows/ci.yml` 只在**所有 PR** 和手动触发时运行 `./gradlew build`（JDK 21，Ubuntu），
  **不被 push 触发**：能进默认分支的提交已经在 PR 上跑过同一套构建。`pull_request.branches` 同时列了
  `main` 和 `master`，因为生成的项目两者都有；漏掉实际默认分支会让 CI 静默不跑，连带 Dependabot 的
  自动合并永不触发。需要验证默认分支现状时用 `gh workflow run ci.yml`。
- 模板仓库自己**跑不了构建**：`ktjs-docusaurus` 占位符会让 npm 拒绝 `ktjs-docusaurus-demo` 这个包名，所以
  `ci.yml` 第一步检查 `settings.gradle.kts` 里还有没有 `ktjs-docusaurus`，没初始化就跳过后面所有步骤
  （run 仍然算成功，Dependabot 的自动合并才能走完）。生成的项目里占位符已被替换，照常执行。
- `.github/workflows/dependabot-auto-merge.yml` 在 CI 成功后自动 squash 合并 Dependabot 的 PR。它用
  `workflow_run` 而不是直接由 Dependabot PR 触发，因为后者拿到的 `GITHUB_TOKEN` 只读。下载 CI 产出的
  `npm-lock` artifact 是 `continue-on-error`：模板仓库没有锁文件可刷，缺这个 artifact 不该卡住合并。
- `.github/dependabot.yml` 覆盖 `gradle`、`npm`（根目录 `package.json`）和 `github-actions` 三个生态。
  npm 版本由 Dependabot 改 `package.json`，Gradle 自动读取，不再手工改 `npm(...)`。Docusaurus 的几个包
  lockstep 发版，升级时它们必须在同一个 PR 里一起动，否则 wrapper 的 npm 依赖会出现版本混搭。
- Gradle wrapper 固定在 `9.4.1`，并在 Dependabot 里忽略 `gradle-wrapper`：IntelliJ IDEA 2026.2.3
  同步 Gradle 9.8.x 项目时会报 `ExternalSystemPartialResolutionException`（消息为空），回退到 9.4.1
  即恢复正常。等 IDEA 修复后再删除这条 ignore 并升级。
- Dependabot 的 npm PR 不会带 Gradle 生成的锁文件更新，所以 CI 对 Dependabot PR 先跑
  `kotlinUpgradePackageLock` 再构建，并把校验过的锁文件作为 `npm-lock` artifact 上传；自动合并 job
  下载该 artifact、提交到 PR 分支，然后 squash 合并。普通 PR 仍然严格校验锁文件。
- `kotlin-js-store` 里只有 Gradle 生成的锁文件，没有也不应该有 `package.json`；根目录的
  `package.json` 才是版本清单。
- 本地增量构建后，`jsPackageJson` / `rootPackageJson` 可能残留旧输出（模块版本变成
  `0.0.0-unspecified`、漏掉 `packages_imported/*`），此时 CI 会报 `Lock file was changed`。执行
  `./gradlew kotlinUpgradePackageLock --rerun-tasks` 重新生成锁文件并提交。
- 提升 `wrapper-revisions.properties` 的修订号也会改生成的 workspace 包版本，必须同时跑
  `./gradlew kotlinUpgradePackageLock` 并提交 `kotlin-js-store/package-lock.json`，否则 CI 在
  `:kotlinStorePackageLock` 报 `Lock file was changed`。

## 发布到 Maven Central

发布配置统一在根 `build.gradle.kts` 的 `subprojects {}` 里，使用 `com.vanniktech.maven.publish`，
group 固定为 `io.github.shijianjs`。`subprojects` 的 group 必须单独设置，根项目的 `group` 不会自动继承。

发布是两段式，Central 只在第二段被触及：

1. 本仓库 `.github/workflows/release.yml`（只有 `workflow_dispatch`）：解析版本 → 用仓库里的签名密钥
   签名 → 发布到模块内的 `build/hub-staging` 文件仓库 → `scripts/hub-release.py` 为**每个 Maven 坐标**
   打一个 `.tar.xz` 并生成注册 → 创建/更新 GitHub Release 并上传归档 → 把注册推到 hub → 触发 hub 的
   `upload=false` dry run 并等待它结束。
2. hub（`shijianjs/maven-central-publishing`）手动 dispatch 且勾选 `upload=true`，才真正上传 Central，
   并在成功后删除已满足的注册。

```bash
gh workflow run release.yml -f release=release-1 \
  -f modules=ktjs-docusaurus
```

- `release`：`release-<数字>`，tag 由工作流创建；工作流不会移动已有 tag。
- `modules`：逗号分隔；留空表示全部发布模块。只列本次修订号变化的模块。
- `register`（默认 true）：把注册推到 hub；关掉则只做构建和 GitHub Release。
- `verify`（默认 true）：触发 hub 的 dry run 并等待；关掉则到 Release 为止。

重跑同一 `release`：已存在的 Release 用 `gh release upload --clobber` 覆盖同名归档，注册按 sha256 覆盖
旧文件。归档时间戳固定为 1980-01-01、条目顺序固定，但 Gradle 产出的 klib/jar/module 本身不是逐字节可
复现的，所以重跑会得到新的 sha256 并改写注册；覆盖是幂等的，不要把归档 sha256 当成构建输入的指纹。

版本仍完全由 `package.json` 与 `wrapper-revisions.properties` 推导，工作流不重复 `-kt.N` 规则：
`./gradlew -q printPublishedVersions` 输出 `模块=版本`，只把 `modules` 选中的模块传给
`hub-release.py`。

### 一次性准备

1. 在 [Central Portal](https://central.sonatype.com/) 注册账号并验证 `io.github.shijianjs` namespace
   （GitHub 方式）。
2. 生成 GPG 密钥并发布公钥到 `keys.openpgp.org` / `keyserver.ubuntu.com`。
3. 在本仓库 Settings → Secrets 配置 `MAVEN_CENTRAL_SIGNING_KEY`、`MAVEN_CENTRAL_SIGNING_KEY_PASSWORD`
   与 `HUB_RELEASE_TOKEN`（fine-grained PAT：hub 的 Contents + Actions 读写）。Central User Token 配在
   hub 仓库，不放在本仓库。
4. **若沿用 hub：新增 KMP 模块（产生新的 `*-js` 坐标）时，先在 hub 的 `artifactOverrides` 里补
   `jar: false` 并关闭 `verifyPom`**。JReleaser 的 PomChecker 不认 klib packaging，且它把 pom/aar/war/zip
   以外的 packaging 都当成必须有同名 `.jar`；漏配会让 hub 整批失败。
5. 第一次发布用 `release-1`，`modules` 留空（发布全部模块）；之后只列修订号变化的模块。

### 本地预演

```bash
ORG_GRADLE_PROJECT_signingInMemoryKey="$(< ~/central-signing-key.txt)" \
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=... \
./gradlew :ktjs-docusaurus:publishAllPublicationsToHubStagingRepository

python3 scripts/hub-release.py --project shijianjs/ktjs-docusaurus --release release-1 \
  --module ktjs-docusaurus=<version> --repository . \
  --archives build/hub-release/archives --registrations build/hub-release
```

每个归档只含一个 Maven 坐标、含 `.asc`、不含 `.md5` / `.sha1`。还可以在 hub 仓库执行
`python3 scripts/collect.py --local <本仓库>/build/hub-release` 再 `python3 scripts/stage.py` 做离线预检。

### 发布额度

Central 社区版按月限制 Release Count、File Count 与 Release Size：

- **Release Count 按 deployment 计**，不按模块或坐标计。所有待发布构件必须在同一次上传里；不要按模块
  循环调用 `publishToMavenCentral`，那会把 1 次 deployment 拆成 N 次。
- **只发变更模块**：未提升修订号的模块不要列进发布命令，减少 File Count、Release Size 和无意义的版本号。
- **javadoc 空包**：`configureBasedOnAppliedPlugins(javadocJar = JavadocJar.Empty())`，API 文档由 Pages
  统一部署，不在每个构件里重复 Dokka 的静态资源。
- **不传签名校验文件**：`excludeSignatureChecksums()` 保证不会为 `.asc` 上传 `.md5` / `.sha1`。

### klibs.io 自动发现

klibs.io 从 Maven Central 与 GitHub 自动抓取，无需提交元数据文件。条件：项目开源且托管在 GitHub；至少
一个构件发布到 Maven Central 且已同步到 Google 的镜像；至少一个构件是 KMP 并发布
`kotlin-tooling-metadata.json`；POM 的 `url` 或 `scm.url` 是有效的 GitHub 仓库链接。改仓库地址时要同步
修改根 `build.gradle.kts` 的 POM 配置。

## 文档站与 GitHub Pages

- `docs` 只消费公共 API，不发布、也不进入 Dokka。根 `build.gradle.kts` 的发布与 Dokka 配置按
  `ktjs-docusaurus` 前缀过滤子模块，新增非发布模块时沿用该约定。
- `docs` 就是文档站本身：`src/jsMain/resources/{docs,pages,css,static}` 是内容，
  `src/jsMain/kotlin/io/github/shijianjs/ktjs/docusaurus/docs/Config.kt` 是配置。
  `./gradlew :docs:docusaurusBuild` 把它变成静态站点，产物在 `build/js/docusaurus/docs/build`（站点目录
  在 npm workspace 里的原因见上面的「Gradle 插件与站点组装」）。
- 导航栏分四块，四块各自一个 `docSidebar` 条目、一个同名 sidebar：User guide（`userGuide`）与 Docs kit
  （`docsKit`）面向站点使用者，DOM kit（`domKit`）是两者底下那层通用 DOM 辅助的说明，Development
  （`development`）放本仓库自己的开发文档。Gradle 插件页在用户指南里，不在 kit 里——wrapper 加插件才是
  完整的 Docusaurus，kit 只是本项目按自己的喜好加的增强。开发文档只写中文，不建 `i18n/` 下的对应文件
  （`zh-Hans` 会回落到原文）；其余三块英文为源、中文在
  `i18n/zh-Hans/docusaurus-plugin-content-docs/current/` 下对应路径。
- 浏览器端 e2e 是 `:docs:e2eTest`（JVM Playwright，见「Kotlin/JS 模块格式与测试运行时」），改完站点
  本地按需核对；它不进 `build` / `check`，也不进 CI。CI 的链路验证仍是「构建成功 + Docusaurus 自己的
  断链检查」：`onBrokenLinks = 'throw'`（`docs` 里显式设置）让断链变成构建失败。
- `./gradlew pagesSite` 把 `build/js/docusaurus/docs/build` 与 `build/dokka/html` 组装到 `build/pages`：
  站点在根目录，API 文档在 `api/`。
- Dokka 2.2.0 同时应用到根项目和 wrapper 子模块；聚合入口是根项目的 `dependencies { dokka(...) }`，
  任务为 `dokkaGeneratePublicationHtml`（2.x 已移除旧的 `dokkaHtmlMultiModule`）。
- `.github/workflows/pages.yml` 只有 `workflow_dispatch`：站点更新在内容值得上线后手动执行
  `gh workflow run pages.yml`（或 Actions 页面点 Run workflow）。它每次都重新跑 `pagesSite`，所以线上
  内容始终对应触发时 `master` 的提交。仓库 Settings → Pages 的 Source 必须选 **GitHub Actions**。
- 本地预览：执行 `./gradlew pagesSite` 后给 `build/pages` 起任意静态服务器。直接开 `file://` 会因相对
  路径与 CORS 导致资源加载异常。要看 dev server 用 `./gradlew :docs:docusaurusStart`。
- 右侧目录的折叠按钮由 kit 的 `TocToggle` 注入：Node 那一半只找到桌面目录、在列表前插一个
  `<ktjs-toc-toggle>` 标签，按钮、存储的偏好和布局切换全在 `ktjs-docusaurus-kit-browser` 的元素里
  （kit 不能 import 它，那会在 Node prerender 时求值 `HTMLElement`，所以标签名按字面量跨边界重复一次，
  由 e2e 兜住）。元素不认 Docusaurus 选择器，只看自己的位置：`parentElement` 是目录列、
  `nextElementSibling` 是列表、目录列的 `previousElementSibling` 是文章列。收起时这几处改的是**内联样式**
  （列缩成 2.5rem 的窄条——不能整列隐藏，按钮就在里面；列表 `display: none`；文章列
  `max-width: 100% !important`，因为 Docusaurus 在哈希 CSS module 里用 `max-width: 75% !important` 钉住
  它），**没有** `document.head` 里的全局样式表、也没有 `body` 上的状态类；状态就是宿主上的 `collapsed`
  属性。按钮自身的 sticky、胶囊、箭头旋转在该元素自己的 `cssSheet` 里。目录列是唯一既挨着目录、又在文章
  之外的位置；视口右下角是主题自带「回到顶部」按钮的地盘，放那里会重叠。按钮高度把目录的 sticky 偏移往下
  顶，两者因此不会叠在一起。

## 文档约定

- 长期任务（跨上下文）用根目录 `TODO.md` 记总览、
  `docs/src/jsMain/resources/docs/development/roadmap.md` 记完整清单（同一份也发布在站点的 Development
  分类下）。状态只有
  完成 / 明确不做 / 待完成三种，标「完成」前必须以源码和构建结果核对，不能凭对话里的声明。
- 需要跨会话、跨上下文感知的信息（环境约定、构建方式、模块结构、容易踩的坑）记录在本文件。
- 只与某个目录强相关、篇幅较长的说明，放在该目录下的 `AGENTS.md`，根文件只保留指向它的简短索引。
- 公开声明都要有 KDoc：上游源码或 README 有说明的，翻译并改写过来；没有的按实际行为补充。描述以源码
  为准，不要照搬过时或错误的理解。
- 每个模块维护 `README.md` 与 `README.zh-CN.md`；根 README 的模块表、版本与 Maven 徽章随发布更新。

## 模板同步与升级

本文件同时是 [`shijianjs/ktjs-wrapper-template`](https://github.com/shijianjs/ktjs-wrapper-template) 的
`AGENTS.md` 模板，只写适用于所有 wrapper 项目的通用约定：不出现具体项目名、npm 包名、模块名、版本号或
仓库地址；与具体领域相关的清单和规则写在派生项目自己的 `AGENTS.md`。

- 在派生项目里总结出对所有 wrapper 项目都成立的约定（构建、测试、发布、DOM/UI 约定、上游 API 建模等）
  时，主动把它同步到模板仓库：先读模板现状，已有等价内容就跳过，缺失或过时就用通用措辞补上或修正；
  完成后自动提交并推送模板仓库，不需要逐次征求同意。
- 只回灌通用部分。项目专属的包清单、目录名、演示内容、版本号留在派生项目。
- 优先就地修改或扩展现有小节，不要为同一主题新开重复小节。
- 从模板升级派生项目时仍从模板仓库 `git diff` 取变更后人工 cherry-pick，不要用派生项目的其他文件
  反向覆盖模板。
