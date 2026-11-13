#!/usr/bin/env bash

# ==============================================================================
# 一键安全移除业务特性模块 (:features:<name>:api + :features:<name>:impl)
# 用法: ./remove_feature.sh <feature_name> [--force]
# ==============================================================================
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

if [ -z "$1" ]; then
    echo -e "${RED}[错误] 请提供要移除的特性模块名称！${NC}"
    echo -e "用法: ./remove_feature.sh <feature_name> [--force]"
    exit 1
fi

RAW_NAME="$1"
FEATURE_NAME=$(echo "$RAW_NAME" | tr '[:upper:]-' '[:lower:]_')
CLASS_PREFIX=$(echo "$FEATURE_NAME" | awk -F_ '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)} 1' OFS="")
FORCE=false
if [ "$2" = "--force" ]; then
    FORCE=true
fi

echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}    Removing Feature: ${FEATURE_NAME} (${CLASS_PREFIX})    ${NC}"
echo -e "${BLUE}====================================================${NC}"

if [ ! -d "features/${FEATURE_NAME}" ]; then
    echo -e "${RED}[错误] 目录 features/${FEATURE_NAME} 不存在！${NC}"
    exit 1
fi

NAVGRAPH_FILE=$(find shared/src/commonMain/kotlin -name "AppNavGraph.kt" | head -n 1)
if [ -n "$NAVGRAPH_FILE" ] && grep -q "${CLASS_PREFIX}NavKey" "$NAVGRAPH_FILE"; then
    # 检查是否为 defaultStartKey 起始路由
    if awk '/\[START_ROUTE_START\]/,/\[START_ROUTE_END\]/' "$NAVGRAPH_FILE" | grep -q "${CLASS_PREFIX}NavKey"; then
        if [ "$FORCE" = false ]; then
            echo -e "${RED}[安全拦截] ${FEATURE_NAME} (${CLASS_PREFIX}NavKey) 当前被配置为应用起始默认路由 (defaultStartKey)！${NC}"
            echo -e "${YELLOW}请先在 AppNavGraph.kt 的 [START_ROUTE_START]~[START_ROUTE_END] 中切换至其他首页路由，或追加 --force。${NC}"
            exit 1
        fi
    fi
fi

# 检查是否有其他业务模块直接依赖该 feature:api
OTHER_REFS=$(grep -rnIE --exclude-dir=build --include="*.kts" --include="*.kt" ":features:${FEATURE_NAME}:|projects\.features\.${FEATURE_NAME}\b|\b${CLASS_PREFIX}NavKey\b" features/ 2>/dev/null | grep -v "^features/${FEATURE_NAME}/" || true)
if [ -n "$OTHER_REFS" ] && [ "$FORCE" = false ]; then
    echo -e "${RED}[安全拦截] 检测到其他业务模块引用了 :features:${FEATURE_NAME}：${NC}"
    echo "$OTHER_REFS"
    echo -e "${YELLOW}请先解除依赖后再移除，或使用 --force 强制删除。${NC}"
    exit 1
fi

strip_block() {
    local file="$1"
    local tag="$2"
    if [ -f "$file" ]; then
        awk -v start="[FEATURE:${tag}:START]" -v end="[FEATURE:${tag}:END]" '
            index($0, start) { skip=1; next }
            index($0, end) { skip=0; next }
            !skip { print }
        ' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
    fi
}

echo -e "${YELLOW}[1/3] 卸载 Gradle / Koin / 路由挂载锚点...${NC}"
strip_block "settings.gradle.kts" "$FEATURE_NAME"
strip_block "shared/build.gradle.kts" "$FEATURE_NAME"

BOOTSTRAP_FILE=$(find shared/src/commonMain/kotlin -name "AppBootstrap.kt" | head -n 1)
if [ -n "$BOOTSTRAP_FILE" ]; then
    strip_block "$BOOTSTRAP_FILE" "$FEATURE_NAME"
fi
if [ -n "$NAVGRAPH_FILE" ]; then
    strip_block "$NAVGRAPH_FILE" "$FEATURE_NAME"
fi

echo -e "${YELLOW}[2/3] 删除物理模块目录 features/${FEATURE_NAME}...${NC}"
rm -rf "features/${FEATURE_NAME}"
if [ "$FEATURE_NAME" = "debug" ]; then
    rm -rf "shared/src/debugEnabled"
    rm -f androidApp/src/*/assets/like.pag
    rm -f iosApp/Sources/like.pag iosApp/DebugResources/like.pag
fi

echo -e "${YELLOW}[3/3] 执行 Spotless 格式化...${NC}"
chmod +x gradlew
./gradlew spotlessApply > /dev/null 2>&1 || true

echo -e "${GREEN}🎉 特性模块 ${FEATURE_NAME} 已安全移除！${NC}"
