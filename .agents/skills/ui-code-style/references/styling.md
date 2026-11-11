# 样式落地细节

配套 [`../SKILL.md`](../SKILL.md) 第三节「样式：三级阶梯」。这里只放**怎么写**与**坑**。

## 1. 内联写法

### TypeScript / 原生 DOM

```ts
const card = el('article', {class: 'x-card'});
card.style.display = 'flex';
card.style.flexDirection = 'column';
card.style.padding = '1.5rem';
card.style.borderRadius = '1rem';
```

- 一个属性一行，按视觉顺序（布局 → 盒模型 → 字体 → 颜色）。
- 多个声明可以用 `Object.assign(card.style, {...})`，但**不要**为此退回 `cssText` 字符串拼接，也不要
  为了「少写几行」抽一个 `applyStyles(el, obj)` 通用工具（那就变成 CSS-in-JS 了）。
- 取值用 CSS 长度/颜色字面量，不引入额外库。

### Kotlin / JS（kotlin-wrappers + csstype）

`HTMLElement.style` 来自 lib.dom，属性的类型是 `String`。要拿到类型化视图，用 csstype 的
`Properties`（本项目里由 `ktjs-dom-kit` 的 `HTMLElement.inlineStyle` 提供，实现就是一次
`unsafeCast(style)`——同一个 `CSSStyleDeclaration` 对象，只是换成类型化接口看它）：

```kotlin
card.inlineStyle.apply {
    display = Display.flex
    flexDirection = FlexDirection.column
    padding = 1.5.rem                  // 四边相同；多值用 Padding(vertical, horizontal) 等工厂
    borderRadius = 1.rem
}
```

常用取值词汇（`web.cssom`）：

| 想要 | 写法 |
| --- | --- |
| 长度 | `1.5.rem`、`16.px`、`100.vw`、`50.pct`、`0.px` |
| 百分比 / 计算 | `50.pct`、`50.pct - 50.vw`（生成 `calc(50% - 50vw)`） |
| 流式尺寸 | `clamp(2.25.rem, 5.vw, 3.25.rem)`、`min(17.rem, 100.pct)`、`max(...)` |
| 无单位数字（行高、opacity、flex-grow/shrink） | `number(1.65)`、`number(0.0)` |
| 整数（font-weight、z-index） | `integer(600)`、`integer(-1)` |
| 关键字 | `Display.flex`、`Position.relative`、`Isolation.isolate`、`Overflow.clip`、`TextAlign.center`、`FlexWrap.wrap`、`AlignItems.center`、`JustifyContent.center`、`AutoRepeat.autoFit`、`AutoLength`/`Auto.auto` |
| `none` | `None.none`（它同时是 `Display` / `PointerEvents` / `TextDecorationLine` / `BackgroundImage` … 的子类型） |
| 颜色（含主题变量） | `Color("var(--accent, var(--ifm-color-primary))")`、`Color.transparent`、`Color("inherit")` |
| 简写 | 单值直接赋长度（`padding = 1.5.rem`）；多值用 `Padding(vertical, horizontal)`、`Padding(top, horizontal, bottom)`、`Padding(top, right, bottom, left)`、`Margin(0.px, Auto.auto)`、`Border(1.px, LineStyle.solid, Color.transparent)` |
| 阴影 / 滤镜 / 变换 | `dropShadow(0.px, 14.px, 28.px, Color(...))`、`translatex(-50.pct)`（`BoxShadow(...)`、`Transition(...)` 有工厂，但带状态时按下面第 3 条留在 CSS 里） |

闭合联合类型里没有工厂的值（例如 `repeat(auto-fit, minmax(min(17rem, 100%), 1fr))`——cssom 的
`minmax` 两端要求 `GridAutoTracks`，`min()` 不是），用 `unsafeCast("…")` 并**在注释里写明为什么**。
不要因为写不出来就退回 `String`：那等于放弃这层类型检查。

## 2. 组件自己的小段 CSS

只放内联表达不了的规则，并与组件同目录——一个组件一份，不合并成「首页样式表」：

```
home/
├── Hero.ts        + hero.css          # 这个组件的规则 + 它自己的 sheet
├── Features.ts    + features.css
├── NextSteps.ts   + next-steps.css
├── Section.ts     + section.css       # 两个分区共用的外壳（真共享）
├── base.css                           # :host + box-sizing，所有组件都用
└── styles.ts                          # ?inline 收集 + 惰性 sheet
```

注入方式是**一个组件类一份 `CSSStyleSheet`，该组件的所有实例 adopt 同一份**（Lit 的 `static styles`
就是这个模型）。不要每个实例建 `<style>` 节点，也不要每个实例 `new CSSStyleSheet()`：规则是静态的，
一份就够，实例多时差别是「一份解析」对「N 份文本 + N 次解析」。

```ts
// styles.ts：每份文件惰性建一次，导出取 sheet 的函数
function lazySheet(css: string): () => CSSStyleSheet {
  let sheet: CSSStyleSheet | null = null;
  return () => {
    if (!sheet) {
      sheet = new CSSStyleSheet();
      sheet.replaceSync(css);
    }
    return sheet;
  };
}
export const heroStyles = lazySheet(heroCss);

// 组件：共享 reset 在前，自己的规则在后
const shadow = this.attachShadow({mode: 'open'});
shadow.adoptedStyleSheets = [baseStyles(), heroStyles()];
```

```kotlin
// 同文件：规则文本 + 模块级 sheet（Kotlin/JS 顶层 val 首次访问才初始化）
private const val ACTION_CSS = """…"""
private val ACTION_SHEET: CSSStyleSheet = CSSStyleSheet().apply { replaceSync(ACTION_CSS) }

val shadow = attachStyledShadow(HOME_RESET, ACTION_SHEET)
```

类名只留给这些规则选中的节点。有了内联样式就不需要「每个节点一个语义 class」，多余 class 会让读者以为
别处还有规则在用它。

**惰性创建是 SSR 的要求**：`CSSStyleSheet` 在 Node 里不存在，模块级 `new CSSStyleSheet()` 会让预渲染
直接崩。上面两种写法都是第一次用到才建（Kotlin/JS 的顶层 `val` 本身也是惰性初始化，但只在浏览器侧
被求值的模块里才安全）。

## 3. 内联与状态的冲突（最容易做错）

```ts
// 反例：hover 永远不生效
run.style.backgroundColor = 'var(--accent)';
// 组件 CSS: .x-run:hover { background-color: var(--accent-strong); }
```

内联声明优先级高于任何选择器规则（`!important` 除外，而 `!important` 只会让下一个人更难改）。
所以：

- **被 `:hover` / `:focus-visible` / `[aria-pressed]` 改变的属性**（背景、边框色、字色、阴影）
  → 连同它们的 base 值、`transition` 一起写在组件 CSS 里。
- **被 `@media` 改变的属性**（窄屏换成的 `padding`、`flex-direction`）同理：内联会让断点失效。
  要么这组属性留在 CSS，要么改写成 `clamp()` 之类的流式值、把断点整段删掉——后者更省事。
- **不会被状态改变的布局属性**（`display`、`gap`、`padding`、`border-radius`、字号）
  → 内联。
- **简写与长写**：内联 `border` / `margin` / `padding` 会同时写死各长写属性，CSS 里的
  `border-color` / `margin-top` 就盖不住。要么整组留在 CSS，要么整组内联，不要一半一半。
- **`@supports` 守卫的增强**（例如 `background-clip: text` 的渐变标题）留在 CSS：内联没法条件降级，
  而内联的 `color` 又会压住 CSS 里的 `color: transparent`。
- **`:host` 自身**：自定义元素的 `display` 不能从构造函数写（给 `this` 加属性会让
  `document.createElement` 抛错），所以 `:host { display: block }` 只能落在共享 reset 里。

## 4. 从集中式 CSS 拆回去（迁移步骤）

已有一份几百行、多个组件共用 class 的 CSS 时：

1. **按组件切段**：给每个选择器标出它属于哪个组件（`ktjs-hero::before` → hero，
   `ktjs-feature-card` → feature，`.ktjs-section` → 分区外壳）。
2. **每段搬进该组件的文件**，先原样保留 class 与规则，保证行为不变。
3. **逐条判断能否内联**：静态布局属性改写成内联，删掉对应规则；不能内联的（第 3 条）留下。
4. **被删空的 class 一起删掉**，别留「没人用的钩子」。
5. **剩下跨组件重复的规则**才进共享文件：reset、主题 token、父容器布局。一份都不剩就把共享文件删掉。
6. **流式尺寸替换断点**：能写成 `clamp()` / `min()` / `max()` 的 `@media` 就消掉；真正的断点才留。
7. **在浏览器里核对**：计算样式（`getComputedStyle`）、hover 态、窄屏布局，三者一起过一遍；只看构建
   通过说明不了样式对不对。

## 5. 主题与边界

- 主题颜色从页面继承进 shadow 树，所以组件里写 `var(--ifm-*)` / `var(--品牌-*)` 就够；
  `[data-theme]` 选择器留在**文档级**，组件里不要写。
- 需要给下游留覆盖点时，token 写成 `var(--组件-xxx, 回退值)`，覆盖点写在文档里的一处（例如
  `:root { --组件-accent: … }`），不要散落在各组件。
- `box-sizing` 不是继承属性：宿主的 `* { box-sizing: border-box }` 选不进 shadow 树，组件内所有盒子会
  退回 `content-box`，带 padding / max-width 的盒子尺寸随之变化（实测会把网格的列数阈值挪一档）。所以
  每棵树里都要 `*, *::before, *::after { box-sizing: border-box }`——这是共享 reset 的正当理由。
- 结果区、编辑器这类「必须复用宿主 light DOM 规则」的组件才退回 light DOM，并写清依赖了哪条规则；
  三方部件往 document 注入样式表时，shadow 边界会让它失效，这类部件也因此更适合留在 light DOM。
