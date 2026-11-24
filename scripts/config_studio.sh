#!/usr/bin/env bash

# ==============================================================================
# KMP 双端统一配置台启动 CLI
# 用法:
#   ./scripts/config_studio.sh --env dev [--port 8765] [--no-open]
#   ./scripts/config_studio.sh --env prod [--port 8765] [--no-open]
# ==============================================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

ENV_LOCK=""
PORT="8765"
NO_OPEN=false

print_usage() {
    cat <<'EOF'
用法: ./scripts/config_studio.sh --env <dev|prod> [选项]

作用:
  启动 127.0.0.1 可视化配置台并物理锁定当前环境，校验并直写双端单一真源 config/*.xcconfig。

选项:
  --env <dev|prod>           必填！显式锁定操作环境（dev 与 prod 物理隔离）
  --port <端口>              本地 HTTP 服务端口（默认 8765；传 0 自动分配可用端口）
  --no-open                  启动服务后不自动唤起默认浏览器
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

CMD=(python3 "${REPO_ROOT}/scripts/config_studio.py" serve --env "$ENV_LOCK" --port "$PORT" --root "$REPO_ROOT")
if [[ "$NO_OPEN" == true ]]; then
    CMD+=(--no-open)
fi
exec "${CMD[@]}"
