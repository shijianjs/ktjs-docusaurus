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
目录是 React 渲染的，所以它只出现在有标题的页面上、只出现在宽视口下。按钮只创建一次，没有目录的页面再把它
移除；React 重建目录列之后会重新插入，所以重渲染不会把它带走。

Gradle 插件把导出的函数变成 `client-module.mjs`，站点在 `clientModules` 里列出它。

## 布局

按钮插在目录列里、正好在它控制的目录列表上方，并用与列表相同的方式钉住：`position: sticky`，紧贴导航栏
下方。放在视口角落更简单，但右下角已经属于主题自带的「回到顶部」按钮，两者会重叠。

收起时目录列缩成一条窄栏，而不是整列隐藏——按钮就在这一列里，隐藏整列等于把「把目录叫回来」的控件也藏掉。
窄栏里只隐藏列表，文章列（打了 `ktjs-doc-article` 标记）用 `max-width: 100% !important` 接管腾出来的宽度：
Docusaurus 在一个带哈希的 CSS module 里用 `max-width: 75% !important` 钉住文章列，只隐藏列表会留下 25% 的
空白。

按钮自身的高度把目录的 sticky 偏移往下顶（`calc(var(--ifm-navbar-height) + 3rem)`），这就是两者不重叠的
原因。

折叠控件只在 `min-width: 997px` 出现，与 Docusaurus 桌面目录的断点一致：更窄时根本没有
`theme-doc-toc-desktop`，`refresh()` 会把按钮移除。

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
