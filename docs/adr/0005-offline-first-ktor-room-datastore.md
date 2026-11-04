# 0005. 离线优先：Ktor + Room KMP + DataStore KMP 跨平台数据存储选型

* 状态: Accepted
* 日期: 2026-11-04

## 背景与问题上下文

移动端应用必须支持无网或弱网下的离线可用性。数据层 Repository 统一遵循 Cache-First 范式：先读取本地持久化数据呈现，后并发发起网络请求更新缓存。

## 决策依据与选项

1. **网络层**：选用 **Ktor Client**，Android 端使用 `OkHttp` 引擎，iOS 端使用 `Darwin` 引擎，结合 `kotlinx.serialization` 提供跨平台 JSON 解析。
2. **结构化持久化**：选用 **Room KMP** 与 `BundledSQLiteDriver`，替代 SQLDelight，与 Android 原生 Room 体验保持最大一致性。
3. **键值存储**：选用 **DataStore Preferences KMP**，跨平台安全管理用户偏好与轻量状态。

## 最终决策

采纳 Ktor + Room KMP + DataStore Preferences KMP 技术组合。网络 DTO 与 Entity 必须在 Repository 内部转换为纯领域模型（Domain Model），禁止泄露至 UI 层。
