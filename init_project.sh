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
echo -e "${BLUE}    Kotlin Multiplatform (KMP) Architecture Init    ${NC}"
echo -e "${BLUE}====================================================${NC}"

# 解析命令行参数
DRY_RUN=false
while [[ $# -gt 0 ]]; do
    case "$1" in
        --dry-run) DRY_RUN=true; shift ;;
        --force) FORCE=true; shift ;;
        *) break ;;
    esac
done

# 1. 引导交互输入
read -p "请输入新的命名空间 (Namespace, 比如 com.example.myapp): " NEW_NAMESPACE
if [ -z "$NEW_NAMESPACE" ]; then
    echo -e "${RED}[错误] 命名空间不能为空！${NC}"
    exit 1
fi

if ! echo "$NEW_NAMESPACE" | grep -qE '^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$'; then
    echo -e "${YELLOW}[警告] 命名空间格式非标准，请确认（合法格式: com.example.myapp）${NC}"
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
echo -e "- 应用名称: ${GREEN}$NEW_APP_NAME${NC}"

if [ "$DRY_RUN" = true ]; then
    echo -e "\n${YELLOW}[--dry-run 模式] 以上为将要应用的变更。未做任何实际修改。${NC}"
    exit 0
fi

OLD_PATH="xxx/yyy/zzz"
NEW_PATH=$(echo "$NEW_NAMESPACE" | tr '.' '/')

# 2. 全局文本替换
echo -e "${YELLOW}[1/4] 正在执行全局文本内容替换...${NC}"

find . -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" -o -name "*.toml" -o -name "*.json" -o -name "*.pro" -o -name "*.swift" \) \
    -not -path '*/.*' -not -path '*/build/*' -not -path '*/bin/*' -not -path './init_project.sh' | while read -r file; do
    "${SED_INPLACE[@]}" "s/xxx\.yyy\.zzz/${NEW_NAMESPACE}/g" "$file"
    "${SED_INPLACE[@]}" "s/myproject/${NEW_PREFIX}/g" "$file"
    "${SED_INPLACE[@]}" "s/MyProjectTheme/${NEW_APP_NAME// /}Theme/g" "$file"
done

# 替换 Android 独立宿主应用 ID
if [ "$NEW_NAMESPACE" != "$NEW_APP_ID" ]; then
    "${SED_INPLACE[@]}" "s/applicationId = \"${NEW_NAMESPACE}\"/applicationId = \"${NEW_APP_ID}\"/g" androidApp/build.gradle.kts
fi

echo -e "${GREEN}✔ 文本替换完成。${NC}"

# 3. 物理目录结构迁移
echo -e "${YELLOW}[2/4] 正在移动源码文件夹以匹配新包路径...${NC}"

find . -type d \( -path "*/src/*/kotlin/${OLD_PATH}" -o -path "*/src/*/java/${OLD_PATH}" \) | while read -r old_dir; do
    base_dir=$(echo "$old_dir" | sed "s/\/${OLD_PATH}//g")
    mkdir -p "${base_dir}/${NEW_PATH}"
    mv "${old_dir}"/* "${base_dir}/${NEW_PATH}/"
    rm -rf "${base_dir}/xxx"
done

echo -e "${GREEN}✔ 物理路径迁移完成。${NC}"

# 4. 更新 settings.gradle.kts 中的 rootProject.name
echo -e "${YELLOW}[3/4] 正在更新项目名称...${NC}"
"${SED_INPLACE[@]}" "s/rootProject\.name = \"[^\"]*\"/rootProject.name = \"${NEW_APP_NAME// /}\"/" settings.gradle.kts

echo -e "${GREEN}✔ 项目名称更新完成。${NC}"

# 5. 格式化代码
echo -e "${YELLOW}[4/4] 正在自动触发 Spotless 代码格式化...${NC}"
chmod +x gradlew
./gradlew spotlessApply || echo -e "${YELLOW}[警告] Spotless 格式化失败，可能需要手动修复语法。${NC}"

echo -e "\n${GREEN}====================================================${NC}"
echo -e "${GREEN}🎉 KMP 项目初始化成功！${NC}"
echo -e "已应用的配置："
echo -e "  命名空间:    ${NEW_NAMESPACE}"
echo -e "  应用 ID:     ${NEW_APP_ID}"
echo -e "  插件前缀:    ${NEW_PREFIX}"
echo -e "  应用名称:    ${NEW_APP_NAME}"
echo -e "====================================================${NC}"
