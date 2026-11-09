# TODO

把 Docusaurus 站点做成 Kotlin/JS 项目：配置是 Kotlin，页面是 Markdown/MDX，构建由 Gradle 插件驱动。

- 总计：44
- 已完成：27
- 明确不做：4
- 待完成：13

详细清单：[docs/development/roadmap.md](docs/development/roadmap.md)

## 当前待办

- [ ] demo 的中文翻译补齐 4 篇（`guide/content`、`kit/home-sections`、`kit/toc-toggle`、`kit/gradle-plugin`）
- [ ] TOC 折叠在客户端导航后重新挂载（现在只在页面加载时挂一次）
- [ ] 浏览器里实际验收首页与 kit（构建与产物已验证，运行时未验收）
- [ ] `@easyops-cn/docusaurus-search-local` 的选项类型（当前只有 `JsAny` 口子）
- [ ] Gradle 插件的 Maven 发布（独立构建，需要单独发布命令 + 接入 `release.yml`）
- [ ] wrapper / kit / 插件首次发布到 Maven Central
- [ ] 清理 `probes/web-components`（官方示例复现用，issue 已关闭）

## 已排期的升级（等 Kotlin 2.5 正式版）

- [ ] `target = "es2020"`：2.4 只接受 es5/es2015，五个仓库现在都写死 es2015
- [ ] `KotlinPublicationFormat.KOTLIN_ARCHIVE`：发布体积，`gradle.properties` 一行开关

## 注意事项

- 状态以源码和构建结果为准；只写了代码但没验证的不算完成。
- 明确不做的四项及理由在 roadmap 里，不要因为「顺便」把它们捡回来。
- 跨仓库（vaadin / echarts / iconify / wrapper-template）的构建约定改动，改完要同步到每个仓库。
