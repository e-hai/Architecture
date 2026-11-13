#!/usr/bin/env bash

# ==============================================================================
# Android 签名证书生成脚本（对齐 androidApp/build.gradle.kts 的 config/prod.properties 与 config/dev.properties）
# 用法:
#   ./scripts/gen_keystore.sh [--type release|test] [--output <path>] [--alias <alias>]
#                             [--store-pass <pwd>] [--key-pass <pwd>] [--dname <dname>]
#                             [--write-prod-props] [--force]
# 示例:
#   ./scripts/gen_keystore.sh
#   ./scripts/gen_keystore.sh --type release --write-prod-props
#   ./scripts/gen_keystore.sh --type release --output /tmp/ci-release.jks --alias ci_key --store-pass changeit --key-pass changeit --force
# ==============================================================================
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

TYPE="release"
OUTPUT_PATH=""
KEY_ALIAS=""
STORE_PASSWORD=""
KEY_PASSWORD=""
DNAME=""
WRITE_PROD_PROPS=false
FORCE=false

print_usage() {
    cat <<'EOF'
用法: ./scripts/gen_keystore.sh [选项]

选项:
  --type <release|test>   证书类型，默认 release
  --output <path>         输出 .jks 路径（默认 release: config/keystore/release.jks，test: config/keystore/test.jks）
  --alias <alias>         证书别名（默认 release: release_key，test: androiddebugkey）
  --store-pass <pwd>      Keystore 密码（交互模式未传时会提示输入；test 默认 android）
  --key-pass <pwd>        Key 密码（默认同 store-pass；test 默认 android）
  --dname <distinguished> 证书 DN 信息（默认 CN=App,OU=Mobile,O=Company,L=City,S=State,C=CN）
  --write-prod-props      生成 release 证书后自动同步写入 config/prod.properties（已由 .gitignore 忽略）
  --force                 覆盖已存在的目标 .jks 文件
  -h, --help              显示此帮助信息
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --type)
            TYPE="$2"
            shift 2
            ;;
        --output)
            OUTPUT_PATH="$2"
            shift 2
            ;;
        --alias)
            KEY_ALIAS="$2"
            shift 2
            ;;
        --store-pass)
            STORE_PASSWORD="$2"
            shift 2
            ;;
        --key-pass)
            KEY_PASSWORD="$2"
            shift 2
            ;;
        --dname)
            DNAME="$2"
            shift 2
            ;;
        --write-prod-props)
            WRITE_PROD_PROPS=true
            shift
            ;;
        --force)
            FORCE=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo -e "${RED}[错误] 未知参数: $1${NC}" >&2
            print_usage >&2
            exit 1
            ;;
    esac
done

resolve_keytool() {
    if command -v keytool &> /dev/null; then
        command -v keytool
        return 0
    fi
    if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/keytool" ]]; then
        echo "${JAVA_HOME}/bin/keytool"
        return 0
    fi
    if [[ "$(uname)" == "Darwin" ]] && command -v /usr/libexec/java_home &> /dev/null; then
        local mac_java_home
        mac_java_home=$(/usr/libexec/java_home 2>/dev/null || true)
        if [[ -n "$mac_java_home" && -x "${mac_java_home}/bin/keytool" ]]; then
            echo "${mac_java_home}/bin/keytool"
            return 0
        fi
    fi
    return 1
}

KEYTOOL_BIN=$(resolve_keytool || true)
if [[ -z "$KEYTOOL_BIN" ]]; then
    echo -e "${RED}[错误] 未找到 keytool，请先安装 JDK 或配置 JAVA_HOME。${NC}" >&2
    exit 1
fi

case "$TYPE" in
    release)
        OUTPUT_PATH="${OUTPUT_PATH:-config/keystore/release.jks}"
        KEY_ALIAS="${KEY_ALIAS:-release_key}"
        DNAME="${DNAME:-CN=App,OU=Mobile,O=Company,L=City,S=State,C=CN}"
        ;;
    test)
        OUTPUT_PATH="${OUTPUT_PATH:-config/keystore/test.jks}"
        KEY_ALIAS="${KEY_ALIAS:-androiddebugkey}"
        STORE_PASSWORD="${STORE_PASSWORD:-android}"
        KEY_PASSWORD="${KEY_PASSWORD:-android}"
        DNAME="${DNAME:-CN=Android Debug,O=Android,C=US}"
        ;;
    *)
        echo -e "${RED}[错误] 不支持的证书类型: ${TYPE}（仅支持 release 或 test）${NC}" >&2
        exit 1
        ;;
esac

if [[ -z "$STORE_PASSWORD" ]]; then
    if [[ ! -t 0 ]]; then
        echo -e "${RED}[错误] 非交互模式下生成 ${TYPE} 证书必须通过 --store-pass 指定密码！${NC}" >&2
        exit 1
    fi
    read -r -s -p "请输入 Keystore 密码 (至少 6 位): " STORE_PASSWORD
    echo ""
fi

if [[ ${#STORE_PASSWORD} -lt 6 ]]; then
    echo -e "${RED}[错误] Keystore 密码长度不能少于 6 位！${NC}" >&2
    exit 1
fi

if [[ -z "$KEY_PASSWORD" ]]; then
    KEY_PASSWORD="$STORE_PASSWORD"
fi

if [[ -f "$OUTPUT_PATH" ]]; then
    if [[ "$FORCE" == true ]]; then
        rm -f "$OUTPUT_PATH"
    else
        echo -e "${RED}[错误] 目标文件已存在: ${OUTPUT_PATH}（如需覆盖请追加 --force）${NC}" >&2
        exit 1
    fi
fi

mkdir -p "$(dirname "$OUTPUT_PATH")"

echo -e "${BLUE}[1/2] 正在生成 ${TYPE} Keystore: ${OUTPUT_PATH}...${NC}"
"$KEYTOOL_BIN" -genkeypair -noprompt \
    -keystore "$OUTPUT_PATH" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storetype PKCS12 \
    -storepass "$STORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "$DNAME"

echo -e "${GREEN}✔ 证书生成成功: ${OUTPUT_PATH}${NC}"
echo -e "${BLUE}[2/2] 提取证书指纹（供 Firebase / 开放平台配置）:${NC}"
"$KEYTOOL_BIN" -list -v \
    -keystore "$OUTPUT_PATH" \
    -alias "$KEY_ALIAS" \
    -storepass "$STORE_PASSWORD" | grep -E "Alias name:|Valid from:|SHA1:|SHA256:" || true

upsert_prop() {
    local file="$1"
    local key="$2"
    local value="$3"
    if grep -qE "^${key}=" "$file"; then
        python3 - "$file" "$key" "$value" <<'PY'
import sys
path, key, val = sys.argv[1], sys.argv[2], sys.argv[3]
lines = open(path, encoding="utf-8").read().splitlines(True)
out = []
replaced = False
for line in lines:
    if line.startswith(f"{key}=") and not replaced:
        out.append(f"{key}={val}\n")
        replaced = True
    else:
        out.append(line)
open(path, "w", encoding="utf-8").writelines(out)
PY
    else
        printf '%s=%s\n' "$key" "$value" >> "$file"
    fi
}

if [[ "$WRITE_PROD_PROPS" == true ]]; then
    if [[ "$TYPE" != "release" ]]; then
        echo -e "${YELLOW}[提示] --write-prod-props 仅对 release 证书生效，已忽略。${NC}"
    else
        if [[ ! -f "config/prod.properties" && -f "config/prod.properties.example" ]]; then
            cp config/prod.properties.example config/prod.properties
        fi
        touch config/prod.properties
        upsert_prop "config/prod.properties" "STORE_FILE" "$OUTPUT_PATH"
        upsert_prop "config/prod.properties" "STORE_PASSWORD" "$STORE_PASSWORD"
        upsert_prop "config/prod.properties" "KEY_ALIAS" "$KEY_ALIAS"
        upsert_prop "config/prod.properties" "KEY_PASSWORD" "$KEY_PASSWORD"
        echo -e "${GREEN}✔ 已将签名配置写入 config/prod.properties（已由 .gitignore 忽略，切勿提交到仓库）。${NC}"
    fi
else
    echo ""
    echo -e "${YELLOW}下一步（详见 config/prod.properties 或环境变量）：${NC}"
    if [[ "$TYPE" == "release" ]]; then
        echo "  cp -n config/prod.properties.example config/prod.properties"
        echo "  STORE_FILE=${OUTPUT_PATH}"
        echo "  KEY_ALIAS=${KEY_ALIAS}"
    else
        echo "  在 config/dev.properties 中设置 STORE_FILE=${OUTPUT_PATH} 即可启用团队统一测试证书。"
    fi
fi
