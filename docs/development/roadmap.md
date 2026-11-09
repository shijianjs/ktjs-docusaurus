# Roadmap

## 来源与范围

- 来源：本项目的需求清单（2026-11-08 起的需求对话）与 Docusaurus 3.10.2 的公开配置面。
- 版本：`@docusaurus/*` 3.10.2；Kotlin 2.4.21；`kotlin-wrappers` 2026.11.0。
- 核对日期：2026-11-09。
- 范围：`ktjs-docusaurus` 仓库（wrapper、Gradle 插件、kit、demo），以及 `ktjs-*` 系列的通用构建约定
  （第 6 节）。
- 不在范围：`duckfn` 站点的内容、`oop-react`（TypeScript 项目，`target: ES2022`）。

## 统计

| 状态 | 数量 |
| --- | ---: |
| 总计 | 44 |
| 已完成 | 29 |
| 明确不做 | 4 |
| 待完成 | 11 |

## 1. 项目形态（6 项，全部完成）

- 1.1 wrapper 作为单一 Gradle 模块，Docusaurus 的包只对应 Kotlin 包
- 1.2 配置、主题、侧边栏都是 Kotlin
- 1.3 文档放在 `src/jsMain/resources`，随 Gradle 资源走
- 1.4 仓库里没有手写 npm 工程（`package.json` 只做版本清单）
- 1.5 包名统一为 `io.github.shijianjs.ktjs.docusaurus`
- 1.6 `../AGENTS.md` 的两条约定（国内镜像克隆、只读仓库可随意切分支/tag）

## 2. wrapper 覆盖面（9 项：6 完成 / 1 待完成 / 2 不做）

- 2.1 `Config` 全字段
- 2.2 `preset-classic` 与 docs / blog / pages / sitemap / svgr / gtag / gtm / analytics / theme-classic 选项
- 2.3 `ThemeConfig`（navbar、footer、prism、colorMode、toc、algolia、mermaid）
- 2.4 `SidebarsConfig` 与全部条目类型
- 2.5 `PrismThemes`
- 2.6 React 客户端层（`Layout`、`Heading`、`Link`、`Translate`、`useDocusaurusContext` 等）
- 2.8 `@easyops-cn/docusaurus-search-local` 的选项类型 —— **待完成**；目前只通过
  `PluginConfig(name, options: JsAny)` 留了口子

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

## 4. kit（5 项：3 完成 / 2 不做）

- 4.1 `HomeHero` / `FeatureSection` / `NextStepSection`
- 4.2 `TocToggle`
- 4.3 iconify 图标接入

明确不做：

| 名称 | 理由 |
| --- | --- |
| colormath 颜色计算 | 组件只读 `--ktjs-*` 自定义属性，不参与颜色运算 |
| Playwright 相关 | kit 不需要浏览器测试；duckfn 那套已有 JVM 实现 |

## 5. demo 文档站（7 项：5 完成 / 2 待完成）

- 5.1 首页 MDX + 自定义元素（`ktjs-hero` / `ktjs-features` / `ktjs-next-steps`）
- 5.2 双语 i18n 配置（en / zh-Hans）
- 5.3 英文文档页（`intro`、`guide/*`、`kit/*`）
- 5.4 中文翻译：`guide/content`、`kit/home-sections`、`kit/toc-toggle`、`kit/gradle-plugin` —— **待完成**
- 5.5 导航栏两个大分类（User guide / Docs kit）
- 5.6 TOC 在客户端导航后重新挂载（`onRouteDidUpdate` 每次路由变化都 `refresh()`）
- 5.7 首页与 kit 的运行时验收（headless Chrome：hero 1 / 卡片 4 / 步骤 3，console 无错误）

## 6. 跨仓库构建约定（5 项：2 完成 / 3 待完成）

- 6.1 `-Xir-dce=false`（本仓库、vaadin、echarts、iconify、wrapper-template）
- 6.2 `target = "es2015"`（同上五个）
- 6.3 Kotlin 2.5：`target = "es2020"` —— **待完成**，阻塞于 2.5 正式版
- 6.4 Kotlin 2.5：`KOTLIN_ARCHIVE` 发布格式 —— **待完成**，同上
- 6.5 清理 `probes/web-components`（官方示例复现模块） —— **待完成**，issue 已关闭并更正

## 7. 发布（3 项：全部待完成）

- 7.1 wrapper 首发到 Maven Central —— **待完成**；`release.yml` 沿用模板但未实际跑通
- 7.2 kit 首发 —— **待完成**，依附 7.1 的流程
- 7.3 插件首发 —— **待完成**，与 3.8 / 3.9 是同一件事的两个侧面
