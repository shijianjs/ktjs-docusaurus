# Roadmap

## 来源与范围

- 来源：本项目的需求清单（2026-11-08 起的需求对话）与 Docusaurus 3.10.2 的公开配置面。
- 版本：`@docusaurus/*` 3.10.2；`@easyops-cn/docusaurus-search-local` 0.55.3；Kotlin 2.4.21；
  `kotlin-wrappers` 2026.11.0。
- 核对日期：2026-11-10。
- 范围：`ktjs-docusaurus` 仓库（wrapper、Gradle 插件、kit、demo），以及 `ktjs-*` 系列的通用构建约定
  （第 6 节）。
- 不在范围：`duckfn` 站点的内容、`oop-react`（TypeScript 项目，`target: ES2022`）。

## 统计

| 状态 | 数量 |
| --- | ---: |
| 总计 | 47 |
| 已完成 | 35 |
| 明确不做 | 4 |
| 保留 | 1 |
| 待完成 | 7 |

## 1. 项目形态（6 项，全部完成）

- 1.1 wrapper 作为单一 Gradle 模块，Docusaurus 的包只对应 Kotlin 包
- 1.2 配置、主题、侧边栏都是 Kotlin
- 1.3 文档放在 `src/jsMain/resources`，随 Gradle 资源走
- 1.4 仓库里没有手写 npm 工程（`package.json` 只做版本清单）
- 1.5 包名统一为 `io.github.shijianjs.ktjs.docusaurus`
- 1.6 `../AGENTS.md` 的两条约定（国内镜像克隆、只读仓库可随意切分支/tag）

## 2. wrapper 覆盖面（11 项：9 完成 / 2 不做）

- 2.1 `Config` 全字段
- 2.2 `preset-classic` 与 docs / blog / pages / sitemap / svgr / gtag / gtm / analytics / theme-classic 选项
- 2.3 `ThemeConfig`（navbar、footer、prism、colorMode、toc、algolia、mermaid、liveCodeBlock）
- 2.4 `SidebarsConfig` 与全部条目类型
- 2.5 `PrismThemes`
- 2.6 React 客户端层（`Layout`、`Heading`、`Link`、`Translate`、`useDocusaurusContext` 等）
- 2.8 `@easyops-cn/docusaurus-search-local` 的选项类型：`plugins.searchlocal` 覆盖 0.55.3 的全部
  `PluginOptions`，联合类型各分支都有工厂（`hashed`、`ignoreFiles`、`searchContextByPaths`、
  `removeDefaultStopWordFilter`、`searchBarPosition`）
- 2.9 官方插件全覆盖（14 个）：content-docs / content-blog / content-pages / sitemap / svgr /
  google-analytics / google-gtag / google-tag-manager / client-redirects / ideal-image / pwa /
  rsdoctor / css-cascade-layers / vercel-analytics。`plugin-debug` 没有选项，只有
  `DebugPlugin.NAME`。顺带把已有声明按 3.10.2 补齐：docs 的 `banner` 改成字面量、pages 的
  `editUrl` / `showLastUpdate*`、blog 的 `pageBasePath` / `tags` / `onInlineTags` / `feedOptions.xslt`、
  sitemap 的 `lastmod` 改成 `'date' | 'datetime'`、gtag 的 `trackingID` 支持多 id
- 2.10 官方主题全覆盖（4 个）：theme-classic 的 `customCss`、theme-search-algolia 的
  `ThemeConfig.algolia`（含 `insights` / `searchParameters` / `translations`）、
  theme-live-codeblock 的 `ThemeConfig.liveCodeBlock`、theme-mermaid 的 `ThemeConfig.mermaid`

明确不做：

| 名称 | 理由 |
| --- | --- |
| `@docusaurus/core` 运行时导出（`build` / `start` / `serve` / `swizzle`） | Gradle 插件直接调 CLI，Kotlin 侧没有消费者 |
| 其余第三方主题/插件选项类型 | 按实际需求逐个补，不做枚举式预置 |

## 3. Gradle 插件（9 项：7 完成 / 2 待完成）

- 3.1 站点组装（内容、编译产物、config/sidebars shim）
- 3.2 `docusaurusBuild` / `Start` / `Serve` / `Clear`
- 3.3 `i18n/` 目录随内容拷贝
- 3.4 `clientModuleFunction` → `client-module.mjs`
- 3.5 `browserBundle` + `browserBundleEntry` → `static/ktjs-client/`（浏览器专属 bundle）
- 3.6 `pageFunctions` / `pageBundle` 页面 shim
- 3.7 Node 运行时探测（workspace / Gradle 用户目录 / PATH）
- 3.8 插件本体的 Maven 发布（独立构建，需单独的发布命令与 staging 配置） —— **待完成**
- 3.9 插件接入 `release.yml`，与 wrapper 同版本发布 —— **待完成**（依赖 3.8）

## 4. kit（6 项：4 完成 / 2 不做）

- 4.1 `HomeHero` / `FeatureSection` / `NextStepSection`
- 4.2 `TocToggle`
- 4.3 iconify 图标接入
- 4.4 JVM 版 Playwright e2e（`jvm()` + `src/jvmTest` + `e2eTest`，参考 `ktjs-vaadin`）

明确不做：

| 名称 | 理由 |
| --- | --- |
| colormath 颜色计算 | 组件只读 `--ktjs-*` 自定义属性，不参与颜色运算 |
| Playwright 的 TS wrapper / TS 测试层 | 直接用 Playwright Java API 与 JUnit 5 集成；duckfn kit 那套 TS 封装不搬，简单思路可参考 |

## 5. demo 文档站（7 项：全部完成）

- 5.1 首页 MDX + 自定义元素（`ktjs-hero` / `ktjs-features` / `ktjs-next-steps`）
- 5.2 双语 i18n 配置（en / zh-Hans）
- 5.3 英文文档页（`intro`、`guide/*`、`kit/*`）
- 5.4 中文翻译：`guide/content`、`guide/local-search`、`kit/home-sections`、`kit/toc-toggle`、
  `kit/gradle-plugin`
- 5.5 导航栏两个大分类（User guide / Docs kit）
- 5.6 TOC 在客户端导航后重新挂载（`onRouteDidUpdate` 每次路由变化都 `refresh()`）；按钮改为挂在
  `<body>` 上并固定在视口右下角，因为原来的位置在页面顶部会被固定导航栏盖住、收起后又会随目录列一起
  `display: none`；收起时文章列同时加宽到整行
- 5.7 运行时验收（headless Chromium）：首页 hero 1 / 卡片 4 / 步骤 3、shadow root 已挂载；文档页的 TOC
  按钮在「加载 → 客户端导航 → 无目录页 → 回到有目录页」全链路都在，收起/展开与刷新后保持；本地搜索在
  en / zh-Hans 两侧都能返回建议与结果页；console 无错误

## 6. 跨仓库构建约定（5 项：2 完成 / 2 待完成 / 1 保留）

- 6.1 `-Xir-dce=false`（本仓库、vaadin、echarts、iconify、wrapper-template）
- 6.2 `target = "es2015"`（同上五个）
- 6.3 Kotlin 2.5：`target = "es2020"` —— **待完成**，阻塞于 2.5 正式版
- 6.4 Kotlin 2.5：`KOTLIN_ARCHIVE` 发布格式 —— **待完成**，同上
- 6.5 `probes/web-components` —— **保留**（原计划清理）。它是自定义元素行为的可复现探针，issue 关闭后
  仍然有用：以后再遇到 `connectedCallback` 不触发、原型链自递归桥、`@JsName` 改名这几类问题时不必重建
  最小工程。不参与发布与 Dokka。

## 7. 发布（3 项：全部待完成）

- 7.1 wrapper 首发到 Maven Central —— **待完成**；`release.yml` 沿用模板但未实际跑通
- 7.2 kit 首发 —— **待完成**，依附 7.1 的流程
- 7.3 插件首发 —— **待完成**，与 3.8 / 3.9 是同一件事的两个侧面
