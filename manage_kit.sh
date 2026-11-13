#!/usr/bin/env bash

# ==============================================================================
# Kit 模块生命周期管理脚本（支持列表检视与一键干净裁剪移除）
# 用法:
#   ./manage_kit.sh list
#   ./manage_kit.sh remove <kit_name> [--force]
# 示例:
#   ./manage_kit.sh remove video
#   ./manage_kit.sh remove ads
# ==============================================================================
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PKG_PREFIX="xxx.yyy.zzz"
PKG_PATH="${PKG_PREFIX//.//}"

ALL_KITS=("log" "crashreport" "analytics" "abtesting" "mmp" "ads" "pay" "push" "video" "pag")

kit_pascal() {
    case "$1" in
        log) echo "Log" ;;
        crashreport) echo "CrashReport" ;;
        analytics) echo "Analytics" ;;
        abtesting) echo "AbTesting" ;;
        mmp) echo "Mmp" ;;
        ads) echo "Ads" ;;
        pay) echo "Pay" ;;
        push) echo "Push" ;;
        video) echo "Video" ;;
        pag) echo "Pag" ;;
        *) echo "" ;;
    esac
}

cmd_list() {
    echo -e "${BLUE}=== 当前已集成的 Kit 模块状态 ===${NC}"
    for k in "${ALL_KITS[@]}"; do
        if [ -d "core/${k}" ]; then
            echo -e "  ${GREEN}[已启用]${NC} :core:${k}"
        else
            echo -e "  ${YELLOW}[已移除]${NC} :core:${k}"
        fi
    done
}

strip_kit_block() {
    local file="$1"
    local kit="$2"
    if [ -f "$file" ]; then
        awk -v start="[KIT:${kit}:START]" -v end="[KIT:${kit}:END]" '
            index($0, start) { skip=1; next }
            index($0, end) { skip=0; next }
            !skip { print }
        ' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
    fi
}

cmd_remove() {
    local KIT="$1"
    local FORCE=false
    if [ "$2" = "--force" ]; then
        FORCE=true
    fi

    local PASCAL
    PASCAL=$(kit_pascal "$KIT")
    if [ -z "$PASCAL" ]; then
        echo -e "${RED}[错误] 未知的 Kit 名称: ${KIT}${NC}"
        echo -e "支持列表: ${ALL_KITS[*]}"
        exit 1
    fi

    if [ ! -d "core/${KIT}" ]; then
        echo -e "${YELLOW}[提示] :core:${KIT} 目录已不存在，跳过。${NC}"
        exit 0
    fi

    if [ "$KIT" = "log" ] && [ "$FORCE" = false ]; then
        echo -e "${RED}[安全拦截] :core:log 是全工程核心基础日志门面（被 AppBootstrap / AppNavGraph / DebugScreen 全局引用）！${NC}"
        echo -e "${YELLOW}如确认要彻底替换日志系统，请先处理全局引用或追加 --force。${NC}"
        exit 1
    fi

    # 检查外部业务特性模块是否引用该 Kit（排除 features/debug）
    EXTERNAL_REFS=$(grep -rnIE --exclude-dir=build --include="*.kts" --include="*.kt" ":core:${KIT}\b|projects\.core\.${KIT}\b|core\.${KIT}\." features/ 2>/dev/null | grep -v "^features/debug/" || true)
    if [ -n "$EXTERNAL_REFS" ] && [ "$FORCE" = false ]; then
        echo -e "${RED}[安全拦截] 检测到业务特性模块仍在依赖 :core:${KIT}：${NC}"
        echo "$EXTERNAL_REFS"
        echo -e "${YELLOW}请先解除上述业务模块中的引用，或追加 --force 强制裁剪。${NC}"
        exit 1
    fi

    echo -e "${BLUE}====================================================${NC}"
    echo -e "${BLUE}    Removing Kit Module: :core:${KIT} (${PASCAL})    ${NC}"
    echo -e "${BLUE}====================================================${NC}"

    echo -e "${YELLOW}[1/5] 卸载 Gradle 与依赖导出锚点...${NC}"
    strip_kit_block "settings.gradle.kts" "$KIT"
    strip_kit_block "shared/build.gradle.kts" "$KIT"
    strip_kit_block "androidApp/build.gradle.kts" "$KIT"
    strip_kit_block "androidApp/proguard-rules.pro" "$KIT"
    if [ -f "features/debug/impl/build.gradle.kts" ]; then
        strip_kit_block "features/debug/impl/build.gradle.kts" "$KIT"
    fi

    echo -e "${YELLOW}[2/5] 卸载双端宿主 NativeKitBootstrap / 入口挂载与 Delegate 文件...${NC}"
    local MAIN_ACTIVITY="androidApp/src/main/kotlin/${PKG_PATH}/android/MainActivity.kt"
    strip_kit_block "$MAIN_ACTIVITY" "$KIT"

    local AND_BOOTSTRAP="androidApp/src/main/kotlin/${PKG_PATH}/android/bridge/NativeKitBootstrap.kt"
    local AND_DELEGATE="androidApp/src/main/kotlin/${PKG_PATH}/android/bridge/delegates/Android${PASCAL}Delegate.kt"
    strip_kit_block "$AND_BOOTSTRAP" "$KIT"
    if [ "$KIT" = "pag" ]; then
        sed -i.bak "/PagKitAndroid/d;/core\.pag\./d" "$AND_BOOTSTRAP" && rm -f "$AND_BOOTSTRAP.bak"
    else
        sed -i.bak "/Android${PASCAL}Delegate/d;/core\.${KIT}\./d" "$AND_BOOTSTRAP" && rm -f "$AND_BOOTSTRAP.bak"
    fi
    rm -f "$AND_DELEGATE"

    local IOS_APP="iosApp/Sources/iOSApp.swift"
    strip_kit_block "$IOS_APP" "$KIT"

    local IOS_BOOTSTRAP="iosApp/Sources/Bridge/NativeKitBootstrap.swift"
    local IOS_DELEGATE="iosApp/Sources/Bridge/Delegates/Ios${PASCAL}Delegate.swift"
    strip_kit_block "$IOS_BOOTSTRAP" "$KIT"
    rm -f "$IOS_DELEGATE"

    strip_kit_block "iosApp/project.yml" "$KIT"
    strip_kit_block "iosApp/Package.swift" "$KIT"
    if [ "$KIT" = "pag" ]; then
        rm -rf "iosApp/Vendor/LibPag"
    fi

    echo -e "${YELLOW}[3/5] 卸载 Debug 模块对应调试区块...${NC}"
    local DEBUG_MODULE="features/debug/impl/src/commonMain/kotlin/${PKG_PATH}/feature/debug/impl/DebugModule.kt"
    local DEBUG_SECTION="features/debug/impl/src/commonMain/kotlin/${PKG_PATH}/feature/debug/impl/sections/${PASCAL}DebugSection.kt"
    local DEBUG_TEST="features/debug/impl/src/commonTest/kotlin/${PKG_PATH}/feature/debug/impl/DebugModuleTest.kt"
    strip_kit_block "$DEBUG_MODULE" "$KIT"
    strip_kit_block "$DEBUG_TEST" "$KIT"
    rm -f "$DEBUG_SECTION"

    echo -e "${YELLOW}[4/5] 删除核心模块目录 core/${KIT}...${NC}"
    rm -rf "core/${KIT}"

    echo -e "${YELLOW}[5/5] 同步 Xcode 工程与代码格式化...${NC}"
    if command -v xcodegen &> /dev/null; then
        (cd iosApp && xcodegen generate --spec project.yml > /dev/null)
    fi
    chmod +x gradlew
    ./gradlew spotlessApply > /dev/null 2>&1 || true

    echo -e "${GREEN}🎉 Kit 模块 :core:${KIT} 已完成双端全链路干净裁剪！${NC}"
}

case "$1" in
    list)
        cmd_list
        ;;
    remove)
        if [ -z "$2" ]; then
            echo -e "${RED}[错误] 请指定要移除的 Kit 名称！${NC}"
            exit 1
        fi
        cmd_remove "$2" "$3"
        ;;
    *)
        echo -e "用法: ./manage_kit.sh <list|remove> [kit_name] [--force]"
        exit 1
        ;;
esac
