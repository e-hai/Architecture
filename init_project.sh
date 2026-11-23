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

print_usage() {
    echo -e "用法: ./init_project.sh [选项]"
    echo -e "  --package, -p <pkg>     新的命名空间 / 包名（如 com.example.myapp）"
    echo -e "  --name, -n <app_name>   应用显示名称"
    echo -e "  --app-id <formal_id>    正式版 ApplicationId / Bundle ID"
    echo -e "  --test-app-id <test_id> 测试版 ApplicationId / Bundle ID"
    echo -e "  --prefix <prefix>       Gradle 约定插件前缀"
    echo -e "  --features, -f <list>   批量创建的初始业务特性模块（逗号分隔）"
    echo -e "  --exclude-kits <list>   初始化时裁剪移除的 Kit 列表（逗号分隔，如 ads,video）"
    echo -e "  --no-debug              裁剪移除 :features:debug 调试控制台"
    echo -e "  --yes, -y               非交互模式，缺失可选项自动采用默认值"
    echo -e "  --dry-run               仅预览将要应用的变更，不实际修改文件"
    echo -e "  --force                 强制执行（即使当前工程未包含 xxx/yyy/zzz 模板目录）"
    echo -e ""
    echo -e "提示：如需通过网页可视化向导完成立项与参数配置，可运行 ./scripts/config_studio.sh --env dev"
}

require_arg_val() {
    if [ "$2" -lt 2 ] || [ -z "$3" ]; then
        echo -e "${RED}[错误] 选项 $1 需要一个参数值！${NC}"
        print_usage
        exit 1
    fi
}

# 解析命令行参数
NEW_NAMESPACE=""
NEW_APP_NAME=""
NEW_APP_ID=""
NEW_TEST_APP_ID=""
NEW_PREFIX=""
INITIAL_FEATURES=""
EXCLUDE_KITS=""
DRY_RUN=false
FORCE=false
AUTO_YES=false
NO_DEBUG=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --package|-p)
            require_arg_val "$1" "$#" "${2:-}"
            NEW_NAMESPACE="$2"
            shift 2
            ;;
        --name|-n)
            require_arg_val "$1" "$#" "${2:-}"
            NEW_APP_NAME="$2"
            shift 2
            ;;
        --app-id)
            require_arg_val "$1" "$#" "${2:-}"
            NEW_APP_ID="$2"
            shift 2
            ;;
        --test-app-id)
            require_arg_val "$1" "$#" "${2:-}"
            NEW_TEST_APP_ID="$2"
            shift 2
            ;;
        --prefix)
            require_arg_val "$1" "$#" "${2:-}"
            NEW_PREFIX="$2"
            shift 2
            ;;
        --features|-f)
            require_arg_val "$1" "$#" "${2:-}"
            INITIAL_FEATURES="$2"
            shift 2
            ;;
        --exclude-kits)
            require_arg_val "$1" "$#" "${2:-}"
            EXCLUDE_KITS="$2"
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
        --help|-h)
            print_usage
            exit 0
            ;;
        *)
            echo -e "${RED}[未知选项] $1${NC}"
            print_usage
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
    read -r -p "请输入新的命名空间 (Namespace, 比如 com.example.myapp): " NEW_NAMESPACE
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
        read -r -p "请输入新的应用名称 (App Display Name, 默认 ${INFERRED_APP_NAME}): " NEW_APP_NAME
        if [ -z "$NEW_APP_NAME" ]; then
            NEW_APP_NAME="$INFERRED_APP_NAME"
        fi
    fi
fi

if [ -z "$NEW_APP_ID" ]; then
    if [ "$NON_INTERACTIVE" = true ]; then
        NEW_APP_ID="$NEW_NAMESPACE"
    else
        read -r -p "请输入正式应用 ID (ApplicationId, 默认同命名空间 ${NEW_NAMESPACE}): " NEW_APP_ID
        if [ -z "$NEW_APP_ID" ]; then
            NEW_APP_ID="$NEW_NAMESPACE"
        fi
    fi
fi

if [ -z "$NEW_TEST_APP_ID" ]; then
    if [ "$NON_INTERACTIVE" = true ]; then
        NEW_TEST_APP_ID="${NEW_APP_ID}.dev"
    else
        read -r -p "请输入测试应用 ID (Test ApplicationId, 默认 ${NEW_APP_ID}.dev): " NEW_TEST_APP_ID
        if [ -z "$NEW_TEST_APP_ID" ]; then
            NEW_TEST_APP_ID="${NEW_APP_ID}.dev"
        fi
    fi
fi

if [ -z "$NEW_PREFIX" ]; then
    if [ "$NON_INTERACTIVE" = true ]; then
        NEW_PREFIX="$INFERRED_PREFIX"
    else
        read -r -p "请输入约定插件前缀 (Plugin Prefix, 默认 ${INFERRED_PREFIX}): " NEW_PREFIX
        if [ -z "$NEW_PREFIX" ]; then
            NEW_PREFIX="$INFERRED_PREFIX"
        fi
    fi
fi

if [ -z "$INITIAL_FEATURES" ] && [ "$NON_INTERACTIVE" != true ]; then
    read -r -p "请输入需要一并创建的业务特性模块 (逗号分隔，如 profile,paywall，直接回车仅保留 home): " INITIAL_FEATURES
fi

echo -e "\n${YELLOW}[信息] 开始初始化项目...${NC}"
echo -e "- 新命名空间 (Namespace):      ${GREEN}$NEW_NAMESPACE${NC}"
echo -e "- 正式默认 ID (Default Prod):  ${GREEN}$NEW_APP_ID${NC}"
echo -e "- 测试应用 ID (Test App ID):   ${GREEN}$NEW_TEST_APP_ID${NC}"
echo -e "- 约定插件前缀:                ${GREEN}$NEW_PREFIX${NC}"
echo -e "- 应用名称:                    ${GREEN}$NEW_APP_NAME${NC}"
if [ -n "$INITIAL_FEATURES" ]; then
    echo -e "- 待创建初始业务模块:          ${GREEN}$INITIAL_FEATURES${NC}"
fi
if [ "$NO_DEBUG" = true ]; then
    echo -e "- 调试控制台:                  ${YELLOW}已禁用 (--no-debug，将移除 :features:debug)${NC}"
fi
if [ -n "$EXCLUDE_KITS" ]; then
    echo -e "- 待裁剪移除的 Kit:            ${YELLOW}$EXCLUDE_KITS${NC}"
fi

if [ "$DRY_RUN" = true ]; then
    echo -e "\n${YELLOW}[--dry-run 模式] 以上为将要应用的变更。未做任何实际修改。${NC}"
    exit 0
fi

OLD_PATH="xxx/yyy/zzz"
OLD_DOT_PKG="xxx.yyy.zzz"

if [ "$FORCE" != true ] && [ ! -d "androidApp/src/main/kotlin/${OLD_PATH}" ]; then
    echo -e "${RED}[错误] 当前工程似乎已完成初始化（未找到 ${OLD_PATH} 模板目录）。如需强制执行请传入 --force。${NC}"
    exit 1
fi
NEW_PATH=$(echo "$NEW_NAMESPACE" | tr '.' '/')

THEME_PREFIX=$(echo "$NEW_APP_NAME" | tr -cd '[:alnum:]')
if [ -z "$THEME_PREFIX" ] || [[ "$THEME_PREFIX" =~ ^[0-9] ]]; then
    THEME_PREFIX="App${THEME_PREFIX}"
fi
APP_NAME_COMPACT="${NEW_APP_NAME// /}"
ESCAPED_APP_NAME=$(printf '%s\n' "$NEW_APP_NAME" | sed 's/[&|\\]/\\&/g')

# 2. 全局文本替换
echo -e "${YELLOW}[1/6] 正在执行全局文本内容替换...${NC}"

find . -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" -o -name "*.toml" -o -name "*.json" -o -name "*.pro" -o -name "*.swift" -o -name "*.yml" -o -name "*.yaml" -o -name "*.pbxproj" -o -name "create_feature.sh" -o -name "remove_feature.sh" -o -name "manage_kit.sh" -o -name "*.xcconfig" \) \
    -not -path '*/.git/*' -not -path '*/.gradle/*' -not -path '*/.idea/*' -not -path '*/.kotlin/*' -not -path '*/.swiftpm/*' -not -path '*/.build/*' -not -path '*/xcuserdata/*' -not -path '*/build/*' -not -path '*/bin/*' -not -path './init_project.sh' | while read -r file; do
    "${SED_INPLACE[@]}" "s|xxx\.yyy\.zzz|${NEW_NAMESPACE}|g" "$file"
    "${SED_INPLACE[@]}" "s|myproject|${NEW_PREFIX}|g" "$file"
    "${SED_INPLACE[@]}" "s|MyProjectTheme|${THEME_PREFIX}Theme|g" "$file"
    "${SED_INPLACE[@]}" "s|APP_NAME = Architecture (Dev)|APP_NAME = ${ESCAPED_APP_NAME} (Dev)|g" "$file"
    "${SED_INPLACE[@]}" "s|APP_NAME = Architecture$|APP_NAME = ${ESCAPED_APP_NAME}|g" "$file"
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

# 替换正式应用 ID 与测试应用 ID（统一 SSOT：config/dev.xcconfig 与 config/prod.xcconfig）
if [ "$NEW_APP_ID" != "$NEW_NAMESPACE" ]; then
    for prod_file in config/prod.xcconfig config/prod.local.xcconfig; do
        if [ -f "$prod_file" ]; then
            "${SED_INPLACE[@]}" "s|APP_ID = ${NEW_NAMESPACE}$|APP_ID = ${NEW_APP_ID}|g" "$prod_file"
        fi
    done
    if [ -f "iosApp/iosApp.xcodeproj/project.pbxproj" ]; then
        "${SED_INPLACE[@]}" "s|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_NAMESPACE};|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_APP_ID};|g" iosApp/iosApp.xcodeproj/project.pbxproj
    fi
fi

if [ "$NEW_TEST_APP_ID" != "${NEW_NAMESPACE}.dev" ]; then
    if [ -f "config/dev.xcconfig" ]; then
        "${SED_INPLACE[@]}" "s|APP_ID = ${NEW_NAMESPACE}\.dev$|APP_ID = ${NEW_TEST_APP_ID}|g" config/dev.xcconfig
    fi
    if [ -f "iosApp/iosApp.xcodeproj/project.pbxproj" ]; then
        "${SED_INPLACE[@]}" "s|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_NAMESPACE}\.dev;|PRODUCT_BUNDLE_IDENTIFIER = ${NEW_TEST_APP_ID};|g" iosApp/iosApp.xcodeproj/project.pbxproj
    fi
fi

echo -e "${GREEN}✔ 文本替换完成。${NC}"

# 3. 物理源码目录结构迁移
echo -e "${YELLOW}[2/6] 正在移动源码文件夹以匹配新包路径...${NC}"

find . -type d \( -path "*/src/*/kotlin/${OLD_PATH}" -o -path "*/src/*/java/${OLD_PATH}" \) | while read -r old_dir; do
    base_dir="${old_dir%/"${OLD_PATH}"}"
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

# 5. 更新 settings.gradle.kts 中的 rootProject.name
echo -e "${YELLOW}[4/6] 正在更新 Gradle 根工程名称...${NC}"
"${SED_INPLACE[@]}" "s/rootProject\.name = \"[^\"]*\"/rootProject.name = \"${APP_NAME_COMPACT}\"/" settings.gradle.kts
echo -e "${GREEN}✔ rootProject.name 已更新为 ${APP_NAME_COMPACT}。${NC}"

# 6. 结构裁剪（初始业务模块、Debug 面板、Kit 裁剪）
echo -e "${YELLOW}[5/6] 正在执行业务模块生成与按需模块裁剪...${NC}"
chmod +x gradlew ./*.sh scripts/*.sh

if [ -n "$INITIAL_FEATURES" ]; then
    echo -e "- 正在批量创建初始业务模块: ${GREEN}${INITIAL_FEATURES}${NC}"
    IFS=',' read -ra FEAT_ARRAY <<< "$INITIAL_FEATURES"
    for feat in "${FEAT_ARRAY[@]}"; do
        feat_trimmed=$(echo "$feat" | xargs)
        if [ -n "$feat_trimmed" ] && [ "$feat_trimmed" != "home" ]; then
            echo -e "  * 创建业务特性模块: ${GREEN}${feat_trimmed}${NC}"
            ./create_feature.sh "$feat_trimmed"
        fi
    done
fi

if [ "$NO_DEBUG" = true ] && [ -d "features/debug" ]; then
    echo -e "- 已启用禁用调试台，正在安全卸载 :features:debug 模块..."
    ./remove_feature.sh debug
fi

if [ -n "$EXCLUDE_KITS" ]; then
    echo -e "- 正在裁剪不需要的 Kit 模块: ${YELLOW}${EXCLUDE_KITS}${NC}"
    IFS=',' read -ra KIT_ARRAY <<< "$EXCLUDE_KITS"
    for kit in "${KIT_ARRAY[@]}"; do
        kit_trimmed=$(echo "$kit" | xargs)
        if [ -n "$kit_trimmed" ]; then
            ./manage_kit.sh remove "$kit_trimmed"
        fi
    done
fi

# 7. 重新生成 Xcode 工程、激活 Git Hook 并统一格式化
echo -e "${YELLOW}[6/6] 正在生成 iOS Xcode 工程、激活本地质量 Hook 并统一格式化代码...${NC}"
if command -v xcodegen &> /dev/null; then
    echo -e "- 检测到 xcodegen，正在生成 iosApp.xcodeproj..."
    (cd iosApp && xcodegen generate --spec project.yml > /dev/null)
    echo -e "${GREEN}✔ iosApp.xcodeproj 生成完成。${NC}"
else
    echo -e "${YELLOW}[提示] 未检测到 xcodegen（iosApp.xcodeproj 已纳入 .gitignore，不随仓库提交）。请先安装 xcodegen（brew install xcodegen）并在 iosApp/ 下执行 xcodegen generate --spec project.yml 生成 Xcode 工程。${NC}"
fi

if [ -d ".githooks" ]; then
    chmod +x .githooks/*
    if [ -d ".git" ]; then
        git config core.hooksPath .githooks || true
    fi
fi
./gradlew spotlessApply || echo -e "${YELLOW}[警告] Spotless 格式化失败，可能需要手动修复语法。${NC}"

echo -e "\n${GREEN}====================================================${NC}"
echo -e "${GREEN}🎉 KMP 项目初始化成功！${NC}"
echo -e "已应用的配置："
echo -e "  命名空间:        ${NEW_NAMESPACE}"
echo -e "  测试应用 ID:     ${NEW_TEST_APP_ID}"
echo -e "  正式默认 ID:     ${NEW_APP_ID}"
echo -e "  插件前缀:        ${NEW_PREFIX}"
echo -e "  应用名称:        ${NEW_APP_NAME}"
if [ -n "$INITIAL_FEATURES" ]; then
    echo -e "  初始业务模块:    ${INITIAL_FEATURES}"
fi
if [ -n "$EXCLUDE_KITS" ]; then
    echo -e "  已裁剪 Kit:      ${EXCLUDE_KITS}"
fi
echo -e "\n${YELLOW}📋 上手前就绪检查清单 (Next Steps)：${NC}"
echo -e "  1. 测试环境配置与 Firebase（Web 可视化面板）：运行 './scripts/config_studio.sh --env dev' 或直接编辑 config/dev.xcconfig（唯一真源，改完直读生效）"
echo -e "  2. 正式环境 (Prod) 隔离接入：由发版负责人运行 './scripts/config_studio.sh --env prod'（或导入离线导出 zip），参数写入本地忽略的 config/prod.local.xcconfig"
echo -e "  3. Android 正式签名证书：运行 './scripts/gen_keystore.sh --type release --write-prod-config' 生成 keystore 并写入 config/prod.local.xcconfig（详见 docs/sop/0001-app-signing-and-certificates.md）"
echo -e "====================================================${NC}"
