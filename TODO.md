# TODO

把 Docusaurus 站点做成 Kotlin/JS 项目：配置是 Kotlin，页面是 Markdown/MDX，构建由 Gradle 插件驱动。

- 总计：51
- 已完成：43
- 明确不做：4
- 保留：1
- 待完成：3

详细清单：[docs/src/jsMain/resources/docs/development/roadmap.md](docs/src/jsMain/resources/docs/development/roadmap.md)
（同时发布在站点的「Development」分类下：<https://shijianjs.github.io/ktjs-docusaurus/docs/development/roadmap>）

## 当前待办

- [ ] dom-kit 首发上传：`release-2` 已注册 `ktjs-dom-kit` 与 `ktjs-dom-kit-js` 0.1.0，hub dry run 通过
  （2 个坐标、44 个文件）；等下一次 hub 勾 `upload=true` 随批上传，不单独占一次 deployment。
- [ ] 另外两项要等 Kotlin 2.5 正式版（见下一节）。

## 已发布

`release-1`（2026-11-12）：`ktjs-docusaurus` 3.10.2-kt.2、`ktjs-docusaurus-kit` 0.1.0、
`ktjs-docusaurus-gradle-plugin` 3.10.2-kt.1（含它的 marker 坐标），由 `shijianjs/maven-central-publishing`
合并成一次 Central deployment。

`release-2`（2026-11-12）：只打包 `ktjs-dom-kit` 0.1.0 与 `ktjs-dom-kit-js` 0.1.0 并注册到 hub，dry run
通过；按计划没有上传，注册留在 hub 里等下一次随批。

## 已排期的升级（等 Kotlin 2.5 正式版）

- [ ] `target = "es2020"`：2.4 只接受 es5/es2015，五个仓库现在都写死 es2015
- [ ] `KotlinPublicationFormat.KOTLIN_ARCHIVE`：发布体积，`gradle.properties` 一行开关

## 保留

- `probes/web-components`：不再清理。它是自定义元素行为的可复现探针（官方示例 + 本仓库的桥接方案），
  以后再遇到 `connectedCallback`、原型链桥接、`@JsName` 之类的问题，不用重新搭一个最小工程。
  它不参与发布与 Dokka，平时不碰。

## 注意事项

- 状态以源码和构建结果为准；只写了代码但没验证的不算完成。
- 明确不做的四项及理由在 roadmap 里，不要因为「顺便」把它们捡回来。
- 跨仓库（vaadin / echarts / iconify / wrapper-template）的构建约定改动，改完要同步到每个仓库。
