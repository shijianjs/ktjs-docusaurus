---
sidebar_position: 3
---

# 本地搜索

导航栏里的搜索框来自 `@easyops-cn/docusaurus-search-local`：索引在 `docusaurus build` 时从 Markdown
里建出来，所以既没有搜索服务，也没有第三方爬虫。`ktjs-docusaurus` 的 wrapper 映射了它的选项，本站用的就是
这套类型。

## 注册

这个包是**主题**，不是插件——它提供 `@theme/SearchBar` 与 `@theme/SearchPage`——所以放在 `themes` 里。
Docusaurus 会自己在导航栏渲染 `@theme/SearchBar`，不需要加导航栏条目：

```kotlin
themes = arrayOf<PluginConfig?>(
    PluginConfig(
        "@easyops-cn/docusaurus-search-local",
        unsafeJso<SearchLocalOptions> {
            // 站点上的每一种语言：每种语言各用自己的分词器。
            language = SearchLocalStringOrStringArray(arrayOf("en", "zh"))
            // 本站没开博客，没有可索引的内容。
            indexBlog = false
            // 索引 URL 带内容哈希，重新部署后浏览器缓存自动失效。
            hashed = BooleanOrHashed(Hasheds.query)
        },
    ),
)
```

`language` 必须列出站点发布的每一种语言。`zh` 不是普通的 lunr 语言：这个包用 jieba 给中文分词，所以选项
是 `zh` 而不是 `zh-CN`。

## 各选项的作用

| 选项 | 本站取值 | 理由 |
| --- | --- | --- |
| `language` | `["en", "zh"]` | 两种语言都建索引，各用自己的分词器 |
| `indexBlog` | `false` | 本站的 classic preset 没有博客 |
| `hashed` | `"query"` | 索引文件的缓存很激进，靠哈希打掉 |
| `searchBarPosition` | `"auto"` | 交给主题，跟随导航栏自身布局 |
| `docsRouteBasePath` | 默认 `/docs` | 与 preset 的 `routeBasePath` 一致 |

其余上游选项用同名同类型声明——`searchResultLimits`、`ignoreFiles`、`searchContextByPaths`、
`zhUserDict`、`fuzzyMatchingDistance` 等。联合类型保持「每个分支一个工厂函数」，所以多态选项只能通过
wrapper 构造：

```kotlin
ignoreFiles = IgnoreFiles(arrayOf(StringOrRegExp("/legacy"), StringOrRegExp(RegExp("^/v1/"))))
```

## 构建

索引是构建后的一步，由 `docusaurusBuild` 产出（改页面时开发服务器也会重建）：

```
build/js/docusaurus/docs/build/
├── search-index.json            # 默认语言
└── zh-Hans/search-index.json    # 另一种语言
```

已经构建但不该被搜到的页面——草稿、`noIndex` 页面——默认不进索引，除非设了 `forceIgnoreNoIndex`。
`/search` 路由展示完整结果列表，导航栏里的框展示建议。

## 快捷键

`Ctrl`/`Cmd`+`K` 聚焦搜索框（`searchBarShortcutKeymap`，默认 `"mod+k"`）。只想隐藏框里的提示、保留
快捷键时，设 `searchBarShortcutHint = false`。
