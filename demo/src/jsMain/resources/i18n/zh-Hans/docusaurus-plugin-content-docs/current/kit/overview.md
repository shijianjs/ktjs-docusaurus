---
sidebar_position: 1
---

# 工具箱

`ktjs-docusaurus-kit` 不是 wrapper。wrapper（`ktjs-docusaurus`）映射 npm 包；工具箱里是自己写的逻辑，
直接对着 DOM，也不跟随上游版本——它有自己的语义版本。

它存在的理由是文档站需要三样 npm 包不提供的东西：

- **首页分区**——hero、特性网格、"下一步"卡片。纯 DOM，构造时建一次，之后只通过 setter 更新。
- **目录折叠**——Docusaurus 只给左侧栏提供了 `themeConfig.docs.sidebar.hideable`，右侧目录没有折叠开关。
- **小部件**——不值得为它引入一个 React 组件或一套组件运行时。
