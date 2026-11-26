#!/usr/bin/env bash

# ==============================================================================
# iOS Shared.framework 智能增量编排包装脚本（Xcode Build Phase 调用）
#
# 职责：
# 1. 对 shared / core / features / build-logic / gradle 的 Kotlin/Gradle/Resource 输入计算指纹；
# 2. 若仅修改 Swift 代码且目标产物 Shared.framework/Shared 已存在且指纹一致，则 ~20ms 快速跳过 Gradle；
# 3. 任意 Kotlin/Gradle/资源/构建配置发生变化，或设置 FORCE_KMP_BUILD=1 时，自动执行
#    `./gradlew :shared:embedAndSignAppleFrameworkForXcode -PenableDebugPanel=...` 并刷新指纹。
# ==============================================================================

set -euo pipefail

REPO_ROOT="${REPO_ROOT_OVERRIDE:-${SRCROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}}"
if [ -z "${REPO_ROOT_OVERRIDE:-}" ] && [ -n "${SRCROOT:-}" ]; then
    REPO_ROOT="$(cd "${SRCROOT}/.." && pwd)"
fi
cd "$REPO_ROOT"

CONFIGURATION="${CONFIGURATION:-Debug}"
SDK_NAME="${SDK_NAME:-iphonesimulator}"
ARCHS="${ARCHS:-arm64}"
FORCE_KMP_BUILD="${FORCE_KMP_BUILD:-0}"
GRADLEW_BIN="${GRADLEW_BIN:-${REPO_ROOT}/gradlew}"

ENABLE_DEBUG_FLAG="true"
if [ "$CONFIGURATION" = "Release" ]; then
    ENABLE_DEBUG_FLAG="false"
fi

FRAMEWORK_DIR="${REPO_ROOT}/shared/build/xcode-frameworks/${CONFIGURATION}/${SDK_NAME}"
FRAMEWORK_BIN="${FRAMEWORK_DIR}/Shared.framework/Shared"
STAMP_FILE="${FRAMEWORK_DIR}/.kmp_inputs.sha1"

compute_fingerprint() {
    python3 - "$REPO_ROOT" "$CONFIGURATION" "$SDK_NAME" "$ARCHS" "$ENABLE_DEBUG_FLAG" <<'PY'
import hashlib
import os
import sys

repo_root, config, sdk_name, archs, enable_debug = sys.argv[1:6]
h = hashlib.sha256()
h.update(f"config={config}|sdk={sdk_name}|archs={archs}|debug={enable_debug}\n".encode("utf-8"))

watched_roots = ["shared", "features", "core", "gradle", "build-logic"]
watched_files = ["build.gradle.kts", "settings.gradle.kts", "gradle.properties"]
ignored_dirs = {".git", ".gradle", ".kotlin", ".build", "build", "DerivedData", ".swiftpm"}
watched_exts = (".kt", ".kts", ".toml", ".xml", ".png", ".webp", ".ttf", ".otf", ".json", ".properties", ".def")

entries = []
for rel in watched_files:
    full = os.path.join(repo_root, rel)
    if os.path.isfile(full):
        st = os.stat(full)
        entries.append((rel, st.st_mtime_ns, st.st_size))

for root_name in watched_roots:
    root_path = os.path.join(repo_root, root_name)
    if not os.path.isdir(root_path):
        continue
    for dirpath, dirnames, filenames in os.walk(root_path):
        dirnames[:] = sorted(d for d in dirnames if d not in ignored_dirs)
        for fname in sorted(filenames):
            if not fname.endswith(watched_exts):
                continue
            full = os.path.join(dirpath, fname)
            rel = os.path.relpath(full, repo_root)
            st = os.stat(full)
            entries.append((rel, st.st_mtime_ns, st.st_size))

entries.sort()
for rel, mtime_ns, size in entries:
    h.update(f"{rel}:{mtime_ns}:{size}\n".encode("utf-8"))

print(h.hexdigest())
PY
}

CURRENT_FP=""
if command -v python3 >/dev/null 2>&1; then
    CURRENT_FP="$(compute_fingerprint)"
fi

if [ "$FORCE_KMP_BUILD" != "1" ] && [ -n "$CURRENT_FP" ] && [ -f "$FRAMEWORK_BIN" ] && [ -f "$STAMP_FILE" ]; then
    PREV_FP="$(cat "$STAMP_FILE" 2>/dev/null || true)"
    if [ "$CURRENT_FP" = "$PREV_FP" ]; then
        echo "[KMP-iOS] Kotlin/Gradle 输入未变化，跳过 Gradle 编排（设置 FORCE_KMP_BUILD=1 可强制重编）。"
        exit 0
    fi
fi

echo "[KMP-iOS] 编译 Shared.framework (CONFIGURATION=${CONFIGURATION}, SDK=${SDK_NAME}, debugPanel=${ENABLE_DEBUG_FLAG})..."
"$GRADLEW_BIN" :shared:embedAndSignAppleFrameworkForXcode -PenableDebugPanel="$ENABLE_DEBUG_FLAG"

if [ -n "$CURRENT_FP" ]; then
    mkdir -p "$FRAMEWORK_DIR"
    printf '%s\n' "$CURRENT_FP" > "$STAMP_FILE"
fi
