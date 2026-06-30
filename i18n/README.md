# i18n 多语言配置

本项目多语言文案管理方案：**一个 `strings.xlsx` 文件 + 每个模块一个 sheet + 脚本生成 `strings.xml`**。

---

## 目录结构

```
i18n/
├── strings.xlsx                # 文案数据源（PM 维护）
├── generate-i18n-strings.py    # 生成脚本
└── README.md                   # 本文件
```

## 前提条件

```bash
pip install openpyxl
```

## 运行方法

```bash
python i18n/generate-i18n-strings.py
```

脚本会读取 `i18n/strings.xlsx`，为每个 sheet 生成对应模块的 `values/strings.xml`（默认语言）和 `values-{lang}/strings.xml`（其他语言）。

## Excel 格式

| key | en | zh | ja | ... |
|-----|----|----|----|-----|
| app_name | My App | 我的应用 | マイアプリ | ... |

- **一个 sheet 对应一个 Android 模块**，sheet 名称与模块的映射关系见脚本中的 `SHEET_TO_RES`
- **首行**：语言代码，首列固定为 `key`
- **首列**（默认语言 `en`）→ 生成 `values/strings.xml`
- **其他列** → 生成 `values-{lang}/strings.xml`
- **key 命名**：小写字母、数字、下划线
- **占位符**：使用 `%1$s`、`%2$d` 标准 Android 格式

## 新增语言

在每个 sheet 中追加一列，列名为语言代码（如 `ja`），填写翻译后重新运行脚本即可。

## 新增模块

1. 在 `strings.xlsx` 中新建 sheet，sheet 名称按模块标识命名
2. 在 `generate-i18n-strings.py` 的 `SHEET_TO_RES` 字典中添加对应 res 路径
3. PM 填写文案 → 运行脚本

## 注意事项

- **文案以 xlsx 为唯一权威来源**，生成的 `strings.xml` 不应手动修改
- 脚本不纳入 Gradle 构建，PM 无需 Android 环境即可使用
- 详细架构决策见 `docs/adr/0009-i18n-excel-driven-strings.md`
