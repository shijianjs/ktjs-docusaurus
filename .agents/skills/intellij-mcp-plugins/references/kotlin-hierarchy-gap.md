# Kotlin 类型层级缺失：证据链与排查

## 现象

同一台 IDE、同一个插件、同一组参数：

| 调用 | Java | Kotlin |
|---|---|---|
| `ide_type_hierarchy` | 完整 `supertypes` 树 + `subtypes` | `No class/type found at the specified position` |
| `ide_find_implementations` | 接口 → 全部实现类 | `No method or class found at position` |
| `ide_find_super_methods` | 返回方法层级 | `No method found at position` |
| `ide_symbol_info` | `signatureSource: "java_psi"` | `signatureSource: "element_text"` |
| `ide_find_symbol` / `ide_find_references` | 可用 | 可用 |

对照组做法：Java 项目里放 `class MyList extends ArrayList`，先查 `java.util.ArrayList`，再查
`org.example.MyList`，两次都能返回树，且 `subtypes` 里带出项目内的 `MyList`。

## 证据链

1. **实现走「Java PSI + Kotlin light class 反射桥」**，不是原生 Kotlin Analysis API。
   修复记录里写明：`PsiTreeUtil.getParentOfType(PsiClass)` 匹配不到 `KtClass` / `KtNamedFunction`，
   只能反射找 Kotlin PSI 再转 light class。Kotlin 光标解析、suspend 调用点、val/var 赋值等是逐个补的。
2. **作者承认 Kotlin 测试有缺口**：真实 Kotlin PSI 测试是 opt-in（`-PkotlinPluginTests=true`），
   默认 CI 不跑；Java 路径有行为覆盖，Kotlin 层级基本靠手工 IDE 验证。
3. **K2 是默认模式，而 Kotlin 解析对线程极敏感**：插件声明 `supportsK2="true"`，但 K2 的 Analysis API
   禁止在 EDT 上解析，曾导致 Kotlin 重命名全挂；也有 Kotlin 属性 FQN 被解析到 light backing field、
   `ide_find_references` 静默返回 0 usages 且 `totalIsExact: true` 的案例。
4. **社区没有对应 issue**：公开 issue 里没有「Kotlin 类型层级空结果」的在办条目。

结论：这不是配置错误，而是插件对 Kotlin 缺少 PSI 桥接的结构性限制。

## 排查顺序（按收益排序）

1. **升级插件**：≥ 5.1.0（最好 5.20.x），并确认 IDEA / Kotlin 插件版本配套。
2. **确认索引状态**：`ide_index_status` 的 `isDumbMode` 必须为 false。
3. **确认项目路由**：多项目 / workspace 必须带 `project_path`。
4. **换成 symbolId 入口**（绕开 language handler）：

   ```json
   { "name": "ide_find_symbol",
     "arguments": { "query": "UserRepository", "language": "Kotlin",
                    "scope": "project_and_libraries" } }
   ```

   拿到 `symbolId` 后分别调用（两个工具代码路径不同，要分开试）：

   ```json
   { "name": "ide_find_implementations",
     "arguments": { "symbolId": "<id>", "scope": "project_and_libraries",
                    "includeGenerated": true } }
   ```

   ```json
   { "name": "ide_type_hierarchy",
     "arguments": { "symbolId": "<id>", "scope": "project_and_libraries",
                    "maxNodes": 100, "includeGenerated": true } }
   ```

   注意：`ide_find_implementations` 的 `includeGenerated` 默认 false（KSP/Dagger 生成的实现会被丢掉），
   `ide_type_hierarchy` 默认 true；加 `maxNodes` 走 BFS 分页，避免老式嵌套树静默截断。

5. **K1 / K2 A/B**：K1 能出树、K2 不能，即可定位为 K2 / Analysis API 问题。
6. 仍失败 → 走替代流程，并提 issue。

## 替代流程（Kotlin 可用的组合）

- **子类型**：`ide_find_references`（position 或 symbolId 入口）的结果中包含
  `class X : Base()` 继承声明，筛出即得子类；可递归。
- **父类型**：`ide_symbol_info` 的 `signature` 含超类型列表（`open class X : HTMLElement()`），
  解析后对父名再 `ide_find_symbol` 递归。
- **短名消歧**：同名类可能来自无关库（`Element`、`Node` 极易撞车）。按「同文件 > 同目录 > 同库 >
  `.kt` 源码 > `.class`」排序；`.class` 的 signature 不可靠，不要继续深入。
- 不要再回退到 grep；`ide_find_references` 已经能给出语义引用。

## issue 上报模板

带上这些信息，作者修复节奏很快：

- 插件版本、IDE build、Kotlin 插件版本、K1/K2；
- 最小 `.kt` 片段（一个接口 + 一个实现即可）；
- 请求 JSON（`ide_type_hierarchy` 的 position 与 symbolId 两种形式）；
- 期望返回 vs 实际返回（含 `isError` 与原始文本）；
- 同项目下 Java 类可正常返回树的对照结果。

## 长期方案

1. **给官方内置 MCP 补工具**：JetBrains 允许插件向内置 MCP Server 贡献工具，并在 Exposed Tools 里统一
   开关。向 YouTrack 提需求，或自己写个小插件调用 `TypeHierarchyProvider` /
   `DefinitionsScopedSearch`，比等第三方修 light-class 桥更可靠。
2. **双挂**：官方负责稳定性与工作流，第三方负责语义索引；在项目 `AGENTS.md` 里写死「查引用/层级用
   `ide_*`，禁止 grep 回退」。
