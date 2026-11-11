---
sidebar_position: 2
---

# 目录折叠

Docusaurus 允许读者隐藏左侧栏（`themeConfig.docs.sidebar.hideable`），但右侧目录没有对应的开关。
`TocToggle` 补上这个按钮和布局切换。

## 接线

两块内容：一个 Docusaurus 在每次路由变化后调用的 client module，以及折叠控件自己注入的 CSS。

```kotlin
private val toggle = TocToggle()

@JsExport
fun onRouteDidUpdate() {
    toggle.install()
    toggle.refresh()
}
```

`install()` 是幂等的——它读取已存的选择、接上视口监听，只做一次。`refresh()` 让按钮与当前页面重新对齐：
目录是 React 渲染的，所以它只出现在有标题的页面上、只出现在宽视口下。按钮本身只在 `<body>` 上创建一次，
没有目录的页面再把它移除，所以文章区域重渲染不会把它带走。

Gradle 插件把导出的函数变成 `client-module.mjs`，站点在 `clientModules` 里列出它。

## 布局

按钮固定在视口右下角：滚动时够得着，更关键的是目录收起后也够得着。如果把它插在目录旁边，它就落在 React
的子树里，而收起目录列会把按钮一起隐藏掉。

收起时目录列隐藏，文章列加宽到整行。后半段需要自己的类（`.ktjs-doc-article`）和
`max-width: 100% !important`：Docusaurus 在一个带哈希的 CSS module 里用 `max-width: 75% !important`
钉住了文章列，只隐藏目录列会留下 25% 的空白。

按钮只在 `min-width: 997px` 出现，与 Docusaurus 桌面目录的断点一致。

## 状态类为什么挂在 `<body>`

Docusaurus 每次路由都会重写 `<html>` 的整个 `class` 属性，挂在那里的类会被丢掉。`<body>` 不受影响，
所以折叠状态以 `ktjs-toc-collapsed` 的形式放在它身上。

## 文案

文案跟随 `html-lang`，所以中文页面拿到的是中文标签：

```kotlin
TocToggle(
    TocToggleOptions(
        labels = mapOf(
            "en" to TocToggleLabels("Collapse table of contents", "Expand table of contents"),
            "zh-hans" to TocToggleLabels("收起目录", "展开目录"),
        ),
        storageKey = "ktjs:toc-collapsed",
    ),
)
```

没有条目的语言回退到 `en`。

## 存储失败不算错误

`localStorage` 在隐私模式、或禁止 Cookie 时会抛异常。读取失败当作「展开」；写入失败意味着这次选择不被记住。
两者都不会让页面出错。
