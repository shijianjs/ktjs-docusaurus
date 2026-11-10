---
name: intellij-mcp-plugins
description: 在 IntelliJ IDEA 中选型与配置 MCP 插件（官方内置、IDE Index MCP、Debugger MCP），并处理 Kotlin 项目里类型层级/实现树工具（ide_type_hierarchy、ide_find_implementations、ide_find_super_methods）失效的问题。
---

# IntelliJ IDEA MCP 插件选型与 Kotlin 层级排查

## 适用场景

- 准备给 IDEA 挂 MCP 给编码助手用，纠结选官方内置还是第三方 IDE Index MCP。
- `ide_type_hierarchy` / `ide_find_implementations` / `ide_find_super_methods` 在 Kotlin 项目上返回空、报
  `No class/type found` 或静默 0 结果。
- 同一个插件 Java 能出继承树、Kotlin 出不来，需要判断是配置问题还是插件缺陷。
- 需要一套可复用的「查父类 / 查子实现」替代流程，替代 IDEA 的 F4 类型层级树。

## 结论先行

1. **「功能最全」「最稳」「维护最积极」三者当前无法同时满足。**
2. **Kotlin 的 F4 等价物（类型层级树）目前没有任何 IDEA MCP 插件能稳定给出。**
3. **Java 侧 IDE Index MCP 的层级工具完整可用；Kotlin 侧只有文本/索引级能力。** 这已被对照实验证实：
   `ide_symbol_info` 对 Java 返回 `signatureSource: "java_psi"`，对 Kotlin 返回 `"element_text"`。

## 三个 MCP 的定位

| | 官方内置 MCP Server | IDE Index MCP Server（第三方） | Debugger MCP Server（同作者） |
|---|---|---|---|
| 定位 | 内置 IDE 工作流 | 语义索引 / 层级 / 重构 | 调试专用 |
| 类型层级 / 实现树 | 完全没有（只有 `analyze_calls` 调用层级） | 宣称支持 Java、Kotlin 等；**实测仅 Java 可用** | 无关 |
| 工具面 | build/run/terminal/DB/VCS + 少量符号检索 | 符号、引用、层级、重构、诊断、文件结构 | 断点、线程、内存 |
| 稳定性 | 官方、随 IDE 发版、YouTrack 支持 | 社区单人主导，迭代最快 | 社区 |

插件**默认禁用一批写操作与重工具**（`ide_symbol_info`、`ide_read_file`、`ide_open_project`、
`ide_project_diagnostics` 等），需要在 `Settings → Tools → Index MCP Server → Exposed Tools` 里手动打开。
工具数量随暴露配置变化，不要用「工具数」判断能力。

## 推荐组合：双挂

- **官方内置**：Auto-Configure、build/run/debug/VCS/DB、Exposed Tools 权限控制。
- **IDE Index MCP**：语义索引、引用、层级、重构。Java 项目直接可用；Kotlin 走下面的替代流程。
- 两者端口不同、不冲突。都是 `127.0.0.1` 无鉴权，共享机器上启用写工具前要收紧。

## Kotlin 层级失效时的处理顺序

1. `ide_index_status` 确认 smart mode；IDE 外写过文件先 `ide_sync_files`。
2. 多项目 / workspace 下**必须带 `project_path`**，否则报 `multiple_projects_open`。
3. 确认插件版本 ≥ 5.1.0（最好 5.20.x，带分页版），并核对 IDEA / Kotlin 插件版本配套。
4. 统一用 **`symbolId` 入口**：先 `ide_find_symbol` 拿 symbolId，再喂给层级工具；不要依赖
   `language + symbol`，Kotlin 的 language 归属在文档和实现之间并不一致。
5. 做一次 K1 / K2 A/B：K1 能出树、K2 不能，即可定位为 K2 / Analysis API 问题。
6. 仍失败，不要继续调参——按下面的替代流程走，并考虑提 issue。

## 替代流程（Kotlin）

- **子类型**：`ide_find_references` 的结果里包含 `class X : Base()` 继承声明，筛出即可。
- **父类型**：`ide_symbol_info` 的 `signature` 字段含超类型列表（`open class X : HTMLElement()`）。
- **短名消歧**：同名类可能来自无关库（`Element`、`Node` 很常见），按「同文件 > 同目录 > 同库 >
  `.kt` 源码 > `.class` 编译产物」排序，且不要深入 `.class`（其 signature 不可靠）。
- 以上即可复刻 F4 的两个方向；细节与证据链见 `references/kotlin-hierarchy-gap.md`。

## 禁止事项

- 不要用 `grep` / 文本搜索替代语义查询（引用、层级、重命名）。
- 不要在层级失败后反复换 target 形式或加参数硬试；先确认是 Kotlin 专属限制。
- 不要把「插件工具列表里有 `ide_type_hierarchy`」当成「它对 Kotlin 可用」。
- 不要在共享机器上默认开启写工具与 `ide_open_project`。

## 参考

- `references/plugin-comparison.md`：三插件详细对比、版本节奏、数据来源与截止日期。
- `references/kotlin-hierarchy-gap.md`：Kotlin 支持缺陷的证据链、可复现排查步骤、issue 上报模板、长期方案。
