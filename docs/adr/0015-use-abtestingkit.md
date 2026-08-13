# ADR-0015: 使用 AbTestingKit 作为远程配置 / A/B 封装

## 状态

Accepted

---

## 背景

`:core:abtesting` 原先手写 `AbTestingHelper` + Firebase Remote Config + Koin。已有自研 [AbTestingKit](https://github.com/e-hai/AbTestingKit) 提供 `AbTestingClient` 门面与 Firebase Provider，继续维护项目内双重封装与其它 Kit 风格不一致。

## 决策

- 使用 **AbTestingKit `v1.0.0`**（`abtesting-firebase`）。
- 瘦身 `:core:abtesting`：`api` 透出门面 + `AbTestingInitializer` + `AbTestingKeys`。
- **删除** `AbTestingHelper` / `FirebaseAbTestingHelper` / `coreAbTestingModule`。
- 启动顺序：`install` → `setDefaults` → 异步 `fetchAndActivate`（经 `ProcessLifecycleOwner.lifecycleScope`）。
- Firebase BOM 对齐至 **34.17.0**（与 Kit POM 一致）。
- 业务直接调用 `AbTestingClient.getBoolean(AbTestingKeys.…)`；测试使用 Kit 内 `FakeAbTestingProvider`。

## 参考

- [AbTestingKit](https://github.com/e-hai/AbTestingKit)
- ADR-0011（AnalyticsKit 瘦身范式）
