---
name: intellij-mcp-plugins
description: 用脚本调用 IntelliJ IDEA 的 MCP server（官方内置与 IDE Index MCP 插件），并在 Kotlin 项目里取得类型继承树（父类链 + 子实现），弥补 ide_type_hierarchy 对 Kotlin 的缺失。
---

# IntelliJ MCP 脚本化查询

## 适用场景

- 想从命令行或脚本查询 IDEA 索引（符号、引用、继承），不依赖 agent 客户端内置的 MCP 工具。
- 需要 Kotlin 的 F4 等价物：父类链 + 子实现列表。
- 在 CI 或 agent 流程里做批量语义查询。

## 背景

- IDEA 有两个 MCP server：官方内置（稳定，工作流类工具）与 IDE Index MCP 插件（语义索引 / 层级 / 重构，
  迭代最快），端口不同，可以同时挂。
- 插件的 `ide_type_hierarchy` / `ide_find_implementations` / `ide_find_super_methods` 只对 Java 有效；
  Kotlin 未桥接 PSI（`ide_symbol_info` 的 `signatureSource`：Java 是 `java_psi`，Kotlin 是 `element_text`）。
- 所以 Kotlin 的继承树由 `scripts/kt_hierarchy.py` 用 `ide_symbol_info` + `ide_find_symbol` +
  `ide_find_references` 组合得出。

## 前置条件

- IDE 已打开目标项目；多项目或 workspace 下调用必须带 `--project`。
- Index MCP 插件默认地址：`http://127.0.0.1:29170/index-mcp/streamable-http`。
- 官方内置 server 的 URL 随 IDE 版本变化（常见 63342 / 64342），用 `--url` 覆盖；它还需要 header
  `IJ_MCP_SERVER_PROJECT_PATH`。
- 默认禁用的工具需在 `Settings → Tools → Index MCP Server → Exposed Tools` 打开，例如
  `ide_symbol_info`、`ide_read_file`、`ide_find_symbol`。

## scripts/mcp_client.py

通用 streamable-HTTP 客户端，`list` / `schema` / `call` 三个子命令：

```bash
# 列出工具
python scripts/mcp_client.py list --project /path/to/repo

# 查看工具参数
python scripts/mcp_client.py schema ide_type_hierarchy ide_find_symbol

# 调用任意工具
python scripts/mcp_client.py call ide_find_symbol '{"query":"Repository","language":"Kotlin"}' \
  --project /path/to/repo

# 连官方内置 server（需要 header）
python scripts/mcp_client.py --url http://127.0.0.1:64342/stream \
  --header IJ_MCP_SERVER_PROJECT_PATH=/path/to/repo list
```

`--project` 会作为 `project_path` 注入每次 `tools/call`；也可用环境变量 `IDE_MCP_PROJECT`、
`IDE_MCP_URL`、`IDE_MCP_TIMEOUT`。

## scripts/kt_hierarchy.py

Kotlin 继承树，父类链 + 子类型一次给出：

```bash
python scripts/kt_hierarchy.py src/main/kotlin/App.kt 42 7 --project /path/to/repo
```

- `line` / `column` 为 1-based，`column` 落在符号名首字母上。
- UP 输出父链，追进库源码时是 `jar://...` 路径；DOWN 输出子类型。
- `--up N` / `--down N` 控制递归深度，默认 8。

输出示例（节选）：

```text
Hierarchy for: CustomElementAdapter  (…/CustomElementAdapter.kt:49)  [open class CustomElementAdapter : HTMLElement()]

UP (supertypes):
  HTMLElement  @ jar://…/org.w3c.dom.kt:72
    └ Element  @ jar://…/org.w3c.dom.kt:5186
      └ Node  @ jar://…/org.w3c.dom.kt:4762

DOWN (subtypes):
  - KtjsHero  @ …/KtjsHero.kt:16
  - KtjsFeatures  @ …/KtjsFeatures.kt:14
```

## 实现要点

- UP 先从 `ide_symbol_info` 的 `signature` 解析超类型短名，再用 `ide_find_symbol` 解析成 symbolId。
  短名消歧按「同文件 > 同目录 > 同库 > `.kt` 源码 > `.class`」排序，编译产物不再深入。
- DOWN 用 `ide_find_references`，其命中包含 `class X : Base()` 继承声明，过滤后递归。
- 两个 server 都监听 `127.0.0.1` 且无鉴权，共享机器上注意。

## 禁止事项

- 不要用 `grep` / 文本搜索替代语义查询。
- 不要对 Kotlin 反复更换 `ide_type_hierarchy` 的 target 形式硬试，直接走脚本的组合路径。
- 不要把「工具列表里有 `ide_type_hierarchy`」当成「它对 Kotlin 可用」。
