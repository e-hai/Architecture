#!/usr/bin/env bash

# ==============================================================================
# 双端构建产物（APK / iOS .app）体积基线采集与阈值门禁脚本
# 用法:
#   ./scripts/hooks/check_artifact_size.sh \
#     --platform <android|ios> \
#     --artifact <path> \
#     --warn-mb <num> \
#     [--fail-mb <num>] \
#     [--report-json <path>]
# ==============================================================================

set -euo pipefail

PLATFORM=""
ARTIFACT=""
WARN_MB=""
FAIL_MB=""
REPORT_JSON=""

print_usage() {
    echo "用法: $0 --platform <android|ios> --artifact <path> --warn-mb <num> [--fail-mb <num>] [--report-json <path>]"
}

while [ $# -gt 0 ]; do
    case "$1" in
        --platform)
            PLATFORM="${2:-}"
            shift 2
            ;;
        --artifact)
            ARTIFACT="${2:-}"
            shift 2
            ;;
        --warn-mb)
            WARN_MB="${2:-}"
            shift 2
            ;;
        --fail-mb)
            FAIL_MB="${2:-}"
            shift 2
            ;;
        --report-json)
            REPORT_JSON="${2:-}"
            shift 2
            ;;
        --help|-h)
            print_usage
            exit 0
            ;;
        *)
            echo "error: 未知参数: $1" >&2
            print_usage >&2
            exit 1
            ;;
    esac
done

if [ -z "$PLATFORM" ] || [ -z "$ARTIFACT" ] || [ -z "$WARN_MB" ]; then
    echo "error: 缺少必需参数 --platform / --artifact / --warn-mb" >&2
    print_usage >&2
    exit 1
fi

if [ ! -e "$ARTIFACT" ]; then
    echo "error: 产物路径不存在: ${ARTIFACT}" >&2
    exit 1
fi

python3 - "$PLATFORM" "$ARTIFACT" "$WARN_MB" "$FAIL_MB" "$REPORT_JSON" <<'PY'
import json
import os
import pathlib
import sys

platform, artifact_str, warn_mb_str, fail_mb_str, report_json = sys.argv[1:6]
warn_mb = float(warn_mb_str)
fail_mb = float(fail_mb_str) if fail_mb_str else None
artifact = pathlib.Path(artifact_str)


def measure_bytes(path: pathlib.Path) -> int:
    if path.is_file():
        return path.stat().st_size
    total = 0
    for root, _, files in os.walk(path):
        for fname in files:
            fpath = pathlib.Path(root) / fname
            if not fpath.is_symlink() and fpath.is_file():
                total += fpath.stat().st_size
    return total


total_bytes = measure_bytes(artifact)
total_mb = total_bytes / (1024.0 * 1024.0)

shared_fw_bytes = None
shared_fw_bin = artifact / "Frameworks" / "Shared.framework" / "Shared"
if artifact.is_dir() and shared_fw_bin.is_file():
    shared_fw_bytes = shared_fw_bin.stat().st_size

status = "OK"
exit_code = 0
if fail_mb is not None and total_mb > fail_mb:
    status = "FAIL"
    exit_code = 1
elif total_mb > warn_mb:
    status = "WARN"

payload = {
    "platform": platform,
    "artifact": str(artifact),
    "bytes": total_bytes,
    "size_mb": round(total_mb, 2),
    "warn_mb": warn_mb,
    "fail_mb": fail_mb,
    "shared_framework_mb": round(shared_fw_bytes / (1024.0 * 1024.0), 2) if shared_fw_bytes is not None else None,
    "status": status,
}

print(
    f"[SizeGate][{platform}] artifact={artifact.name} size={payload['size_mb']:.2f} MB "
    f"({total_bytes} bytes), warn={warn_mb:.2f} MB"
    + (f", fail={fail_mb:.2f} MB" if fail_mb is not None else "")
    + (f", Shared.framework={payload['shared_framework_mb']:.2f} MB" if payload["shared_framework_mb"] is not None else "")
    + f" -> {status}",
)

if status == "WARN":
    print(
        f"::warning::[SizeGate][{platform}] {artifact.name} 体积 {payload['size_mb']:.2f} MB "
        f"已超过告警阈值 {warn_mb:.2f} MB",
    )
elif status == "FAIL":
    print(
        f"::error::[SizeGate][{platform}] {artifact.name} 体积 {payload['size_mb']:.2f} MB "
        f"已超出硬阻断阈值 {fail_mb:.2f} MB",
        file=sys.stderr,
    )

summary_path = os.environ.get("GITHUB_STEP_SUMMARY", "")
if summary_path:
    extra = (
        f"{payload['shared_framework_mb']:.2f} MB"
        if payload["shared_framework_mb"] is not None
        else "—"
    )
    fail_col = f"{fail_mb:.2f} MB" if fail_mb is not None else "—"
    with open(summary_path, "a", encoding="utf-8") as fp:
        fp.write(
            f"\n### 产物包体积基线 ({platform})\n\n"
            "| 产物 | 总体积 | `Shared.framework` | 告警阈值 | 阻断阈值 | 状态 |\n"
            "| :--- | :--- | :--- | :--- | :--- | :--- |\n"
            f"| `{artifact.name}` | {payload['size_mb']:.2f} MB | {extra} | {warn_mb:.2f} MB | {fail_col} | **{status}** |\n",
        )

if report_json:
    report_path = pathlib.Path(report_json)
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

sys.exit(exit_code)
PY
