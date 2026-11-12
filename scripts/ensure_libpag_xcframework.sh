#!/usr/bin/env bash

# ==============================================================================
# 确保 Kotlin/Native 链接所需的 libpag.xcframework 存在
# - Xcode iOS 宿主工程已通过 Vendor/LibPag (Swift Package binaryTarget) 原生集成 libpag
# - Gradle Kotlin/Native 链接 (Shared.framework / iOS test.kexe) 从 .gradle/libpag-ios 缓存读取，
#   不在 Git 中存放任何 8MB 二进制；本地首次或 CI 干净克隆时自动下载 Tencent 官方 Release 并校验 SHA256
# ==============================================================================
set -e

LIBPAG_VERSION="4.5.102"
LIBPAG_URL="https://github.com/Tencent/libpag/releases/download/v${LIBPAG_VERSION}/libpag_${LIBPAG_VERSION}_ios_arm64_x86_64.zip"
LIBPAG_SHA256="c8615ae4891440e3c24c3622b7150d3fd4a041d0108edf33429a3193d0339d18"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
TARGET_DIR="${1:-${ROOT_DIR}/.gradle/libpag-ios/${LIBPAG_VERSION}}"
XCFRAMEWORK_DIR="${TARGET_DIR}/libpag.xcframework"
MARKER_FILE="${TARGET_DIR}/.libpag_${LIBPAG_VERSION}_sha256"

# 若缓存已命中且 SHA256 标记一致，直接秒级退出
if [ -d "${XCFRAMEWORK_DIR}" ] && [ -f "${MARKER_FILE}" ]; then
    RECORDED_SHA=$(cat "${MARKER_FILE}" 2>/dev/null || true)
    if [ "${RECORDED_SHA}" = "${LIBPAG_SHA256}" ]; then
        exit 0
    fi
fi

# 若本地旧路径 iosApp/Frameworks/libpag.xcframework 已完整存在，直接复制进缓存避免重复下载
LEGACY_DIR="${ROOT_DIR}/iosApp/Frameworks/libpag.xcframework"
if [ -d "${LEGACY_DIR}/ios-arm64/libpag.framework" ] && [ -d "${LEGACY_DIR}/ios-arm64_x86_64-simulator/libpag.framework" ]; then
    mkdir -p "${TARGET_DIR}"
    rm -rf "${XCFRAMEWORK_DIR}"
    cp -R "${LEGACY_DIR}" "${XCFRAMEWORK_DIR}"
    echo "${LIBPAG_SHA256}" > "${MARKER_FILE}"
    exit 0
fi

echo "[libpag] 正在下载官方 libpag v${LIBPAG_VERSION} ios xcframework 至 Gradle 缓存..."
mkdir -p "${TARGET_DIR}"
TMP_ZIP="$(mktemp /tmp/libpag_${LIBPAG_VERSION}.XXXXXX.zip)"
trap 'rm -f "${TMP_ZIP}"' EXIT

curl -fsSL -o "${TMP_ZIP}" "${LIBPAG_URL}"

ACTUAL_SHA=$(shasum -a 256 "${TMP_ZIP}" | awk '{print $1}')
if [ "${ACTUAL_SHA}" != "${LIBPAG_SHA256}" ]; then
    echo "[libpag] 错误：SHA256 校验失败！预期 ${LIBPAG_SHA256}，实际 ${ACTUAL_SHA}" >&2
    exit 1
fi

rm -rf "${XCFRAMEWORK_DIR}"
unzip -q "${TMP_ZIP}" -d "${TARGET_DIR}"
echo "${LIBPAG_SHA256}" > "${MARKER_FILE}"
echo "[libpag] libpag.xcframework (v${LIBPAG_VERSION}) 已缓存至 ${XCFRAMEWORK_DIR}。"
