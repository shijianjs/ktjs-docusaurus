---
sidebar_position: 2
---

# 内容与 i18n

页面是 `src/jsMain/resources` 下的 Markdown 文件。Gradle 插件把它们拷进站点目录；正文部分没有任何东西
是从 Kotlin 生成的。

| 目录 | 落点 | 用途 |
| --- | --- | --- |
| `resources/docs` | `docs/` | 文档页 |
| `resources/blog` | `blog/` | 博客文章（本站已关闭） |
| `resources/pages` | `src/pages/` | 独立页面（`.md`、`.mdx`，或 Kotlin 组件的 `.jsx` shim） |
| `resources/css` | `src/css/` | 额外样式表 |
| `resources/static` | `static/` | 原样拷贝的文件 |
| `resources/i18n` | `i18n/` | 翻译 |

## 翻译

`docusaurus.config.mjs` 声明了 `en` 和 `zh-Hans` 两种语言，源语言是英文。每个译文页都在自己语言、自己
插件的目录下镜像源文件的路径——文档页放在 `docusaurus-plugin-content-docs/current/`，独立页面放在
`docusaurus-plugin-content-pages/`：

```
resources/docs/intro.md
resources/i18n/zh-Hans/docusaurus-plugin-content-docs/current/intro.md

resources/pages/index.md
resources/i18n/zh-Hans/docusaurus-plugin-content-pages/index.md
```

所以文案始终留在页面里：两种语言两个 Markdown 文件，首页也不例外。同一个 `i18n` 目录下的 `code.json`
是给**组件**自己持有的字符串用的——导航栏、页脚，以及 Kotlin 页面用
`translate { id = …; message = "…" }` 渲染出来的内容。

## 正文为什么不用 MDX

MDX 是可用的，页面也可以用。它只是不作为默认：只有正文的页面就是 `.md` 文件，这样它和 React 组件树
毫无关系。当页面确实需要 `<Layout>` 级别的调整或者一个组件时，把它命名为 `index.mdx`，走同一条 MDX
流水线——`resources/pages` 是原样拷贝的，所以变的只有扩展名。
