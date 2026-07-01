# Android Modern Architecture Scaffold

多模块 Android 项目骨架，用于快速启动真实业务项目。

## 快速开始

```bash
# 1. 替换占位符为真实项目参数
./init_project.sh

# 2. 验证编译
./gradlew assemble
```

`init_project.sh` 会交互式输入命名空间、应用 ID、插件前缀、应用名称、API Base URL，支持 `--dry-run` 预览。

## 添加第一个 Feature 模块

骨架不包含任何 feature 模块（均为 demo 代码已移除）。添加步骤如下：

```
feature/yourfeature/
├── api/    ← @Serializable NavKey、接口定义
└── impl/   ← Screen + ViewModel + Navigation 入口 + Koin 模块
```

以下文件需要注册新模块：

| 操作 | 文件 |
|---|---|
| `include(":feature:yourfeature:api")` + `:feature:yourfeature:impl` | `settings.gradle.kts` |
| `implementation(project(":feature:yourfeature:impl"))` | `app/build.gradle.kts` |
| 注册 Koin module | `app/.../MyApplication.kt` |
| 替换 `SkeletonNavKey` 为真实 NavKey | `app/.../AppNavGraph.kt` |
| 如需多语言文案 | `i18n/strings.xlsx` 新增 sheet + `i18n/generate-i18n-strings.py` 新增映射 |

## 常用命令

```bash
./gradlew assemble           # 全量编译
./gradlew assembleDebug      # 仅 Debug
./gradlew spotlessApply      # 代码格式化
./gradlew check              # 全部检查
```

## 文档索引

- [AGENTS.md](./AGENTS.md) — 架构规则、代码规范、禁止项（**团队/AI 权威参考**）
- [docs/adr/](./docs/adr/) — 架构决策记录（ADR）
- [i18n/README.md](./i18n/README.md) — 多语言文案管理
