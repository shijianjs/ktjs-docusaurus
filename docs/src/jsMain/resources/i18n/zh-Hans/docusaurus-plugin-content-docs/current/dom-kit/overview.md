---
sidebar_position: 1
---

# DOM 工具箱

`ktjs-dom-kit` 是工具箱下面那一层：用 Kotlin/JS 对着原生 DOM 写组件时用的辅助代码，不认识 Docusaurus，
背后也没有任何组件运行时。

它是本构建的一个模块，而不是一个独立库。wrapper（`ktjs-docusaurus`）把 npm 包映射成 Kotlin 声明，文档
工具箱是本项目自家的 UI，而这里是两者共同踩的那层地皮——自定义元素可以继承的基类、把 DOM 当作唯一状态
来源的属性委托、元素内联样式的类型化视图、用 Kotlin 值（而不是 CSS 文本）搭出 `CSSStyleSheet` 的方式，
以及 `loadjs` 加载器的类型化封装。

## 为什么留在本仓库

它依赖 kotlin-browser 与 kotlin-csstype，不依赖 Docusaurus，所以**本来**可以由自己的仓库发布。但没有：
那样它的文档就得用它所帮助构建的那套框架来写——「讲『用来写文档站的辅助库』的文档站」由消费它的站点发布
——绕成一个说不清方向的环。所以这个仓库是它的家，不是临时落脚点。

模块名刻意不以 `ktjs-docusaurus` 开头：这里没有任何 npm 包要跟随，所以它用自己的语义版本，而不是
`{Docusaurus 版本}-kt.{修订号}`；根构建把它显式列在前缀之外一并发布。它是发布出去的
（`io.github.shijianjs:ktjs-dom-kit`），也进 Dokka——它是可复用的契约，不是本站自家的管道。

## 里面有什么

模块按关注点分包：一件事一个包，一个声明一个文件（编解码器、委托与它的 provider、桥的删除），所以没有
哪个文件是杂物间。

| 包 | 提供 |
| --- | --- |
| `element/` | `CustomElementAdapter`——按原生名字声明生命周期钩子、承载 `attr` 委托的基类；`defineIfMissing`；以及 [`stripBridgesFrom`](./custom-elements.md)（删掉编译器生成的桥） |
| `attribute/` | `attr` 属性委托：`AttributeCodec` 与内建实现（`String` / `Boolean` / `Int` / `Double` / `enumAttributeCodec`）、`AttributeChange`、委托与它的 provider，以及属性名规则 |
| `style/` | `HTMLElement.inlineStyle`（元素自己的样式，`csstype.Properties` 视图）、`cssSheet { … }` / `cssText { … }`、`attachShadowRoot` / `attachStyledShadow` |
| `DomExtensions.kt` | `TagName<T>.createElement { … }` |
| `css/` | cssom 没建模的部分，用类型化的值补齐：`Pseudos`、`SupportsQuery`、`radialGradient`、`webkitBackgroundClip`…… |
| `loadjs/` | [`loadjs`](https://github.com/kubetail-org/loadjs) 加载器的封装：四种调用形态各按分支写成一个重载、路径前缀（`css!`、`img!`、`module!`、`nomodule!`）与 bundle 注册表（`ready` / `done` / `reset` / `isDefined`） |

## 它不做什么

- **没有组件模型。** 没有状态、没有 `render()`、没有 diff，也没有 UI 运行时。组件构造时建一次子树，
  之后只改自己持有的节点——实际形态见[首页分区](../kit/home-sections.md)。
- **不拼 HTML 字符串。** 节点就是节点（`document.createElement` 配类型化的 `TagName<T>`），不从标记
  文本拼出来再解析。
- **没有 `<style>` 节点。** 组件自己的样式要么是节点上的内联声明（`inlineStyle`），要么是 shadow root
  adopt 的一份 `CSSStyleSheet`；sheet 属于类，不属于实例。

## 各页面

- [自定义元素](./custom-elements.md)——基类、生命周期钩子与标签定义。
- [属性](./attributes.md)——`attr` 委托与它的编解码器。
- [样式](./styling.md)——内联样式、shadow root 与 adopt 的 sheet。
- [类型化样式表](./css-sheet.md)——`cssSheet { … }`：序列化管线、类型化的值、坑，以及 cssom 没有的
  类型怎么办。
