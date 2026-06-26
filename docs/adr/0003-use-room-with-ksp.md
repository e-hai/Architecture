# ADR-003: 使用 Room + KSP 实现本地数据库

## 状态

Accepted

---

## 背景

项目需要本地持久化关系型数据库来支持离线优先的数据策略。需要编译时 SQL 校验来保证查询正确性。同时考虑 Kotlin 2.0+ 的兼容性和注解处理性能。

## 备选方案

- **Room + KAPT**：官方 ORM 框架，编译时 SQL 校验，但 KAPT 注解处理器在 Kotlin 2.0 下性能下降且将被弃用。
- **Room + KSP**：KSP 是 KAPT 的替代方案，在 Kotlin 2.0+ 中性能更优，兼容性更好。
- **SQLDelight**：SQLDelight 支持多平台但非 Jetpack 官方方案，与 Room 的 Flow/Coroutine 集成需额外配置。

## 决策

使用 **Room 2.8.4** + **KSP 2.3.8**（对应 Kotlin 2.3.21）。通过 `myproject.android.room` 约定插件统一配置。

关键约定：
- Entity 放在 `core/database/model/` 目录，类名以 `Entity` 结尾（如 `UserEntity`）
- DAO 放在 `core/database/dao/` 目录
- Schema 导出到 `$projectDir/schemas`
- Entity 通过 `toDomainModel()` 转换为领域模型，领域模型通过 `toEntity()` 转为 Entity

## 理由

- Room 是 Jetpack 官方持久化方案，长期维护有保障
- KSP 比 KAPT 编译速度快约 2 倍，且是 Kotlin 2.0+ 的推荐注解处理方案
- 编译时 SQL 校验能提前捕获 SQL 语法错误
- Schema 导出支持数据迁移测试和版本管理
- 约定插件封装后，数据库模块只需应用两个插件即可

## 影响

### 正面影响

- 编译时校验 SQL 查询（表名、列名、类型匹配）
- KSP 增量编译性能优于 KAPT
- 自动生成的 DAO 实现代码减少模板
- Schema 导出提供数据迁移的可见性和可测试性

### 负面影响

- 三层模型转换：DTO（网络）→ Entity（数据库）→ 领域模型（业务）
- Schema 版本变更需要编写 Migration 代码或 AutoMigration 注解
- 数据库测试需要 Room 内存数据库 + 真实 Migration 验证

### 中立影响

- 时间处理使用 `kotlinx.datetime.Instant`，需要自定义 TypeConverter

---

## 遵循情况

- 数据库 Entity 必须放在 `core/database/model/`，以 `Entity` 结尾
- DAO 接口必须放在 `core/database/dao/`
- 不允许在模块外直接传递 Entity，必须转为领域模型
- 修改 Entity 时必须增加版本号并编写 Migration
