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
NEW_NAMESPACE=""
NEW_APP_NAME=""
NEW_APP_ID=""
NEW_TEST_APP_ID=""
NEW_PREFIX=""
INITIAL_FEATURES=""
DRY_RUN=false
FORCE=false
AUTO_YES=false
NO_DEBUG=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --package|-p)
            NEW_NAMESPACE="$2"
            shift 2
            ;;
        --name|-n)
            NEW_APP_NAME="$2"
            shift 2
            ;;
        --app-id)
            NEW_APP_ID="$2"
            shift 2
            ;;
        --test-app-id)
            NEW_TEST_APP_ID="$2"
            shift 2
            ;;
        --prefix)
            NEW_PREFIX="$2"
            shift 2
            ;;
        --features|-f)
            INITIAL_FEATURES="$2"
            shift 2
            ;;
        --no-debug)
            NO_DEBUG=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --force)
            FORCE=true
            shift
            ;;
        --yes|-y)
            AUTO_YES=true
            shift
            ;;
        *)
            echo -e "${RED}[未知选项] $1${NC}"
            echo -e "用法: ./init_project.sh [--package <pkg>] [--name <app_name>] [--app-id <formal_id>] [--test-app-id <test_id>] [--prefix <prefix>] [--features <feat1,feat2>] [--no-debug] [--yes] [--dry-run] [--force]"
            exit 1
            ;;
    esac
done

NON_INTERACTIVE=false
if [ "$AUTO_YES" = true ] || [ ! -t 0 ]; then
    NON_INTERACTIVE=true
fi

# 1. 引导交互输入（若未通过命令行参数指定）
if [ -z "$NEW_NAMESPACE" ]; then
    if [ "$NON_INTERACTIVE" = true ]; then
        echo -e "${RED}[错误] 非交互模式下必须通过 --package <pkg> 指定命名空间！${NC}"
        exit 1
    fi
    read -p "请输入新的命名空间 (Namespace, 比如 com.example.myapp): " NEW_NAMESPACE
fi

if [ -z "$NEW_NAMESPACE" ]; then
    echo -e "${RED}[错误] 命名空间不能为空！${NC}"
    exit 1
fi

if ! echo "$NEW_NAMESPACE" | grep -qE '^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$'; then
    echo -e "${YELLOW}[警告] 命名空间格式非标准，请确认（合法格式: com.example.myapp）${NC}"
fi

# 智能推导默认插件前缀与应用名称
INFERRED_PREFIX=$(echo "$NEW_NAMESPACE" | awk -F. '{print $NF}')
INFERRED_APP_NAME=$(echo "$INFERRED_PREFIX" | awk '{print toupper(substr($0,1,1)) substr($0,2)}')

if [ -z "$NEW_APP_NAME" ]; then
    if [ "$NON_INTERACTIVE" = true ]; then
        NEW_APP_NAME="$INFERRED_APP_NAME"
    else
        read -p "请输入新的应用名称 (App Display Name, 默认 ${INFERRED_APP_NAME}): " NEW_APP_NAME
        if [ -z "$NEW_APP_NAME" ]; then
            NEW_APP_NAME="$INFERRED_APP_NAME"
        fi
    fi
fi

if [ -z "$NEW_APP_ID" ]; then
    if [ "$NON_INTERACTIVE" = true ]; then
        NEW_APP_ID="$NEW_NAMESPACE"
    else
        read -p "请输入正式应用 ID (ApplicationId, 默认同命名空间 ${NEW_NAMESPACE}): " NEW_APP_ID
        if [ -z "$NEW_APP_ID" ]; then
            NEW_APP_ID="$NEW_NAMESPACE"
        fi
    fi
fi

if [ -z "$NEW_TEST_APP_ID" ]; then
    if [ "$NON_INTERACTIVE" = true ]; then
        NEW_TEST_APP_ID="${NEW_APP_ID}.dev"
    else
        read -p "请输入测试应用 ID (Test ApplicationId, 默认 ${NEW_APP_ID}.dev): " NEW_TEST_APP_ID
        if [ -z "$NEW_TEST_APP_ID" ]; then
            NEW_TEST_APP_ID="${NEW_APP_ID}.dev"
        fi
    fi
fi

if [ -z "$NEW_PREFIX" ]; then
    if [ "$NON_INTERACTIVE" = true ]; then
        NEW_PREFIX="$INFERRED_PREFIX"
    else
        read -p "请输入约定插件前缀 (Plugin Prefix, 默认 ${INFERRED_PREFIX}): " NEW_PREFIX
        if [ -z "$NEW_PREFIX" ]; then
            NEW_PREFIX="$INFERRED_PREFIX"
        fi
    fi
fi

if [ -z "$INITIAL_FEATURES" ] && [ "$NON_INTERACTIVE" != true ]; then
    read -p "请输入需要一并创建的业务特性模块 (逗号分隔，如 profile,paywall，直接回车仅保留 home): " INITIAL_FEATURES
fi

echo -e "\n${YELLOW}[信息] 开始初始化项目...${NC}"
echo -e "- 新命名空间 (Namespace):    ${GREEN}$NEW_NAMESPACE${NC}"
echo -e "- 正式应用 ID (Formal App ID): ${GREEN}$NEW_APP_ID${NC}"
echo -e "- 测试应用 ID (Test App ID):   ${GREEN}$NEW_TEST_APP_ID${NC}"
echo -e "- 约定插件前缀:              ${GREEN}$NEW_PREFIX${NC}"
echo -e "- 应用名称:                  ${GREEN}$NEW_APP_NAME${NC}"
if [ -n "$INITIAL_FEATURES" ]; then
    echo -e "- 待创建初始业务模块:        ${GREEN}$INITIAL_FEATURES${NC}"
fi
if [ "$NO_DEBUG" = true ]; then
    echo -e "- 调试控制台:                ${YELLOW}已禁用 (--no-debug，将移除 :features:debug)${NC}"
fi

if [ "$DRY_RUN" = true ]; then
    echo -e "\n${YELLOW}[--dry-run 模式] 以上为将要应用的变更。未做任何实际修改。${NC}"
    exit 0
fi

OLD_PATH="xxx/yyy/zzz"
OLD_DOT_PKG="xxx.yyy.zzz"
NEW_PATH=$(echo "$NEW_NAMESPACE" | tr '.' '/')

THEME_PREFIX=$(echo "$NEW_APP_NAME" | tr -cd '[:alnum:]')
if [ -z "$THEME_PREFIX" ] || [[ "$THEME_PREFIX" =~ ^[0-9] ]]; then
    THEME_PREFIX="App${THEME_PREFIX}"
fi
APP_NAME_COMPACT="${NEW_APP_NAME// /}"
ESCAPED_APP_NAME=$(printf '%s\n' "$NEW_APP_NAME" | sed 's/[&|\\]/\\&/g')

# 2. 全局文本替换
echo -e "${YELLOW}[1/6] 正在执行全局文本内容替换...${NC}"

find . -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" -o -name "*.toml" -o -name "*.json" -o -name "*.pro" -o -name "*.swift" -o -name "*.yml" -o -name "*.yaml" -o -name "*.pbxproj" -o -name "create_feature.sh" -o -name "remove_feature.sh" -o -name "manage_kit.sh" -o -name "*.example" -o -name "*.properties" -o -name "*.xcconfig" \) \
    -not -path '*/.git/*' -not -path '*/.gradle/*' -not -path '*/.idea/*' -not -path '*/.kotlin/*' -not -path '*/.swiftpm/*' -not -path '*/.build/*' -not -path '*/xcuserdata/*' -not -path '*/build/*' -not -path '*/bin/*' -not -path './init_project.sh' | while read -r file; do
    "${SED_INPLACE[@]}" "s|xxx\.yyy\.zzz|${NEW_NAMESPACE}|g" "$file"
    "${SED_INPLACE[@]}" "s|myproject|${NEW_PREFIX}|g" "$file"
    "${SED_INPLACE[@]}" "s|MyProjectTheme|${THEME_PREFIX}Theme|g" "$file"
    "${SED_INPLACE[@]}" "s|INFOPLIST_KEY_CFBundleDisplayName: \"Architecture (Dev)\"|INFOPLIST_KEY_CFBundleDisplayName: \"${ESCAPED_APP_NAME} (Dev)\"|g" "$file"
    "${SED_INPLACE[@]}" "s|INFOPLIST_KEY_CFBundleDisplayName: \"Architecture\"|INFOPLIST_KEY_CFBundleDisplayName: \"${ESCAPED_APP_NAME}\"|g" "$file"
    "${SED_INPLACE[@]}" "s|INFOPLIST_KEY_CFBundleDisplayName: Architecture|INFOPLIST_KEY_CFBundleDisplayName: \"${ESCAPED_APP_NAME}\"|g" "$file"
    "${SED_INPLACE[@]}" "s|Architecture (Dev)|${ESCAPED_APP_NAME} (Dev)|g" "$file"
    "${SED_INPLACE[@]}" "s|APP_NAME=Architecture$|APP_NAME=${ESCAPED_APP_NAME}|g" "$file"
    "${SED_INPLACE[@]}" "s|INFOPLIST_KEY_CFBundleDisplayName = Architecture;|INFOPLIST_KEY_CFBundleDisplayName = \"${ESCAPED_APP_NAME}\";|g" "$file"
    "${SED_INPLACE[@]}" "s|INFOPLIST_KEY_CFBundleDisplayName = Architecture$|INFOPLIST_KEY_CFBundleDisplayName = ${ESCAPED_APP_NAME}|g" "$file"
    "${SED_INPLACE[@]}" "s|>Architecture<|>${ESCAPED_APP_NAME}<|g" "$file"
    "${SED_INPLACE[@]}" "s|>Welcome to KMP Architecture<|>Welcome to ${ESCAPED_APP_NAME}<|g" "$file"
    "${SED_INPLACE[@]}" "s|KMP Architecture CI|KMP ${APP_NAME_COMPACT} CI|g" "$file"
done

# 同步核心工程文档中的包名、插件前缀、主题名与根目录展示名（避免误改英文单词 Architecture）
for doc_file in README.md AGENTS.md docs/ARCHITECTURE.md; do
    if [ -f "$doc_file" ]; then
        "${SED_INPLACE[@]}" "s|xxx\.yyy\.zzz|${NEW_NAMESPACE}|g" "$doc_file"
        "${SED_INPLACE[@]}" "s|myproject|${NEW_PREFIX}|g" "$doc_file"
        "${SED_INPLACE[@]}" "s|MyProjectTheme|${THEME_PREFIX}Theme|g" "$doc_file"
        "${SED_INPLACE[@]}" "s|^Architecture/|${APP_NAME_COMPACT}/|g" "$doc_file"
    fi
done

# 替换正式应用 ID 与测试应用 ID
if [ "$NEW_APP_ID" != "$NEW_NAMESPACE" ]; then
    for prod_file in config/prod.properties.example config/prod.properties; do
        if [ -f "$prod_file" ]; then
            "${SED_INPLACE[@]}" "s|APP_ID=${NEW_NAMESPACE}$|APP_ID=${NEW_APP_ID}|g" "$prod_file"
        fi
    done
    if [ -f "iosApp/iosApp.xcodeproj/project.pbxproj" ]; then
        "${SED_INPLACE[@]}" "s|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_NAMESPACE};|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_APP_ID};|g" iosApp/iosApp.xcodeproj/project.pbxproj
    fi
    "${SED_INPLACE[@]}" "s|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_NAMESPACE}$|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_APP_ID}|g" iosApp/Config/Prod.xcconfig 2>/dev/null || true
    "${SED_INPLACE[@]}" "s|PRODUCT_BUNDLE_IDENTIFIER: ${NEW_NAMESPACE}$|PRODUCT_BUNDLE_IDENTIFIER: ${NEW_APP_ID}|g" iosApp/project.yml 2>/dev/null || true
fi

if [ "$NEW_TEST_APP_ID" != "${NEW_NAMESPACE}.dev" ]; then
    "${SED_INPLACE[@]}" "s|APP_ID=${NEW_NAMESPACE}\.dev$|APP_ID=${NEW_TEST_APP_ID}|g" config/dev.properties 2>/dev/null || true
    if [ -f "iosApp/iosApp.xcodeproj/project.pbxproj" ]; then
        "${SED_INPLACE[@]}" "s|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_NAMESPACE}\.dev;|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_TEST_APP_ID};|g" iosApp/iosApp.xcodeproj/project.pbxproj
    fi
    "${SED_INPLACE[@]}" "s|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_NAMESPACE}\.dev$|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_TEST_APP_ID}|g" iosApp/Config/Dev.xcconfig 2>/dev/null || true
    "${SED_INPLACE[@]}" "s|PRODUCT_BUNDLE_IDENTIFIER: ${NEW_NAMESPACE}\.dev$|PRODUCT_BUNDLE_IDENTIFIER: ${NEW_TEST_APP_ID}|g" iosApp/project.yml 2>/dev/null || true
fi

echo -e "${GREEN}✔ 文本替换完成。${NC}"

# 3. 物理源码目录结构迁移
echo -e "${YELLOW}[2/6] 正在移动源码文件夹以匹配新包路径...${NC}"

find . -type d \( -path "*/src/*/kotlin/${OLD_PATH}" -o -path "*/src/*/java/${OLD_PATH}" \) | while read -r old_dir; do
    base_dir="${old_dir%/${OLD_PATH}}"
    mkdir -p "${base_dir}/${NEW_PATH}"
    mv "${old_dir}"/* "${base_dir}/${NEW_PATH}/"
    rm -rf "${base_dir}/xxx"
done

# 4. Room 数据库 Schema 目录重命名
echo -e "${YELLOW}[3/6] 正在同步 Room Schema 快照目录...${NC}"
find core/database/schemas -maxdepth 1 -type d -name "${OLD_DOT_PKG}*" 2>/dev/null | while read -r schema_dir; do
    target_dir="${schema_dir/${OLD_DOT_PKG}/${NEW_NAMESPACE}}"
    mv "$schema_dir" "$target_dir"
done

echo -e "${GREEN}✔ 物理路径迁移完成。${NC}"

# 5. 更新 settings.gradle.kts 中的 rootProject.name 与重新生成 Xcode 工程
echo -e "${YELLOW}[4/6] 正在更新项目名称与 iOS Xcode 工程配置...${NC}"
"${SED_INPLACE[@]}" "s/rootProject\.name = \"[^\"]*\"/rootProject.name = \"${APP_NAME_COMPACT}\"/" settings.gradle.kts

if command -v xcodegen &> /dev/null; then
    echo -e "- 检测到 xcodegen，正在生成 iosApp.xcodeproj..."
    (cd iosApp && xcodegen generate --spec project.yml > /dev/null)
    echo -e "${GREEN}✔ iosApp.xcodeproj 生成完成。${NC}"
else
    echo -e "${YELLOW}[提示] 未检测到 xcodegen（iosApp.xcodeproj 已纳入 .gitignore，不随仓库提交）。请先安装 xcodegen（brew install xcodegen）并在 iosApp/ 下执行 xcodegen generate --spec project.yml 生成 Xcode 工程。${NC}"
fi

echo -e "${GREEN}✔ 项目名称与 iOS 配置更新完成。${NC}"

# 6. 格式化代码
echo -e "${YELLOW}[5/6] 正在自动触发 Spotless 代码格式化...${NC}"
chmod +x gradlew
./gradlew spotlessApply || echo -e "${YELLOW}[警告] Spotless 格式化失败，可能需要手动修复语法。${NC}"

# 7. 按需创建初始业务特性模块与可选裁剪 Debug 面板
if [ -n "$INITIAL_FEATURES" ]; then
    echo -e "\n${YELLOW}[6/6] 正在自动批量创建初始业务模块: ${INITIAL_FEATURES}...${NC}"
    chmod +x create_feature.sh
    IFS=',' read -ra FEAT_ARRAY <<< "$INITIAL_FEATURES"
    for feat in "${FEAT_ARRAY[@]}"; do
        feat_trimmed=$(echo "$feat" | xargs)
        if [ -n "$feat_trimmed" ] && [ "$feat_trimmed" != "home" ]; then
            echo -e "- 正在创建业务特性模块: ${GREEN}${feat_trimmed}${NC}"
            ./create_feature.sh "$feat_trimmed"
        fi
    done
fi

if [ "$NO_DEBUG" = true ] && [ -d "features/debug" ]; then
    echo -e "\n${YELLOW}[额外裁剪] 已指定 --no-debug，正在安全卸载 :features:debug 模块...${NC}"
    chmod +x remove_feature.sh
    ./remove_feature.sh debug
fi

echo -e "\n${GREEN}====================================================${NC}"
echo -e "${GREEN}🎉 KMP 项目初始化成功！${NC}"
echo -e "已应用的配置："
echo -e "  命名空间:        ${NEW_NAMESPACE}"
echo -e "  正式应用 ID:     ${NEW_APP_ID}"
echo -e "  测试应用 ID:     ${NEW_TEST_APP_ID}"
echo -e "  插件前缀:        ${NEW_PREFIX}"
echo -e "  应用名称:        ${NEW_APP_NAME}"
if [ -n "$INITIAL_FEATURES" ]; then
    echo -e "  初始业务模块:    ${INITIAL_FEATURES}"
fi
echo -e "\n${YELLOW}📋 上手前就绪检查清单 (Next Steps)：${NC}"
echo -e "  1. iOS 签名团队：在 iosApp/Config/Dev.xcconfig 与 Prod.xcconfig 填入您的 DEVELOPMENT_TEAM"
echo -e "  2. Android 发版配置：正式发版前复制 config/prod.properties.example 为 config/prod.properties 并填写真实签名库（详见 docs/sop/0001-app-signing-and-certificates.md）"
echo -e "  3. Firebase（按需启用 Analytics/ABTesting/CrashReport/Push）：确保 Firebase 后台注册包名为 ${NEW_TEST_APP_ID} / ${NEW_APP_ID}，再放入 androidApp/src/{dev,prod}/google-services.json 与 iosApp/Sources/GoogleService-Info.plist（随后执行 xcodegen generate）"
echo -e "====================================================${NC}"
