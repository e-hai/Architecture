#!/usr/bin/env bash

# 遇到错误立即退出
set -e

# 检测运行平台（macOS vs Linux），sed -i 语法不同
if [[ "$(uname)" == "Darwin" ]]; then
    SED_INPLACE=("sed" "-i" "")
else
    SED_INPLACE=("sed" "-i")
fi

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

read -p "请输入新的应用名称 (App Display Name, 默认 My App): " NEW_APP_NAME
if [ -z "$NEW_APP_NAME" ]; then
    NEW_APP_NAME="My App"
fi

echo -e "\n${YELLOW}[信息] 开始初始化项目...${NC}"
echo -e "- 新命名空间 (Namespace): ${GREEN}$NEW_NAMESPACE${NC}"
echo -e "- 新应用 ID (ApplicationId): ${GREEN}$NEW_APP_ID${NC}"
echo -e "- 约定插件前缀: ${GREEN}$NEW_PREFIX${NC}"
echo -e "- 应用名称: ${GREEN}$NEW_APP_NAME${NC}\n"

# 将点分包名转换为物理路径 (e.g. com.example.myapp -> com/example/myapp)
OLD_PATH="xxx/yyy/zzz"
NEW_PATH=$(echo "$NEW_NAMESPACE" | tr '.' '/')

# 2. 全局文本替换
echo -e "${YELLOW}[1/4] 正在执行全局文本内容替换...${NC}"

find_and_replace() {
    local find_args=("$@")
    # 替换命名空间/包名文本以及自定义插件前缀
    find . -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" -o -name "*.toml" -o -name "*.json" -o -name "*.pro" \) \
        -not -path '*/.*' -not -path '*/build/*' -not -path '*/bin/*' -not -path './init_project.sh' | while read -r file; do

        # 替换包名/命名空间占位符
        "${SED_INPLACE[@]}" "s/xxx\.yyy\.zzz/${NEW_NAMESPACE}/g" "$file"
        # 替换插件前缀占位符
        "${SED_INPLACE[@]}" "s/myproject/${NEW_PREFIX}/g" "$file"
        # 替换项目名称占位符（大小写敏感变体）
        "${SED_INPLACE[@]}" "s/MyProjectScaffold/${NEW_APP_NAME// /}Scaffold/g" "$file"
        "${SED_INPLACE[@]}" "s/MyProjectTheme/${NEW_APP_NAME// /}Theme/g" "$file"
        "${SED_INPLACE[@]}" "s/MyProjectAppName/${NEW_APP_NAME// /}AppName/g" "$file"
    done

    # 替换 XML 和 strings 中的显示名称
    find . -type f \( -name "*.xml" \) \
        -not -path '*/.*' -not -path '*/build/*' | while read -r file; do
        "${SED_INPLACE[@]}" "s/My App/${NEW_APP_NAME}/g" "$file"
        "${SED_INPLACE[@]}" "s/我的应用/${NEW_APP_NAME}/g" "$file"
    done

    # 针对 app 壳模块的 build.gradle.kts 替换独立的 applicationId（防假设 namespace 与 applicationId 一致）
    if [ "$NEW_NAMESPACE" != "$NEW_APP_ID" ]; then
        "${SED_INPLACE[@]}" "s/applicationId = \"${NEW_NAMESPACE}\"/applicationId = \"${NEW_APP_ID}\"/g" app/build.gradle.kts
    fi
}

find_and_replace
echo -e "${GREEN}✔ 文本替换完成。${NC}"

# 3. 物理目录结构重建与文件迁移
echo -e "${YELLOW}[2/4] 正在移动物理文件夹以匹配新包路径...${NC}"

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

# 4. 更新 settings.gradle.kts 中的 rootProject.name
echo -e "${YELLOW}[3/4] 正在更新项目名称...${NC}"
"${SED_INPLACE[@]}" "s/rootProject\.name = \"[^\"]*\"/rootProject.name = \"${NEW_APP_NAME// /}\"/" settings.gradle.kts

echo -e "${GREEN}✔ 项目名称更新完成。${NC}"

# 5. 项目格式化与编译验证
echo -e "${YELLOW}[4/4] 正在自动触发 Spotless 代码格式化...${NC}"
chmod +x gradlew
./gradlew spotlessApply || echo -e "${YELLOW}[警告] Spotless 格式化失败，可能需要手动修复语法。${NC}"

echo -e "\n${GREEN}====================================================${NC}"
echo -e "${GREEN}🎉 项目初始化成功！${NC}"
echo -e "Tip: 运行 ${BLUE}./gradlew assemble${NC} 验证项目能否正常编译"
echo -e "你可以立即在 Android Studio 中导入并运行该项目。${NC}"
echo -e "${GREEN}====================================================${NC}"
