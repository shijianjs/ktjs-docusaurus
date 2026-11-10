# IDEA MCP 插件对比

数据截止 **2026-11-10**。版本、下载量、评分会过期，使用前以插件市场页面为准。

## 三个插件

| 维度 | IDE Index MCP Server | 官方内置 MCP Server | Debugger MCP Server |
|---|---|---|---|
| 插件 ID | hechtcarmel 的 IDE Index MCP（端口 29170） | JetBrains 内置（2025.2 起） | 同作者，调试专用 |
| 工具数 | 52（第三方目录按 54 计） | 约 25（取决于 Exposed Tools） | 22 |
| 类型层级 | 宣称支持 Java、Kotlin 等；实测仅 Java 可用 | 无 | 无关 |
| 实现查找 | 同上 | 无 | 无关 |
| 调用层级 | 有 | 有（`analyze_calls`） | 无 |
| 下载 / 评分 | 13.07 万，5.00/5（33 评分） | 2117 万（内置累计），2.73/5（41 评分） | 2 万，4.8（9 评分） |
| 版本节奏 | 最新 5.20.3（10-24），9 月从 5.14 发到 5.20 | 随 IDE 发版，2026.2.4 / 2026.3（11-05） | 5.4.0（11-05） |
| 维护 | 379★ / 70 fork，7 个 open issue，单人主导 | 官方，16 个 IDE 兼容，YouTrack | 社区 |
| 风险 | 社区项目，无官方支持 | 工具面窄，无层级/重构 | 不解决索引问题 |

## 为什么选 IDE Index MCP 做语义层

- 功能最全：符号、引用、层级、实现、重构、诊断、文件结构都在一个插件里。
- 迭代最密：issue 响应快，CHANGELOG 详尽，Kotlin 相关修复持续在补。
- 但**层级能力在 Kotlin 上是结构性短板**，不要把它当作 F4 的可替代品。

## 为什么官方内置仍然要挂

- 最稳：随 IDE 走，没有第三方插件生命周期风险。
- 独占能力：Auto-Configure、build/run/debug、terminal、DB/VCS。
- Exposed Tools 提供统一的工具开关与权限控制。

## 数据来源

- GitHub 仓库元数据、release、commit、issue、README/USAGE/CHANGELOG/CONTRIBUTING（API 直连）。
- 插件市场：搜索页与 API 在部分网络环境被网关 403，下载量/评分/版本列表由搜索引擎已索引的市场页
  与公开统计站交叉核对。
- 官方工具清单：JetBrains 帮助文档与 YouTrack。
