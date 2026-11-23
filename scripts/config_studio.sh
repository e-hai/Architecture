#!/usr/bin/env bash

# ==============================================================================
# KMP 双端统一配置台启动与离线包导入 CLI
# 用法:
#   ./scripts/config_studio.sh --env dev [--port 8765] [--no-open]
#   ./scripts/config_studio.sh --env prod [--port 8765] [--no-open]
#   ./scripts/config_studio.sh --import <bundle.zip> --env <dev|prod> [--dry-run]
# ==============================================================================
set -euo pipefail

CALLER_PWD="$(pwd)"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

ENV_LOCK=""
BUNDLE_FILE=""
PORT="8765"
NO_OPEN=false
DRY_RUN=false

print_usage() {
    cat <<'EOF'
用法: ./scripts/config_studio.sh --env <dev|prod> [选项]

模式:
  默认（启动本地网页直写）   打开 127.0.0.1 可视化配置台并锁定当前环境，直写唯一真源 config/*.xcconfig
  --import <bundle.zip>      导入非技术人员离线浏览器导出的配置压缩包并校验落盘

选项:
  --env <dev|prod>           必填！显式锁定操作环境（dev 与 prod 物理隔离）
  --port <端口>              本地 HTTP 服务端口（默认 8765；传 0 自动分配可用端口）
  --no-open                  启动服务后不自动唤起默认浏览器
  --dry-run                  仅在 --import 模式下预演改动，不实际写入文件
  -h, --help                 显示此帮助信息
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --env)
            if [[ $# -lt 2 ]] || [[ -z "${2:-}" ]]; then
                echo "[错误] --env 需要 dev 或 prod 参数" >&2
                exit 1
            fi
            ENV_LOCK="$2"
            shift 2
            ;;
        --import)
            if [[ $# -lt 2 ]] || [[ -z "${2:-}" ]]; then
                echo "[错误] --import 需要传入 zip 压缩包路径" >&2
                exit 1
            fi
            BUNDLE_FILE="$2"
            shift 2
            ;;
        --port)
            if [[ $# -lt 2 ]] || [[ -z "${2:-}" ]]; then
                echo "[错误] --port 需要传入端口号" >&2
                exit 1
            fi
            PORT="$2"
            shift 2
            ;;
        --no-open)
            NO_OPEN=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo "[错误] 未知参数：$1" >&2
            print_usage >&2
            exit 1
            ;;
    esac
done

if [[ "$ENV_LOCK" != "dev" && "$ENV_LOCK" != "prod" ]]; then
    echo "[错误] 必须通过 --env dev 或 --env prod 显式锁定操作环境！" >&2
    print_usage >&2
    exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
    echo "[错误] 未检测到 python3（macOS 默认自带，请检查 PATH）。" >&2
    exit 1
fi

if [[ -n "$BUNDLE_FILE" ]]; then
    if [[ "$BUNDLE_FILE" != /* && -f "${CALLER_PWD}/${BUNDLE_FILE}" ]]; then
        BUNDLE_FILE="${CALLER_PWD}/${BUNDLE_FILE}"
    fi
    CMD=(python3 "${REPO_ROOT}/scripts/config_studio.py" import "$BUNDLE_FILE" --env "$ENV_LOCK" --root "$REPO_ROOT")
    if [[ "$DRY_RUN" == true ]]; then
        CMD+=(--dry-run)
    fi
    exec "${CMD[@]}"
else
    CMD=(python3 "${REPO_ROOT}/scripts/config_studio.py" serve --env "$ENV_LOCK" --port "$PORT" --root "$REPO_ROOT")
    if [[ "$NO_OPEN" == true ]]; then
        CMD+=(--no-open)
    fi
    exec "${CMD[@]}"
fi
