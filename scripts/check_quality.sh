#!/usr/bin/env bash

# ==============================================================================
# KMP + Android + iOS 统一代码质量检测门禁 CLI (L1 ~ L6)
# 用法:
#   ./scripts/check_quality.sh [--fast | --full | --fix | --install-hooks]
# ==============================================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

MODE="${1:---fast}"

print_usage() {
    echo "用法: ./scripts/check_quality.sh [--fast | --full | --fix | --install-hooks]"
    echo "  --fast           默认模式：运行 ShellCheck + Python/JS 脚本自检 + 架构红线校验 + Spotless + Detekt + SwiftLint"
    echo "  --full           完整模式：在 --fast 基础上追加全平台单测、Android Debug/Release Lint & APK 与 iOS Simulator 构建测试"
    echo "  --fix            自动修复 Kotlin/Gradle (spotlessApply) 与 Swift (swiftlint --fix) 格式问题"
    echo "  --install-hooks  一键启用仓库内置 .githooks/pre-commit 提交前静态拦截"
}

require_cmd() {
    local cmd="$1"
    local install_hint="$2"
    if ! command -v "$cmd" >/dev/null 2>&1; then
        echo -e "${RED}[缺失工具] 未检测到 $cmd！请先安装：$install_hint${NC}"
        exit 1
    fi
}

run_fast_checks() {
    echo -e "${BLUE}====================================================${NC}"
    echo -e "${BLUE}    [Quality Gate] Running Fast Static Checks (--fast)    ${NC}"
    echo -e "${BLUE}====================================================${NC}"

    echo -e "${YELLOW}[1/4] L1 ShellCheck 脚本静态检查...${NC}"
    require_cmd shellcheck "brew install shellcheck"
    shellcheck ./*.sh scripts/*.sh .githooks/pre-commit

    echo -e "${YELLOW}[2/4] L1 + L2 Python 脚本单测与 Config Studio JS 语法校验...${NC}"
    require_cmd python3 "Xcode Command Line Tools / brew install python"
    python3 -B -m unittest scripts/test_config_studio.py scripts/test_copy_ios_firebase_plist.py
    if command -v node >/dev/null 2>&1 && [ -f tools/config-studio/index.html ]; then
        python3 - <<'PY'
import pathlib, re, subprocess, tempfile
html = pathlib.Path("tools/config-studio/index.html").read_text(encoding="utf-8")
match = re.search(r'<script type="module">(.*)</script>', html, flags=re.S)
assert match, "tools/config-studio/index.html missing <script type=\"module\">"
with tempfile.NamedTemporaryFile(suffix=".mjs", mode="w", encoding="utf-8") as tf:
    tf.write(match.group(1))
    tf.flush()
    res = subprocess.run(["node", "--check", tf.name], capture_output=True, text=True)
    if res.returncode != 0:
        raise SystemExit(res.stderr)
PY
    fi

    echo -e "${YELLOW}[3/4] L1 + L3 + L5 Gradle 架构红线、Spotless 与 Detekt 检查...${NC}"
    chmod +x gradlew
    ./gradlew verifyArchitecture spotlessCheck detekt

    echo -e "${YELLOW}[4/4] L1 + L3 iOS SwiftLint 严格检查...${NC}"
    if [ "$(uname -s)" = "Darwin" ]; then
        require_cmd swiftlint "brew install swiftlint"
        (cd iosApp && swiftlint lint --config .swiftlint.yml --strict)
    else
        echo -e "${YELLOW}[跳过] 当前非 macOS 环境，跳过 SwiftLint 检查。${NC}"
    fi

    echo -e "${GREEN}✔ Fast 静态质量门禁全部通过！${NC}"
}

run_full_checks() {
    run_fast_checks

    echo -e "${BLUE}====================================================${NC}"
    echo -e "${BLUE}    [Quality Gate] Running Full Build & Test Suite (--full)    ${NC}"
    echo -e "${BLUE}====================================================${NC}"

    echo -e "${YELLOW}[1/3] L2 + L4 + L6 运行 KMP 全平台单测、Android Dev Lint 与 Debug APK...${NC}"
    ./gradlew allTests :shared:testAndroidHostTest -PenableDebugPanel=false :androidApp:lintDevDebug :androidApp:assembleDevDebug

    echo -e "${YELLOW}[2/3] L4 验证临时签名下 ProdRelease Lint 与 R8 混淆剥离包...${NC}"
    local tmp_keystore
    tmp_keystore="$(mktemp /tmp/quality-release-XXXXXX.jks)"
    rm -f "$tmp_keystore"
    trap 'rm -f "${tmp_keystore:-}" /tmp/iosAppQualityDerivedData' EXIT
    ./scripts/gen_keystore.sh \
        --type release \
        --output "$tmp_keystore" \
        --alias ci_release_key \
        --store-pass ci_keystore_pass_123 \
        --key-pass ci_keystore_pass_123 \
        --force
    PROD_STORE_FILE="$tmp_keystore" \
    PROD_STORE_PASSWORD=ci_keystore_pass_123 \
    PROD_KEY_ALIAS=ci_release_key \
    PROD_KEY_PASSWORD=ci_keystore_pass_123 \
    ./gradlew :androidApp:lintProdRelease :androidApp:assembleProdRelease -PenableDebugPanel=false
    rm -f "$tmp_keystore"

    echo -e "${YELLOW}[3/3] L2 + L6 运行 iOS XcodeGen、Simulator 集成测试与 Prod 构建...${NC}"
    if [ "$(uname -s)" = "Darwin" ]; then
        require_cmd xcodegen "brew install xcodegen"
        require_cmd xcodebuild "Xcode Command Line Tools"
        (
            cd iosApp
            xcodegen generate --spec project.yml
            local sim_udid
            sim_udid=$(xcrun simctl list devices available iPhone | sed -n 's/.*(\([0-9A-F-]\{36\}\)).*/\1/p' | head -n 1)
            if [ -z "$sim_udid" ]; then
                echo -e "${RED}[错误] 未找到可用的 iPhone Simulator！${NC}"
                exit 1
            fi
            xcodebuild -project iosApp.xcodeproj \
                -scheme iosApp-Dev \
                -destination "id=${sim_udid}" \
                -derivedDataPath /tmp/iosAppQualityDerivedData \
                CODE_SIGNING_ALLOWED=NO \
                test
            xcodebuild -project iosApp.xcodeproj \
                -scheme iosApp-Prod \
                -destination 'generic/platform=iOS Simulator' \
                -derivedDataPath /tmp/iosAppQualityDerivedData \
                ARCHS=arm64 \
                ONLY_ACTIVE_ARCH=YES \
                CODE_SIGNING_ALLOWED=NO \
                build
        )
        rm -rf /tmp/iosAppQualityDerivedData
    else
        echo -e "${YELLOW}[跳过] 当前非 macOS 环境，跳过 iOS 构建与测试。${NC}"
    fi

    echo -e "${GREEN}🎉 Full 全量质量门禁（L1~L6）全部通过！${NC}"
}

run_fix() {
    echo -e "${YELLOW}[Auto Fix] 正在自动格式化 Kotlin/Gradle 与 Swift 代码...${NC}"
    chmod +x gradlew
    ./gradlew spotlessApply
    if [ "$(uname -s)" = "Darwin" ] && command -v swiftlint >/dev/null 2>&1; then
        (cd iosApp && swiftlint --fix --config .swiftlint.yml)
    fi
    echo -e "${GREEN}✔ 自动格式化完成，建议随后执行 ./scripts/check_quality.sh --fast 复核。${NC}"
}

install_hooks() {
    chmod +x .githooks/pre-commit ./*.sh scripts/*.sh
    if [ ! -d ".git" ]; then
        echo -e "${YELLOW}[提示] 当前目录尚无 .git 仓库，已赋予脚本可执行权限；初始化 Git 后请重新运行 --install-hooks。${NC}"
        exit 0
    fi
    git config core.hooksPath .githooks
    echo -e "${GREEN}✔ Git Hook 已激活 (core.hooksPath=.githooks)，每次 git commit 将自动执行 --fast 静态质量门禁。${NC}"
}

case "$MODE" in
    --fast)
        run_fast_checks
        ;;
    --full)
        run_full_checks
        ;;
    --fix)
        run_fix
        ;;
    --install-hooks)
        install_hooks
        ;;
    --help|-h)
        print_usage
        ;;
    *)
        echo -e "${RED}[未知参数] $MODE${NC}"
        print_usage
        exit 1
        ;;
esac
