#!/bin/bash
set -euo pipefail

# ==============================================================================
#  iOS 现代架构脚手架初始化脚本 (Project Initializer)
#  用于将模板中的占位符一键替换为真实的项目名称与 Bundle Identifier。
# ==============================================================================

# 颜色常量定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# 占位符定义
PLACEHOLDER_BUNDLE_ID="xxx.yyy.zzz"
PLACEHOLDER_APP_NAME="Architecture"

echo -e "${PURPLE}${BOLD}"
echo "=========================================================="
echo "    🚀 iOS 现代模块化架构脚手架项目初始化工具"
echo "    SwiftUI · Swift 6 · SPM 模块化 · 离线优先"
echo "=========================================================="
echo -e "${NC}"

# 1. 输入目标项目名称
read -r -p "$(echo -e "${CYAN}请输入应用名称 (AppName, 例如 MyAwesomeApp): ${NC}")" APP_NAME
if [ -z "$APP_NAME" ]; then
    echo -e "${RED}❌ 应用名称不能为空！${NC}"
    exit 1
fi

# 2. 输入目标 Bundle Identifier
read -r -p "$(echo -e "${CYAN}请输入 Bundle Identifier (例如 com.company.app): ${NC}")" BUNDLE_ID
if [ -z "$BUNDLE_ID" ]; then
    echo -e "${RED}❌ Bundle Identifier 不能为空！${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}正在应用配置...${NC}"
echo -e "  - 应用名称 (AppName): ${GREEN}${APP_NAME}${NC}"
echo -e "  - Bundle ID:         ${GREEN}${BUNDLE_ID}${NC}"
echo ""

# 跨平台 sed 替换函数
replace_in_files() {
    local search="$1"
    local replace="$2"
    
    # 查找所有文本文件，跳过 .git 和 .build 目录
    find . -type f \
        -not -path '*/.*' \
        -not -path './.build/*' \
        -not -name 'init_project.sh' \
        \( -name "*.swift" -o -name "*.md" -o -name "*.yml" -o -name "*.yaml" -o -name "*.json" \) | while read -r file; do
        if grep -q "$search" "$file" 2>/dev/null; then
            sed -i '' "s/${search}/${replace}/g" "$file"
            echo -e "  已更新: ${BLUE}$file${NC}"
        fi
    done
}

echo -e "${YELLOW}[1/3] 替换 Bundle Identifier...${NC}"
replace_in_files "$PLACEHOLDER_BUNDLE_ID" "$BUNDLE_ID"

echo -e "${YELLOW}[2/3] 替换应用名称...${NC}"
replace_in_files "$PLACEHOLDER_APP_NAME" "$APP_NAME"

echo -e "${YELLOW}[3/3] 正在验证工程完整性并运行编译测试...${NC}"
if swift test; then
    echo ""
    echo -e "${GREEN}${BOLD}=========================================================="
    echo "  🎉 项目初始化成功！所有模块与测试均编译通过！"
    echo "  立即开始开发："
    echo "    - 编译工程: swift build"
    echo "    - 运行测试: swift test"
    echo "    - 使用 Xcode 打开: xed ."
    echo "==========================================================${NC}"
else
    echo -e "${RED}❌ 编译或测试验证失败，请检查上述错误信息。${NC}"
    exit 1
fi
