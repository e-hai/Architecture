# ADR-009: 多语言文案采用 Excel 驱动的生成方案

## 状态

Accepted

---

## 背景

随着模块数量增加（app、feature-home-impl、feature-settings-impl 等），手写 `strings.xml` 维护多语言文案出现以下问题：

1. **文案重复**：`app/` 和 `feature/home/impl/` 有 5 个 `home_*` key 完全重复，3 个 `detail_*`/`result_*` key 语义重复但命名不同
2. **协作成本高**：产品经理通过口头或邮件传递文案，开发人员手动写入不同模块的 `values/strings.xml` 和 `values-zh/strings.xml`，容易遗漏或写错
3. **新增语言负担重**：每新增一种语言，需要在每个模块的 `values-{lang}/` 下手动创建 `strings.xml`，且内容需逐一确认

需要一个规范的协作流程：产品经理维护文案的权威来源，开发侧一键生成各模块的资源文件。

## 备选方案

### 方案 A：多文件多 sheet（多个 xlsx，每个模块一个）

每个 Android 模块对应一个独立的 xlsx 文件，各模块各自管理自己的文案。

**优点：**
- 文件粒度与模块边界完全对齐

**缺点：**
- PM 需要打开多个文件才能看到全局翻译进度
- 新增模块需要新建一个文件，文件数量随模块增长线性增加
- 项目管理成本更高

### 方案 B：共享 UI 模块统一托管文案

在 `:core:ui` 中创建 `res/` 目录，所有公共文案集中管理，feature 模块只放本模块独有的文案。

**优点：**
- 公共文案单一定义，杜绝重复

**缺点：**
- 需要模块间建立统一的资源前缀和引用规则
- 运行时资源合并会增加查找开销
- feature 模块的部分文案（如 `home_discover`）归属模糊，既属于 feature 又可能被 app 引用

### 方案 C（选定）：单文件多 sheet + Python 脚本生成

一个 `strings.xlsx`，每个模块一个 sheet，脚本遍历生成各模块的 `strings.xml`。

**优点：**
- PM 只需管理一个文件，无需 Android 开发环境
- 一个 sheet 对应一个 Android 模块，与项目架构对齐
- 新增语言只需在对应 sheet 追加一列，脚本自动生成 `values-{lang}/strings.xml`
- 新模块只需新增一个 sheet 并更新脚本映射表

**缺点：**
- 引入 Python 依赖 `openpyxl`（开发环境需要安装）
- 生成脚本需要与 xlsx 保持同步（新增模块需手动更新映射表）
- 不是 Gradle 原生集成，需要单独运行脚本

### 方案 D：Gradle Task 包装脚本

将 Python 脚本包装为 Gradle Exec Task，使其成为构建的一部分。

**优点：**
- 与现有构建工具链整合，`./gradlew generateI18n` 即可运行

**缺点：**
- PM 仍需要 Android 环境才能运行
- 文案变更不应触发全量构建
- 不属于日常构建的必需步骤，自动执行反而增加无谓等待

## 决策

采用**方案 C**：单文件多 sheet + Python 脚本生成。

具体约定：

- **文案来源**：`i18n/strings.xlsx`，每个 sheet 对应一个 Android 模块
- **表格格式**：首行语言代码（首列为 `key`），后续每行一个 key 及各语言翻译
- **默认语言**：`en`（表格第一列），生成 `values/strings.xml`
  - 其他列生成 `values-{lang}/strings.xml`
- **生成脚本**：`scripts/generate-i18n-strings.py`，使用 Python + `openpyxl`
- **运行方式**：`pip install openpyxl && python scripts/generate-i18n-strings.py`
- **不纳入 Gradle 构建**：保持 PM 可独立使用，无需 Android 环境

### 路径映射

| Sheet 名称 | 目标模块 res 路径 |
|---|---|
| `app` | `app/src/main/res/` |
| `feature-home-impl` | `feature/home/impl/src/main/res/` |
| `feature-settings-impl` | `feature/settings/impl/src/main/res/` |

### 新增语言的流程

1. 在 `strings.xlsx` 每个 sheet 中追加一列，语言代码作为列名
2. PM 填写各 key 的对应翻译
3. 运行生成脚本，各模块的 `values-{lang}/strings.xml` 自动创建

### 新增模块的流程

1. 在 `strings.xlsx` 中新建 sheet，命名遵循模块标识规则
2. 在 `scripts/generate-i18n-strings.py` 的 `SHEET_TO_RES` 映射表中添加对应路径
3. PM 填写文案，运行脚本生成资源文件

## 理由

- **协作效率**：PM 只需维护一个 Excel 文件，无需了解 Android 资源结构
- **与模块化架构对齐**：每个 sheet 对应一个 Android 模块，职责清晰
- **消除重复**：脚本每次覆盖生成，确保各模块的 `strings.xml` 与 xlsx 完全一致，不再出现手工维护导致的内容漂移
- **低成本扩展**：新增语言 = 加一列，新增模块 = 加一个 sheet + 映射表一行
- **无侵入性**：不修改 Gradle 构建流程，团队可快速上手

## 影响

### 正面影响

- PM 与开发团队之间的文案传递从「口头/邮件」变为「xlsx → 脚本」
- `strings.xml` 内容与 xlsx 保持同步，不再出现手工维护导致的重复和遗漏
- 新增语言的沟通和执行成本大幅降低
- 与项目现有的 api/impl 模块拆分架构一致

### 负面影响

- 开发环境需要安装 `openpyxl`（`pip install` 一次即可）
- 运行脚本后才看到文案变更生效，不再是改 XML 立即可见
- 需要约束团队不直接修改生成的 `strings.xml`（以 xlsx 为准）

### 中立影响

- `scripts/` 目录增加一个 Python 脚本，项目技术栈增加了 Python 依赖
- `i18n/` 目录存储商业数据（翻译文案），与 `src/` 的代码技术数据分离

---

## 遵循情况

- `i18n/strings.xlsx` 是文案的唯一权威来源
- 生成后的 `strings.xml` 不应手动修改
- 新增模块时必须在 `SHEET_TO_RES` 映射表中添加对应路径
- 代码审查时检查 `strings.xml` 是否与 xlsx 一致
