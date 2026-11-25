#!/usr/bin/env bash

# ==============================================================================
# iOS Firebase (GoogleService-Info.plist) 按构建环境物理隔离拷贝与校检脚本
#
# 由 Xcode Target Build Phase 调用，职责如下：
# 1. 阻断 iosApp/Sources/ 下残留未隔离的 GoogleService-Info*.plist（防止两套配置同时打入 .app）；
# 2. 按 CONFIGURATION（Release -> Prod，其余 -> Dev）锁定源目录 iosApp/Config/Firebase/{Dev,Prod}/；
# 3. 当 .app 内 plist 状态发生变更（新增 / 更新 / 清理）时自动失效 `_CodeSignature`，
#    促使 Xcode 本次构建确定性重签，避免增量构建下 codesign 验证报 sealed resource is missing or invalid；
# 4. 未放置对应环境的 plist 时零阻塞跳过（运行时安全降级，兼容未接 Firebase 项目与 CI）；
# 5. 已放置 plist 时通过 PlistBuddy 强校验 BUNDLE_ID == PRODUCT_BUNDLE_IDENTIFIER，防止串环境。
# ==============================================================================

set -euo pipefail

SRCROOT="${SRCROOT:?缺少 SRCROOT 环境变量}"
CONFIGURATION="${CONFIGURATION:-Debug}"
TARGET_BUILD_DIR="${TARGET_BUILD_DIR:?缺少 TARGET_BUILD_DIR 环境变量}"
UNLOCALIZED_RESOURCES_FOLDER_PATH="${UNLOCALIZED_RESOURCES_FOLDER_PATH:?缺少 UNLOCALIZED_RESOURCES_FOLDER_PATH 环境变量}"
PRODUCT_BUNDLE_IDENTIFIER="${PRODUCT_BUNDLE_IDENTIFIER:-}"

DEST_DIR="${TARGET_BUILD_DIR}/${UNLOCALIZED_RESOURCES_FOLDER_PATH}"
DEST_PLIST="${DEST_DIR}/GoogleService-Info.plist"
CODE_SIG_DIR="${DEST_DIR}/_CodeSignature"

invalidate_codesign() {
    rm -rf "$CODE_SIG_DIR"
}

cleanup_dest_and_invalidate() {
    if [ -f "$DEST_PLIST" ]; then
        rm -f "$DEST_PLIST"
        invalidate_codesign
    fi
}

# 1. 严禁在 iosApp/Sources/ 下直接放置 GoogleService-Info*.plist
if [ -d "${SRCROOT}/Sources" ]; then
    LEGACY_SOURCES_PLIST=$(find "${SRCROOT}/Sources" -maxdepth 3 -name "GoogleService-Info*.plist" -print -quit 2>/dev/null || true)
    if [ -n "$LEGACY_SOURCES_PLIST" ]; then
        cleanup_dest_and_invalidate
        echo "error: [Firebase-iOS 环境隔离拦截] 检测到未隔离文件 ${LEGACY_SOURCES_PLIST}！请勿将 Firebase 配置直接放入 Sources/（否则会同时打入测试包与正式包），请移动至 iosApp/Config/Firebase/Dev/ 或 Prod/ 目录下。" >&2
        exit 1
    fi
fi

# 2. 按当前 Xcode 构建配置锁定对应隔离目录
ENV_DIR="Dev"
if [ "$CONFIGURATION" = "Release" ]; then
    ENV_DIR="Prod"
fi

SRC_PLIST="${SRCROOT}/Config/Firebase/${ENV_DIR}/GoogleService-Info.plist"

# 3. 若未放置当前环境的 plist，清理旧残留（若存在则失效签名以触发重签）后零阻塞降级
if [ ! -f "$SRC_PLIST" ]; then
    if [ -f "$DEST_PLIST" ]; then
        rm -f "$DEST_PLIST"
        invalidate_codesign
        echo "[Firebase-iOS] 未检测到 Config/Firebase/${ENV_DIR}/GoogleService-Info.plist，已清理旧残留并失效签名以触发重签（运行时将自动安全降级）。"
    else
        echo "[Firebase-iOS] 未检测到 Config/Firebase/${ENV_DIR}/GoogleService-Info.plist，跳过复制（运行时将自动安全降级）。"
    fi
    exit 0
fi

# 4. 校验 plist 内 BUNDLE_ID 有效性及与当前构建包名一致性（防 Dev <-> Prod 放反）
PLIST_BUDDY="${PLIST_BUDDY_BIN:-/usr/libexec/PlistBuddy}"
if [ -x "$PLIST_BUDDY" ]; then
    PLIST_BUNDLE_ID=$("$PLIST_BUDDY" -c "Print :BUNDLE_ID" "$SRC_PLIST" 2>/dev/null || true)
elif command -v python3 >/dev/null 2>&1; then
    PLIST_BUNDLE_ID=$(
        python3 - "$SRC_PLIST" <<'PY' 2>/dev/null || true
import plistlib
import sys

with open(sys.argv[1], "rb") as fp:
    data = plistlib.load(fp)
val = data.get("BUNDLE_ID") if isinstance(data, dict) else None
if isinstance(val, str):
    print(val)
PY
    )
else
    cleanup_dest_and_invalidate
    echo "error: [Firebase-iOS 校验失败] 未找到可执行工具 ${PLIST_BUDDY} 或 python3，无法校检 GoogleService-Info.plist。" >&2
    exit 1
fi
if [ -z "$PLIST_BUNDLE_ID" ]; then
    cleanup_dest_and_invalidate
    echo "error: [Firebase-iOS 校验失败] Config/Firebase/${ENV_DIR}/GoogleService-Info.plist 缺少有效的 BUNDLE_ID 字段，请检查文件是否完整。" >&2
    exit 1
fi

if [ -n "$PRODUCT_BUNDLE_IDENTIFIER" ] && [ "$PLIST_BUNDLE_ID" != "$PRODUCT_BUNDLE_IDENTIFIER" ]; then
    cleanup_dest_and_invalidate
    echo "error: [Firebase-iOS 环境隔离拦截] Config/Firebase/${ENV_DIR}/GoogleService-Info.plist 的 BUNDLE_ID (${PLIST_BUNDLE_ID}) 与当前 ${CONFIGURATION} 构建包名 (${PRODUCT_BUNDLE_IDENTIFIER}) 不匹配！请检查是否将测试/正式环境配置放反。" >&2
    exit 1
fi

# 5. 仅在 plist 新增或内容变更时复制并失效旧签名；内容完全一致时保留现有签名以兼顾增量速度
if [ -f "$DEST_PLIST" ] && cmp -s "$SRC_PLIST" "$DEST_PLIST"; then
    echo "[Firebase-iOS] ✔ ${ENV_DIR} 环境 GoogleService-Info.plist 未变化 (BUNDLE_ID=${PLIST_BUNDLE_ID})，保持现有签名。"
    exit 0
fi

mkdir -p "$DEST_DIR"
cp -f "$SRC_PLIST" "$DEST_PLIST"
invalidate_codesign
echo "[Firebase-iOS] ✔ 已复制 ${ENV_DIR} 环境 GoogleService-Info.plist (BUNDLE_ID=${PLIST_BUNDLE_ID}) 至产物包根目录（已触发重签）。"
