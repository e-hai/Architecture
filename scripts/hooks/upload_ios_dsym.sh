#!/usr/bin/env bash

# ==============================================================================
# iOS Crashlytics dSYM 自动上传脚本（Xcode Release Archive Post-Build Hook）
#
# 职责：
# 1. 仅在 Release 真机归档（非 Debug、非 Simulator）时触发上传，或设置 FORCE_DSYM_UPLOAD=1 强制执行；
# 2. 校验 GoogleService-Info.plist 是否存在且包含真实 GOOGLE_APP_ID（非模板占位符）；
# 3. 自动定位 Swift Package Manager 检出目录下的 FirebaseCrashlytics `upload-symbols`；
# 4. 缺失 plist / 占位配置 / 离线无 upload-symbols 时安全跳过并输出告警（exit 0），不阻断模板仓或本地编译。
# ==============================================================================

set -euo pipefail

CONFIGURATION="${CONFIGURATION:-Debug}"
EFFECTIVE_PLATFORM_NAME="${EFFECTIVE_PLATFORM_NAME:-}"
PLATFORM_NAME="${PLATFORM_NAME:-}"
FORCE_DSYM_UPLOAD="${FORCE_DSYM_UPLOAD:-0}"
DWARF_DSYM_FOLDER_PATH="${DWARF_DSYM_FOLDER_PATH:-}"

if [ "$FORCE_DSYM_UPLOAD" != "1" ]; then
    if [ "$CONFIGURATION" != "Release" ]; then
        echo "[Crashlytics-dSYM] 当前配置为 ${CONFIGURATION}（非 Release），跳过 dSYM 上传。"
        exit 0
    fi
    if [ "$EFFECTIVE_PLATFORM_NAME" = "-iphonesimulator" ] || [ "$PLATFORM_NAME" = "iphonesimulator" ]; then
        echo "[Crashlytics-dSYM] 当前目标为 iOS Simulator，跳过 dSYM 上传。"
        exit 0
    fi
fi

if [ -z "$DWARF_DSYM_FOLDER_PATH" ] || [ ! -d "$DWARF_DSYM_FOLDER_PATH" ]; then
    echo "[Crashlytics-dSYM] 未检测到有效 DWARF_DSYM_FOLDER_PATH (${DWARF_DSYM_FOLDER_PATH})，跳过上传。"
    exit 0
fi

PLIST_PATH="${GOOGLE_SERVICE_PLIST:-}"
if [ -z "$PLIST_PATH" ] && [ -n "${BUILT_PRODUCTS_DIR:-}" ] && [ -n "${PRODUCT_NAME:-}" ]; then
    PLIST_PATH="${BUILT_PRODUCTS_DIR}/${PRODUCT_NAME}.app/GoogleService-Info.plist"
fi
if [ -z "$PLIST_PATH" ] && [ -n "${TARGET_BUILD_DIR:-}" ] && [ -n "${UNLOCALIZED_RESOURCES_FOLDER_PATH:-}" ]; then
    PLIST_PATH="${TARGET_BUILD_DIR}/${UNLOCALIZED_RESOURCES_FOLDER_PATH}/GoogleService-Info.plist"
fi

if [ -z "$PLIST_PATH" ] || [ ! -f "$PLIST_PATH" ]; then
    echo "[Crashlytics-dSYM] 未检测到 GoogleService-Info.plist，跳过 dSYM 上传（安全降级）。"
    exit 0
fi

PLIST_BUDDY="${PLIST_BUDDY_BIN:-/usr/libexec/PlistBuddy}"
GOOGLE_APP_ID=""
if [ -x "$PLIST_BUDDY" ]; then
    GOOGLE_APP_ID=$("$PLIST_BUDDY" -c "Print :GOOGLE_APP_ID" "$PLIST_PATH" 2>/dev/null || true)
elif command -v python3 >/dev/null 2>&1; then
    GOOGLE_APP_ID=$(
        python3 - "$PLIST_PATH" <<'PY' 2>/dev/null || true
import plistlib
import sys

with open(sys.argv[1], "rb") as fp:
    data = plistlib.load(fp)
val = data.get("GOOGLE_APP_ID") if isinstance(data, dict) else None
if isinstance(val, str):
    print(val)
PY
    )
fi

if [ -z "$GOOGLE_APP_ID" ] || [[ "$GOOGLE_APP_ID" == *"PLACEHOLDER"* ]] || [[ "$GOOGLE_APP_ID" == *"000000000000"* ]]; then
    echo "[Crashlytics-dSYM] 检测到占位符或空 GOOGLE_APP_ID，跳过 dSYM 上传（安全降级）。"
    exit 0
fi

UPLOAD_SYMBOLS_BIN="${FIREBASE_UPLOAD_SYMBOLS_BIN:-}"
if [ -z "$UPLOAD_SYMBOLS_BIN" ] || [ ! -x "$UPLOAD_SYMBOLS_BIN" ]; then
    UPLOAD_SYMBOLS_BIN=""
    if [ -n "${BUILD_DIR:-}" ]; then
        DERIVED_ROOT="${BUILD_DIR%/Build/*}"
        candidate="${DERIVED_ROOT}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/upload-symbols"
        if [ -x "$candidate" ]; then
            UPLOAD_SYMBOLS_BIN="$candidate"
        fi
    fi
    if [ -z "$UPLOAD_SYMBOLS_BIN" ] && [ -n "${PROJECT_DIR:-}" ]; then
        candidate="${PROJECT_DIR}/.build/checkouts/firebase-ios-sdk/Crashlytics/upload-symbols"
        if [ -x "$candidate" ]; then
            UPLOAD_SYMBOLS_BIN="$candidate"
        fi
    fi
    if [ -z "$UPLOAD_SYMBOLS_BIN" ] && [ -n "${SRCROOT:-}" ]; then
        candidate="${SRCROOT}/.build/checkouts/firebase-ios-sdk/Crashlytics/upload-symbols"
        if [ -x "$candidate" ]; then
            UPLOAD_SYMBOLS_BIN="$candidate"
        fi
    fi
fi

if [ -z "$UPLOAD_SYMBOLS_BIN" ] || [ ! -x "$UPLOAD_SYMBOLS_BIN" ]; then
    echo "warning: [Crashlytics-dSYM] 未找到可执行 upload-symbols 工具，跳过上传（请确认已拉取 firebase-ios-sdk SPM）。"
    exit 0
fi

echo "[Crashlytics-dSYM] 正在上传 dSYM 至 Firebase Crashlytics (${DWARF_DSYM_FOLDER_PATH})..."
"$UPLOAD_SYMBOLS_BIN" -gsp "$PLIST_PATH" -p ios "$DWARF_DSYM_FOLDER_PATH"
echo "[Crashlytics-dSYM] dSYM 上传完成。"
