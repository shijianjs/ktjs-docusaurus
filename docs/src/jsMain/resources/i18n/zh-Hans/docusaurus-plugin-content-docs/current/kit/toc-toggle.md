---
sidebar_position: 2
---

# 目录折叠

Docusaurus 允许读者隐藏左侧栏（`themeConfig.docs.sidebar.hideable`），但右侧目录没有对应的开关。
`TocToggle` 补上这个控件和布局切换。

## 接线

两块内容，而且没有自己注入的样式表：

1. `TocToggle`，Node 安全的那一半。client module 在每次路由变化后调用它；它找到桌面目录，在列表
   前面插入一个 `<ktjs-toc-toggle>` 标签。
2. `<ktjs-toc-toggle>`，浏览器的那一半。它在 kit 的浏览器包里，持有按钮，并改变它被插入的那几个
   节点的布局。

```kotlin
private val toggle = TocToggle()

@JsExport
fun onRouteDidUpdate() {
    toggle.install()
    toggle.refresh()
}
```

`install()` 是幂等的——它只接一次视口监听。`refresh()` 让标签与当前页面重新对齐：目录是 React
渲染的，所以它只出现在有标题的页面上、只出现在宽视口下。没有目录的页面会把标签移除；React 重建目录列
之后会重新插入，所以重渲染不会把它带走。

Gradle 插件把导出的函数变成 `client-module.mjs`，站点在 `clientModules` 里列出它。标签本身由插件发布到
`static/ktjs-client/` 的包定义，client module 在 hydration 之后加载它——kit 模块本身从不求值它（见
[首页分区](./home-sections.md)）。

## 标签

```html
<ktjs-toc-toggle
  hide-label="收起目录"
  show-label="展开目录"
  storage-key="ktjs:toc-collapsed"
></ktjs-toc-toggle>
```

控件需要的其它信息全部来自它在页面里的位置，因此不需要任何 Docusaurus 选择器：

| 节点 | 从哪里来 | 收起时 |
| --- | --- | --- |
| 目录列 | `parentElement` | 缩成 `2.5rem`（`--ifm-col-width`）并去掉内边距 |
| 目录列表 | `nextElementSibling` | `display: none` |
| 文章列 | 目录列的 `previousElementSibling` | `max-width: 100% !important` |

这些都是正好写在那几个节点上的内联样式。收起时缩窄目录列而不是整列隐藏是有意的：按钮就在这一列里，
隐藏整列等于把「把目录叫回来」的唯一入口也藏掉。文章列要加宽，是因为 Docusaurus 在一个带哈希的 CSS
module 里用 `max-width: 75% !important` 钉住它，只隐藏列表会留下 25% 的空白。

`collapsed` 就是状态，而且只存在于这个属性上。按钮切换它；控件连接时还会从 `localStorage` 里读出上次的
选择，断开连接时把布局恢复原状（控件没了却留下一条收起的窄栏，就没有办法再展开）。

按钮的样式——紧贴导航栏的 sticky、胶囊、箭头及其旋转——是该控件自己的 `CSSStyleSheet`，由它的所有实例
adopt。没有任何东西被写进 `document.head`，也没有页面级规则去改组件之外的节点。箭头在展开时朝右（也就是
这一列收起的方向），收起时朝左（列表回来的方向）。

## 位置

按钮插在目录列里、正好在它控制的目录列表上方，并用与列表相同的方式钉住：`position: sticky`，紧贴导航栏
下方。放在视口角落更简单，但右下角已经属于主题自带的「回到顶部」按钮，两者会重叠。

按钮自身的高度把目录的 sticky 偏移往下顶（`calc(var(--ifm-navbar-height) + 3rem)`），这就是两者不重叠的
原因。

折叠控件只在 `min-width: 997px` 出现，与 Docusaurus 桌面目录的断点一致：更窄时根本没有
`theme-doc-toc-desktop`，`refresh()` 会把标签移除。

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

没有条目的语言回退到 `en`。文案以 `hide-label` / `show-label` 的形式传给控件；不传时控件回退到英文默认值。

## 存储失败不算错误

`localStorage` 在隐私模式、或禁止 Cookie 时会抛异常。读取失败当作「展开」；写入失败意味着这次选择不被记住。
两者都不会让页面出错。
