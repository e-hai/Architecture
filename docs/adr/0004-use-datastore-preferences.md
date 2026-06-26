# ADR-004: 使用 DataStore Preferences 作为键值存储方案

## 状态

Accepted

---

## 背景

项目需要轻量级键值存储用于用户偏好设置、缓存状态等场景。Android 传统方案 SharedPreferences 存在同步写入、数据丢失风险、缺乏类型安全等问题，已被标记为废弃。

## 备选方案

- **SharedPreferences**：已废弃，同步 API 可能阻塞 UI 线程，apply() 的异步行为不可靠，commit() 的返回值无实际意义。
- **DataStore Preferences**：DataStore 的键值对版本，使用 Flow 提供异步、响应式读取，无需定义 schema。
- **Proto DataStore**：DataStore 的强类型版本，需要定义 `.proto` 文件，支持自定义类型，但引入额外的 schema 管理和编译步骤。

## 决策

使用 **DataStore Preferences 1.2.1** 作为键值存储方案。

封装模式：每个功能模块编写 `XxxPreferencesDataSource` 类，对外暴露类型安全的 API（Flow 用于读取，suspend 用于写入），内部封装 DataStore 实例。

## 理由

- Preferences 版本使用简单，无需定义 proto schema，适合项目脚手架阶段的轻量需求
- 所有读取返回 Flow，响应式且生命周期感知
- 写入为 suspend 函数，天然异步，不阻塞 UI
- 读写操作是事务性的，成功或全部回滚
- Proto DataStore 引入的 `.proto` 文件管理和编译步骤在当前阶段属于过度设计

## 影响

### 正面影响

- 无需额外 schema 定义和编译步骤
- 异步 API 天然防止 UI 线程误操作
- Flow 响应式读取，写入后订阅者自动接收更新
- 事务性读写保证数据一致性

### 负面影响

- 没有自定义类型的类型安全（只支持 Int、String、Boolean、Float、Double、Set 基本类型）
- 不适合存储大量结构化数据或复杂对象
- 每次 `edit()` 会对整个文件进行序列化和反序列化，应尽量减少写入频率
- 大数据量场景性能不如 Room

### 中立影响

- 数据需要通过 SharedPreferencesMigration 迁移（如从旧版升级），但本项目为脚手架，不涉及迁移场景

---

## 遵循情况

- 禁止使用 SharedPreferences
- 禁止使用 Proto DataStore
- DataStore 实例通过 Koin 的 `single` 提供
- 对外只暴露 Flow（读取）和 suspend 函数（写入）
