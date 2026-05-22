# Android 现代离线优先项目脚手架设计与初始化指南

本指南定义了遵循 **Now in Android** 离线优先、Koin 依赖注入与 Gradle 约定插件规范的通用 Android 项目脚手架设计。当需要 AI 或开发者生成新的项目骨架时，应严格遵循本契约。

---

## 一、 脚手架物理目录结构

所有应用 ID、命名空间和包名统一使用占位符 `xxx/yyy/zzz`，结构如下：

```text
my-scaffold-project/
├── build-logic/                # 约定插件目录（独立构建，集中管理构建逻辑）
│   ├── convention/
│   │   ├── src/main/kotlin/    # 约定插件源码（如 AndroidFeatureConventionPlugin）
│   │   └── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle.properties
├── gradle/
│   └── libs.versions.toml      # Gradle Version Catalog（全项目版本单一数据源）
├── app/                        # 壳 Application 模块（注入入口，Activity 承载，无业务）
│   └── src/main/
│       ├── java/xxx/yyy/zzz/
│       │   ├── MainActivity.kt # 主入口 Activity
│       │   └── MyApplication.kt # Koin 启动入口
│       └── AndroidManifest.xml
├── feature/                    # 业务功能模块目录
│   └── home/                   # 示例 Feature 模块（必须拆分 api 与 impl）
│       ├── api/                # 对外暴露的接口、数据类和 NavKey
│       └── impl/               # 内部 UI (Compose) 与 ViewModel 实现
├── core/                       # 核心基础设施层
│   ├── model/                  # 纯 Kotlin 领域模型定义
│   ├── domain/                 # 纯 Kotlin 业务用例（UseCase）与 Repository 接口
│   ├── data/                   # 离线优先仓库实现（RepositoryImpl），组合 Remote 与 Local
│   ├── database/               # Room 数据库配置、Entity 实体及 DAO 接口
│   ├── datastore/              # DataStore<Preferences> 本地键值对存储
│   ├── network/                # Retrofit & OkHttp 基础网络客户端及网络数据源
│   ├── navigation/             # 全局类型安全导航组件与自适应布局辅助
│   └── ui/                     # 统一主题系统与通用无状态 Composable 基础组件
├── build.gradle.kts            # 根构建脚本
├── settings.gradle.kts         # 根设置脚本
├── gradle.properties
└── init_project.sh             # 一键项目初始化脚本
```

---

## 二、 核心占位符定义

AI 在生成脚手架或初始化脚本时，必须遵循以下固定的文本占位符，以便进行无损全局替换：

| 占位符 | 代表含义 | 替换示例 |
| :--- | :--- | :--- |
| `xxx.yyy.zzz` | 默认全局包名与命名空间 (Namespace) | `com.company.myapp` |
| `xxx/yyy/zzz` | 默认物理文件夹路径结构 | `com/company/myapp` |
| `myproject` | 约定插件 ID 前缀占位符 | `google` (对应 `google.android.feature`) |

---

## 三、 一键项目初始化脚本 (`init_project.sh`)

项目脚手架根目录下必须预置该脚本。当开发者拉取脚手架后，只需执行此脚本即可一键将其转化为特定包名的真实项目。

### 脚本源码

```bash
#!/usr/bin/env bash

# 遇到错误立即退出
set -e

# 配色输出定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}    Android Modern Architecture Scaffold Init       ${NC}"
echo -e "${BLUE}====================================================${NC}"

# 1. 引导交互输入
read -p "请输入新的命名空间 (Namespace, 比如 com.example.myapp): " NEW_NAMESPACE
if [ -z "$NEW_NAMESPACE" ]; then
    echo -e "${RED}[错误] 命名空间不能为空！${NC}"
    exit 1
fi

read -p "请输入新的应用 ID (ApplicationId, 默认同命名空间): " NEW_APP_ID
if [ -z "$NEW_APP_ID" ]; then
    NEW_APP_ID=$NEW_NAMESPACE
fi

read -p "请输入新的约定插件前缀 (Plugin Prefix, 默认 myproject): " NEW_PREFIX
if [ -z "$NEW_PREFIX" ]; then
    NEW_PREFIX="myproject"
fi

echo -e "\n${YELLOW}[信息] 开始初始化项目...${NC}"
echo -e "- 新命名空间 (Namespace): ${GREEN}$NEW_NAMESPACE${NC}"
echo -e "- 新应用 ID (ApplicationId): ${GREEN}$NEW_APP_ID${NC}"
echo -e "- 约定插件前缀: ${GREEN}$NEW_PREFIX${NC}\n"

# 将点分包名转换为物理路径 (e.g. com.example.myapp -> com/example/myapp)
OLD_PATH="xxx/yyy/zzz"
NEW_PATH=$(echo "$NEW_NAMESPACE" | tr '.' '/')

# 2. 全局文本替换
echo -e "${YELLOW}[1/3] 正在执行全局文本内容替换...${NC}"

find_and_replace() {
    # 替换命名空间/包名文本以及自定义插件前缀
    find . -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" -o -name "*.toml" \) \
        -not -path '*/.*' -not -path '*/build/*' -not -path './init_project.sh' | while read -r file; do
        
        # 替换包名/命名空间占位符
        sed -i '' "s/xxx.yyy.zzz/${NEW_NAMESPACE}/g" "$file"
        # 替换插件前缀占位符
        sed -i '' "s/myproject/${NEW_PREFIX}/g" "$file"
    done
    
    # 针对 app 壳模块的 build.gradle.kts 替换独立的 applicationId（防假设 namespace 与 applicationId 一致）
    if [ "$NEW_NAMESPACE" != "$NEW_APP_ID" ]; then
        sed -i '' "s/applicationId = \"${NEW_NAMESPACE}\"/applicationId = \"${NEW_APP_ID}\"/g" app/build.gradle.kts
    fi
}

find_and_replace
echo -e "${GREEN}✔ 文本替换完成。${NC}"

# 3. 物理目录结构重建与文件迁移
echo -e "${YELLOW}[2/3] 正在移动物理文件夹以匹配新包路径...${NC}"

find . -type d -path "*/src/*/java/${OLD_PATH}" | while read -r old_dir; do
    # 提取物理 java 根路径 (e.g. ./app/src/main/java)
    target_parent=$(echo "$old_dir" | sed "s/\/java\/${OLD_PATH}//g")/java
    
    # 创建新的深层目录结构 (e.g. ./app/src/main/java/com/example/myapp)
    mkdir -p "${target_parent}/${NEW_PATH}"
    
    # 将旧占位包下的所有源码物理移动至新包路径下
    mv "${old_dir}"/* "${target_parent}/${NEW_PATH}/"
    
    # 级联清理旧的占位物理目录
    rm -rf "${target_parent}/xxx"
done

echo -e "${GREEN}✔ 物理路径迁移完成。${NC}"

# 4. 项目格式化与编译验证
echo -e "${YELLOW}[3/3] 正在自动触发 Spotless 代码格式化...${NC}"
chmod +x gradlew
./gradlew spotlessApply || echo -e "${YELLOW}[警告] Spotless 格式化失败，可能需要手动修复语法。${NC}"

echo -e "\n${GREEN}====================================================${NC}"
echo -e "${GREEN}🎉 项目初始化成功！${NC}"
echo -e "你可以立即在 Android Studio 中导入并运行该项目。${NC}"
echo -e "${GREEN}====================================================${NC}"
```

---

## 四、 AI 协助生成脚手架最佳工作流

当您将此文件作为 Context 传递给 AI 助手时，可以使用以下步骤引导 AI 为您瞬间构建项目：

### 第一步：初始化核心配置
> 💬 **指令**：*“请阅读项目 `SCAFFOLDING.md` 中定义的架构。首先为我生成根目录的 `settings.gradle.kts`、`build.gradle.kts` 以及 `gradle/libs.versions.toml`，引入必要的基础构建依赖。”*

### 第二步：生成约定插件（build-logic）
> 💬 **指令**：*“现在请为我编写 `build-logic:convention` 中最核心的 `myproject.android.application` 和 `myproject.android.feature` 约定插件，要求使用 Version Catalog 配置 Java 17、Compose 并确保封装了 Spotless 格式化逻辑。”*

### 第三步：生成 Core 层与 Feature 骨架
> 💬 **指令**：*“请在相应模块中，使用 `xxx.yyy.zzz` 作为占位包名生成 `core:data`、`core:database` 的基座代码，并生成一个 `feature:home:api` 与 `feature:home:impl` 的最简模块以展示单向依赖流与 Koin 绑定。”*

### 第四步：输出 init_project.sh 脚本
> 💬 **指令**：*“最后，请将 `SCAFFOLDING.md` 中的 `init_project.sh` 一键替换脚本写入项目根目录，并赋予执行权限。”*
