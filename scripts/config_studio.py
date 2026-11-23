#!/usr/bin/env python3
"""
双端单一真源配置引擎与零依赖 Web 配置台服务端（纯 Python 3.8+ 标准库）。

核心能力：
1. 原生解析与安全合并 `config/dev.xcconfig` / `config/prod.xcconfig` (+ `#include? "prod.local.xcconfig"`)；
2. 强制环境物理隔离（`--env dev|prod`），Web 与离线导出包全程禁止暴露或写入 `ANDROID_STORE_*` 签名字段；
3. 校检 `API_HOST`、AdMob (`~` vs `/`, Prod 禁用测试 ID)、Team ID、Firebase `google-services.json` / `GoogleService-Info.plist`；
4. 启动本机 `127.0.0.1` Web Config Studio（带 CSRF 防护与环境硬锁定），并支持 CLI `--import <bundle.zip>` 一键落盘。
"""

from __future__ import annotations

import argparse
import base64
import dataclasses
import functools
import http.server
import json
import plistlib
import re
import secrets
import shutil
import socketserver
import subprocess
import sys
import tempfile
import webbrowser
import zipfile
from pathlib import Path
from typing import Any, Iterable
from xml.sax.saxutils import escape as xml_escape

ALL_KITS: tuple[str, ...] = (
    "log",
    "crashreport",
    "analytics",
    "abtesting",
    "mmp",
    "ads",
    "pay",
    "push",
    "video",
    "pag",
)

FORBIDDEN_SIGNING_KEYS: frozenset[str] = frozenset(
    {
        "ANDROID_STORE_FILE",
        "ANDROID_STORE_PASSWORD",
        "ANDROID_KEY_ALIAS",
        "ANDROID_KEY_PASSWORD",
    }
)

EDITABLE_XCCONFIG_KEYS: tuple[str, ...] = (
    "APP_ID",
    "APP_NAME",
    "VERSION_NAME",
    "VERSION_CODE",
    "DEVELOPMENT_TEAM",
    "API_SCHEME",
    "API_HOST",
    "ENABLE_HTTP_LOG",
    "ANDROID_ADMOB_APP_ID",
    "ANDROID_ADMOB_BANNER_ID",
    "ANDROID_ADMOB_INTERSTITIAL_ID",
    "ANDROID_ADMOB_REWARDED_ID",
    "ANDROID_ADMOB_APP_OPEN_ID",
    "IOS_ADMOB_APP_ID",
    "IOS_ADMOB_BANNER_ID",
    "IOS_ADMOB_INTERSTITIAL_ID",
    "IOS_ADMOB_REWARDED_ID",
    "IOS_ADMOB_APP_OPEN_ID",
    "APPSFLYER_KEY",
    "IOS_APPSFLYER_APP_ID",
    "ANALYTICS_DEBUG_MODE",
    "REMOTE_CONFIG_MIN_FETCH_INTERVAL_SECONDS",
    "PAY_SUBS_IDS",
    "PAY_CONSUMABLE_IDS",
    "PAY_NON_CONSUMABLE_IDS",
)

EDITABLE_KEY_SET: frozenset[str] = frozenset(EDITABLE_XCCONFIG_KEYS)
ADMOB_APP_ID_KEYS: frozenset[str] = frozenset({"ANDROID_ADMOB_APP_ID", "IOS_ADMOB_APP_ID"})
ADMOB_UNIT_ID_KEYS: frozenset[str] = frozenset(
    {
        "ANDROID_ADMOB_BANNER_ID",
        "ANDROID_ADMOB_INTERSTITIAL_ID",
        "ANDROID_ADMOB_REWARDED_ID",
        "ANDROID_ADMOB_APP_OPEN_ID",
        "IOS_ADMOB_BANNER_ID",
        "IOS_ADMOB_INTERSTITIAL_ID",
        "IOS_ADMOB_REWARDED_ID",
        "IOS_ADMOB_APP_OPEN_ID",
    }
)
CSV_KEYS: frozenset[str] = frozenset({"PAY_SUBS_IDS", "PAY_CONSUMABLE_IDS", "PAY_NON_CONSUMABLE_IDS"})
BOOL_KEYS: frozenset[str] = frozenset({"ENABLE_HTTP_LOG", "ANALYTICS_DEBUG_MODE"})

GOOGLE_TEST_ADMOB_PUBLISHER_ID = "3940256099942544"

INCLUDE_RE = re.compile(r'^#include(\?)?\s+"([^"]+)"')
INLINE_COMMENT_RE = re.compile(r"\s+//.*$")
VAR_REF_RE = re.compile(r"\$\(([A-Za-z0-9_]+)\)")

PACKAGE_RE = re.compile(r"^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$")
APP_ID_RE = re.compile(r"^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$")
PREFIX_RE = re.compile(r"^[a-z][a-z0-9]*$")
FEATURE_RE = re.compile(r"^[a-z][a-z0-9_]*$")
TEAM_ID_RE = re.compile(r"^[A-Z0-9]{10}$")
HOST_RE = re.compile(r"^[^\s/$.?#][^\s]*$")
ADMOB_APP_ID_RE = re.compile(r"^ca-app-pub-\d+~\d+$")
ADMOB_UNIT_ID_RE = re.compile(r"^ca-app-pub-\d+/\d+$")
DIGITS_RE = re.compile(r"^\d+$")

SECRET_PATTERNS: tuple[tuple[re.Pattern[str], str], ...] = (
    (re.compile(r"ghp_[A-Za-z0-9]{20,}"), "GitHub Classic PAT (ghp_...)"),
    (re.compile(r"github_pat_[A-Za-z0-9_]{20,}"), "GitHub Fine-grained PAT (github_pat_...)"),
    (re.compile(r"gho_[A-Za-z0-9]{20,}"), "GitHub OAuth Token (gho_...)"),
    (re.compile(r"-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----"), "私钥文件内容 (PRIVATE KEY)"),
    (re.compile(r'"private_key_id"\s*:\s*"[^"]+"'), "Firebase Service Account 私钥字段"),
)


@dataclasses.dataclass
class ValidationResult:
    """配置校验结果与归一化后的键值。"""

    errors: list[str]
    warnings: list[str]
    infos: list[str]
    normalized_config: dict[str, str]
    normalized_extras: dict[str, str]

    @property
    def is_valid(self) -> bool:
        return len(self.errors) == 0


def _read_xcconfig_into(path: Path, target: dict[str, str], visited: set[Path]) -> None:
    if not path.is_file():
        return
    resolved = path.resolve()
    if resolved in visited:
        return
    visited.add(resolved)
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        trimmed = raw_line.strip()
        if not trimmed or trimmed.startswith("//"):
            continue
        inc_match = INCLUDE_RE.match(trimmed)
        if inc_match:
            _read_xcconfig_into(path.parent / inc_match.group(2), target, visited)
            continue
        if "=" in trimmed:
            key, val = trimmed.split("=", 1)
            key = key.strip()
            raw_val = val.strip()
            clean_val = "" if raw_val.startswith("//") else INLINE_COMMENT_RE.sub("", raw_val).strip()
            target[key] = clean_val


def _expand_vars(raw_map: dict[str, str], value: str, depth: int = 0) -> str:
    if depth >= 5 or "$(" not in value:
        return value
    return VAR_REF_RE.sub(
        lambda m: _expand_vars(raw_map, raw_map.get(m.group(1), ""), depth + 1),
        value,
    )


def parse_xcconfig(path: Path, resolve_vars: bool = True) -> dict[str, str]:
    """解析单个 `.xcconfig` 及其 `#include?` 链，返回键值映射。"""
    values: dict[str, str] = {}
    _read_xcconfig_into(path, values, set())
    if not resolve_vars:
        return values
    return {k: _expand_vars(values, v) for k, v in values.items()}


def get_public_config(env: str, root: Path) -> dict[str, str]:
    """读取指定环境对外可见的配置（严格剥离 `ANDROID_STORE_*` 签名参数）。"""
    cfg_file = root / "config" / ("dev.xcconfig" if env == "dev" else "prod.xcconfig")
    parsed = parse_xcconfig(cfg_file, resolve_vars=True) if cfg_file.is_file() else {}
    return {k: parsed.get(k, "") for k in EDITABLE_XCCONFIG_KEYS}


def _scan_secrets_in_mapping(values: dict[str, Any]) -> list[str]:
    findings: list[str] = []
    for key, val in values.items():
        text = str(val or "")
        for pattern, desc in SECRET_PATTERNS:
            if pattern.search(text):
                findings.append(f"字段 {key} 检测到禁止写入的机密信息：{desc}")
    return findings


def validate_config_payload(
    env: str,
    config: dict[str, Any],
    extras: dict[str, Any] | None = None,
    project_init: dict[str, Any] | None = None,
    expected_env: str | None = None,
) -> ValidationResult:
    """校验配置提交负载，返回阻断错误、提示及归一化键值。"""
    errors: list[str] = []
    warnings: list[str] = []
    infos: list[str] = []
    extras = extras or {}

    if env not in {"dev", "prod"}:
        errors.append(f"非法环境标识：{env}（仅支持 dev 或 prod）")
    elif expected_env is not None and env != expected_env:
        errors.append(f"环境隔离拦截：当前锁定 --env {expected_env}，拒绝处理 env={env} 的配置。")

    if project_init is not None and env != "dev":
        errors.append("环境隔离拦截：正式环境 (prod) 严禁执行首次立项骨架初始化。")

    # 严格拦截任何尝试读写 Android 签名密码/路径的行为
    for key in config:
        if key in FORBIDDEN_SIGNING_KEYS:
            errors.append(f"安全红线拦截：配置台禁止读写签名字段 {key}，请使用 scripts/gen_keystore.sh 或 CI 环境变量。")

    errors.extend(_scan_secrets_in_mapping(config))
    errors.extend(_scan_secrets_in_mapping(extras))

    normalized: dict[str, str] = {}
    for key, raw_val in config.items():
        if key in FORBIDDEN_SIGNING_KEYS:
            continue
        if key not in EDITABLE_KEY_SET:
            warnings.append(f"忽略未识别字段：{key}")
            continue
        val = str(raw_val or "").strip()
        if key in CSV_KEYS:
            parts = [p.strip() for p in val.replace("，", ",").split(",")]
            val = ",".join(p for p in parts if p)
        elif key in BOOL_KEYS and val:
            upper = val.upper()
            val = "YES" if upper in {"YES", "TRUE", "1"} else ("NO" if upper in {"NO", "FALSE", "0"} else val)
        elif key == "DEVELOPMENT_TEAM" and val:
            val = val.upper()
        elif key == "IOS_APPSFLYER_APP_ID" and val.lower().startswith("id") and val[2:].isdigit():
            val = val[2:]
            infos.append(f"IOS_APPSFLYER_APP_ID 已自动去除 'id' 前缀 -> '{val}'")
        normalized[key] = val

    # API_HOST 若意外带有 http:// 或 https://，自动拆解为 API_SCHEME + API_HOST
    if "API_HOST" in normalized and normalized["API_HOST"]:
        raw_host = normalized["API_HOST"]
        if raw_host.lower().startswith("https://"):
            normalized["API_SCHEME"] = "https"
            normalized["API_HOST"] = raw_host[8:].strip("/")
            infos.append("API_HOST 中包含的 https:// 已自动拆解为 API_SCHEME=https")
        elif raw_host.lower().startswith("http://"):
            normalized["API_SCHEME"] = "http"
            normalized["API_HOST"] = raw_host[7:].strip("/")
            infos.append("API_HOST 中包含的 http:// 已自动拆解为 API_SCHEME=http")

    if "API_SCHEME" in normalized and normalized["API_SCHEME"] and normalized["API_SCHEME"] not in {"http", "https"}:
        errors.append(f"API_SCHEME 非法：'{normalized['API_SCHEME']}'（仅支持 https 或 http）")

    if "API_HOST" in normalized and normalized["API_HOST"]:
        host_val = normalized["API_HOST"]
        if "://" in host_val or not HOST_RE.match(host_val):
            errors.append(f"API_HOST 格式非法：'{host_val}'（请填写纯域名/路径，如 api.example.com，不含空格）")

    if normalized.get("APP_ID") and not APP_ID_RE.match(normalized["APP_ID"]):
        errors.append(f"APP_ID 格式非法：'{normalized['APP_ID']}'（须符合 com.example.app 规范）")

    if normalized.get("DEVELOPMENT_TEAM") and not TEAM_ID_RE.match(normalized["DEVELOPMENT_TEAM"]):
        errors.append(
            f"DEVELOPMENT_TEAM 格式非法：'{normalized['DEVELOPMENT_TEAM']}'（应为 10 位大写字母或数字，如 A1B2C3D4E5）"
        )

    for num_key in ("VERSION_CODE", "REMOTE_CONFIG_MIN_FETCH_INTERVAL_SECONDS"):
        if normalized.get(num_key) and not DIGITS_RE.match(normalized[num_key]):
            errors.append(f"{num_key} 必须为非负整数，当前值 '{normalized[num_key]}'")

    if normalized.get("IOS_APPSFLYER_APP_ID") and not DIGITS_RE.match(normalized["IOS_APPSFLYER_APP_ID"]):
        errors.append(f"IOS_APPSFLYER_APP_ID 必须为纯数字 Apple App ID，当前值 '{normalized['IOS_APPSFLYER_APP_ID']}'")

    if normalized.get("ENABLE_HTTP_LOG") and normalized["ENABLE_HTTP_LOG"] not in {"YES", "NO"}:
        errors.append(f"ENABLE_HTTP_LOG 仅支持 YES / NO，当前值 '{normalized['ENABLE_HTTP_LOG']}'")

    if normalized.get("ANALYTICS_DEBUG_MODE") and normalized["ANALYTICS_DEBUG_MODE"] not in {"YES", "NO"}:
        errors.append(f"ANALYTICS_DEBUG_MODE 仅支持 YES / NO，当前值 '{normalized['ANALYTICS_DEBUG_MODE']}'")

    for key in sorted(ADMOB_APP_ID_KEYS | ADMOB_UNIT_ID_KEYS):
        val = normalized.get(key, "")
        if not val:
            continue
        if env == "prod" and GOOGLE_TEST_ADMOB_PUBLISHER_ID in val:
            errors.append(f"{key} 严禁在正式环境填写 Google 官方测试广告 ID ({GOOGLE_TEST_ADMOB_PUBLISHER_ID})：{val}")
            continue
        if key in ADMOB_APP_ID_KEYS and not ADMOB_APP_ID_RE.match(val):
            errors.append(f"{key} 格式错误：应为 ca-app-pub-数字~数字（使用 ~ 分隔），当前值 '{val}'")
        elif key in ADMOB_UNIT_ID_KEYS and not ADMOB_UNIT_ID_RE.match(val):
            errors.append(f"{key} 格式错误：应为 ca-app-pub-数字/数字（使用 / 分隔），当前值 '{val}'")

    norm_extras = {
        "app_name_zh": str(extras.get("app_name_zh", "") or "").strip(),
        "privacy_mode": str(extras.get("privacy_mode", "") or "").strip().lower(),
    }
    if norm_extras["privacy_mode"] and norm_extras["privacy_mode"] not in {"standard", "strict"}:
        errors.append(f"privacy_mode 值非法：'{norm_extras['privacy_mode']}'（仅支持 standard 或 strict）")

    if project_init is not None:
        errors.extend(_scan_secrets_in_mapping(project_init))
        app_name = str(project_init.get("app_name", "") or "").strip()
        pkg = str(project_init.get("package_name", "") or "").strip()
        prefix = str(project_init.get("plugin_prefix", "") or "").strip()
        if not app_name or app_name == "Architecture":
            errors.append("立项参数 app_name 不能为空且不能保持默认占位 'Architecture'")
        if not pkg or pkg == "xxx.yyy.zzz" or not PACKAGE_RE.match(pkg):
            errors.append(f"立项参数 package_name 非法：'{pkg}'（须为全小写多段反域名，如 com.acme.app）")
        if prefix and (prefix == "myproject" or not PREFIX_RE.match(prefix)):
            errors.append(f"立项参数 plugin_prefix 非法：'{prefix}'（仅支持字母开头的纯小写字母数字）")
        raw_feats = project_init.get("initial_features") or []
        if isinstance(raw_feats, str):
            raw_feats = [x.strip() for x in raw_feats.replace("，", ",").split(",") if x.strip()]
        for feat in raw_feats:
            norm_feat = str(feat).strip().lower().replace("-", "_")
            if norm_feat == "debug":
                errors.append("initial_features 中不能包含 'debug'，请用调试面板开关控制")
            elif norm_feat and norm_feat != "home" and not FEATURE_RE.match(norm_feat):
                errors.append(f"initial_features 包含非法模块名 '{feat}'")
        excluded = [str(k).strip().lower() for k in (project_init.get("excluded_kits") or [])]
        if "log" in excluded:
            errors.append("'log' 是全工程核心基础日志门面，禁止在立项阶段移除")

    return ValidationResult(
        errors=errors,
        warnings=warnings,
        infos=infos,
        normalized_config=normalized,
        normalized_extras=norm_extras,
    )


def validate_firebase_bytes(
    platform: str,
    raw_bytes: bytes,
    expected_app_id: str = "",
) -> tuple[dict[str, str], list[str], list[str]]:
    """解析并校验 Firebase 配置文件 (`google-services.json` / `GoogleService-Info.plist`)。"""
    errors: list[str] = []
    warnings: list[str] = []
    meta: dict[str, str] = {}

    if platform == "android":
        try:
            data = json.loads(raw_bytes.decode("utf-8"))
        except Exception as exc:  # noqa: BLE001
            return meta, [f"google-services.json 解析失败：{exc}"], warnings
        project_id = str((data.get("project_info") or {}).get("project_id", ""))
        clients = data.get("client")
        if not project_id or not isinstance(clients, list) or not clients:
            return meta, ["google-services.json 缺少有效的 project_info.project_id 或 client 节点"], warnings
        packages: list[str] = []
        for client in clients:
            pkg = (
                ((client or {}).get("client_info") or {}).get("android_client_info") or {}
            ).get("package_name", "")
            if pkg:
                packages.append(str(pkg))
        if not packages:
            return meta, ["google-services.json 中未找到任何 package_name 声明"], warnings
        meta = {"project_id": project_id, "package_names": ",".join(packages)}
        if expected_app_id and expected_app_id not in packages:
            errors.append(
                f"google-services.json 包名 ({', '.join(packages)}) 与当前环境 APP_ID ({expected_app_id}) 不匹配，请确认未放错 Dev/Prod 文件！"
            )
        return meta, errors, warnings

    if platform == "ios":
        try:
            plist_data = plistlib.loads(raw_bytes)
        except Exception as exc:  # noqa: BLE001
            return meta, [f"GoogleService-Info.plist 解析失败：{exc}"], warnings
        if not isinstance(plist_data, dict):
            return meta, ["GoogleService-Info.plist 根节点格式无效"], warnings
        bundle_id = str(plist_data.get("BUNDLE_ID", "") or "")
        google_app_id = str(plist_data.get("GOOGLE_APP_ID", "") or "")
        project_id = str(plist_data.get("PROJECT_ID", "") or "")
        if not bundle_id or not google_app_id:
            return meta, ["GoogleService-Info.plist 缺少 BUNDLE_ID 或 GOOGLE_APP_ID 字段"], warnings
        meta = {"bundle_id": bundle_id, "google_app_id": google_app_id, "project_id": project_id}
        if expected_app_id and bundle_id != expected_app_id:
            errors.append(
                f"GoogleService-Info.plist 的 BUNDLE_ID ({bundle_id}) 与当前环境 APP_ID ({expected_app_id}) 不匹配，请确认未放错 Dev/Prod 文件！"
            )
        return meta, errors, warnings

    return meta, [f"未知的 Firebase 平台类型：{platform}"], warnings


def _update_xcconfig_text(content: str, updates: dict[str, str], append_missing: bool) -> tuple[str, list[str]]:
    changed_keys: list[str] = []
    updated = content
    missing: list[str] = []
    for key, val in updates.items():
        if key in FORBIDDEN_SIGNING_KEYS:
            raise ValueError(f"Refusing to modify forbidden signing key: {key}")
        pattern = re.compile(rf"^({re.escape(key)}\s*=).*$", flags=re.MULTILINE)
        if pattern.search(updated):
            new_text = pattern.sub(lambda m, v=val: f"{m.group(1)} {v}" if v else f"{m.group(1)} ", updated, count=1)
            if new_text != updated:
                updated = new_text
                changed_keys.append(key)
        elif append_missing and val:
            missing.append(f"{key} = {val}")
            changed_keys.append(key)
    if missing:
        missing_block = "\n".join(missing) + "\n"
        inc_match = re.search(r'^[ \t]*#include(?:\?)?\s+"[^"]+"', updated, flags=re.MULTILINE)
        if inc_match:
            pos = inc_match.start()
            prefix = updated[:pos]
            if prefix and not prefix.endswith("\n"):
                prefix += "\n"
            updated = prefix + missing_block + updated[pos:]
        else:
            if updated and not updated.endswith("\n"):
                updated += "\n"
            updated += missing_block
    return updated, changed_keys


def _find_single_file(root: Path, rel_base: str, glob_pattern: str) -> Path | None:
    base = root / rel_base
    if not base.exists():
        return None
    matches = sorted(base.glob(glob_pattern))
    return matches[0] if matches else None


def _apply_zh_strings(app_name_zh: str, root: Path, dry_run: bool, changes: list[str]) -> None:
    if not app_name_zh:
        return
    zh_xml = root / "core" / "ui" / "src" / "commonMain" / "composeResources" / "values-zh" / "strings.xml"
    if not zh_xml.is_file():
        return
    escaped_zh = xml_escape(app_name_zh).replace("'", "\\'")
    old_text = zh_xml.read_text(encoding="utf-8")
    new_text = re.sub(
        r'(<string\s+name="app_name">)[^<]*(</string>)',
        lambda m: f"{m.group(1)}{escaped_zh}{m.group(2)}",
        old_text,
    )
    new_text = re.sub(
        r'(<string\s+name="welcome_message">)[^<]*(</string>)',
        lambda m: f"{m.group(1)}欢迎体验 {escaped_zh}{m.group(2)}",
        new_text,
    )
    if new_text != old_text:
        if not dry_run:
            zh_xml.write_text(new_text, encoding="utf-8")
        changes.append(f"[中文资源] values-zh/strings.xml 更新 app_name 与 welcome_message -> '{app_name_zh}'")


def _apply_privacy_mode(privacy_mode: str, root: Path, dry_run: bool, changes: list[str]) -> bool:
    if not privacy_mode:
        return False
    auto_init_val = "false" if privacy_mode == "strict" else "true"
    defer_crash_val = "true" if privacy_mode == "strict" else "false"
    kotlin_changed = False

    app_kt = _find_single_file(root, "shared/src/commonMain/kotlin", "**/shared/App.kt")
    if app_kt and app_kt.is_file():
        old_text = app_kt.read_text(encoding="utf-8")
        new_text = re.sub(
            r"(autoInitDeferredKits\s*:\s*Boolean\s*=\s*)(?:true|false)",
            lambda m: f"{m.group(1)}{auto_init_val}",
            old_text,
            count=1,
        )
        if new_text != old_text:
            if not dry_run:
                app_kt.write_text(new_text, encoding="utf-8")
            kotlin_changed = True
            changes.append(f"[隐私合规:{privacy_mode}] {app_kt.relative_to(root)} -> autoInitDeferredKits = {auto_init_val}")

    and_bs = _find_single_file(root, "androidApp/src/main/kotlin", "**/android/bridge/NativeKitBootstrap.kt")
    if and_bs and and_bs.is_file():
        old_text = and_bs.read_text(encoding="utf-8")
        new_text = re.sub(
            r"(val\s+deferCrashReport\s*:\s*Boolean\s*=\s*)(?:true|false)",
            lambda m: f"{m.group(1)}{defer_crash_val}",
            old_text,
            count=1,
        )
        if new_text != old_text:
            if not dry_run:
                and_bs.write_text(new_text, encoding="utf-8")
            kotlin_changed = True
            changes.append(f"[隐私合规:{privacy_mode}] {and_bs.relative_to(root)} -> deferCrashReport = {defer_crash_val}")

    ios_bs = root / "iosApp" / "Sources" / "Bridge" / "NativeKitBootstrap.swift"
    if ios_bs.is_file():
        old_text = ios_bs.read_text(encoding="utf-8")
        new_text = re.sub(
            r"(deferCrashReport\s*:\s*Bool\s*=\s*)(?:true|false)",
            lambda m: f"{m.group(1)}{defer_crash_val}",
            old_text,
            count=1,
        )
        if new_text != old_text:
            if not dry_run:
                ios_bs.write_text(new_text, encoding="utf-8")
            changes.append(
                f"[隐私合规:{privacy_mode}] iosApp/Sources/Bridge/NativeKitBootstrap.swift -> deferCrashReport = {defer_crash_val}"
            )

    return kotlin_changed


def apply_env_changes(
    env: str,
    root: Path,
    config: dict[str, str],
    extras: dict[str, Any] | None = None,
    firebase_files: dict[str, bytes] | None = None,
    dry_run: bool = False,
) -> list[str]:
    """按严格环境边界将配置、隐私开关与 Firebase 文件写入唯一真源与隔离目录。"""
    changes: list[str] = []
    extras = extras or {}
    firebase_files = firebase_files or {}

    if env == "dev":
        dev_cfg = root / "config" / "dev.xcconfig"
        if dev_cfg.is_file() and config:
            old_text = dev_cfg.read_text(encoding="utf-8")
            new_text, keys = _update_xcconfig_text(old_text, config, append_missing=True)
            if keys:
                if not dry_run:
                    dev_cfg.write_text(new_text, encoding="utf-8")
                changes.append(f"[Dev 唯一真源] config/dev.xcconfig 更新字段: {', '.join(keys)}")

        app_name_zh = str(extras.get("app_name_zh", "") or "").strip()
        if app_name_zh:
            _apply_zh_strings(app_name_zh, root, dry_run, changes)

    elif env == "prod":
        non_empty_prod = {k: v for k, v in config.items() if v}
        if non_empty_prod:
            prod_local = root / "config" / "prod.local.xcconfig"
            existed = prod_local.is_file()
            old_text = (
                prod_local.read_text(encoding="utf-8")
                if existed
                else (
                    "// ===================================================================\n"
                    "// 本地正式环境私有覆写 (已纳入 .gitignore，Android & iOS 共用 SSOT)\n"
                    "// ===================================================================\n"
                )
            )
            new_text, keys = _update_xcconfig_text(old_text, non_empty_prod, append_missing=True)
            if not existed or keys:
                if not dry_run:
                    prod_local.parent.mkdir(parents=True, exist_ok=True)
                    prod_local.write_text(new_text, encoding="utf-8")
                action = "增量合并" if existed else "生成（已入 .gitignore）"
                changes.append(f"[Prod 本地真源] config/prod.local.xcconfig 已{action}：{', '.join(keys)}")

    privacy_mode = str(extras.get("privacy_mode", "") or "").strip().lower()
    if privacy_mode:
        _apply_privacy_mode(privacy_mode, root, dry_run, changes)

    # Firebase 文件写入对应隔离目录
    if "android" in firebase_files:
        dest = root / "androidApp" / "src" / ("dev" if env == "dev" else "prod") / "google-services.json"
        if not dry_run:
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(firebase_files["android"])
        changes.append(f"[Firebase-Android:{env}] 已写入 {dest.relative_to(root)}")

    if "ios" in firebase_files:
        dest = root / "iosApp" / "Config" / "Firebase" / ("Dev" if env == "dev" else "Prod") / "GoogleService-Info.plist"
        if not dry_run:
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(firebase_files["ios"])
        changes.append(f"[Firebase-iOS:{env}] 已写入 {dest.relative_to(root)}")

    return changes


def run_project_init(root: Path, project_init: dict[str, Any], dry_run: bool = False) -> list[str]:
    """在 Dev 环境下调用 `init_project.sh` 执行首次工程立项骨架改写。"""
    app_name = str(project_init.get("app_name", "")).strip()
    pkg = str(project_init.get("package_name", "")).strip()
    formal_id = str(project_init.get("formal_app_id", "") or pkg).strip()
    test_id = str(project_init.get("test_app_id", "") or f"{formal_id}.dev").strip()
    prefix = str(project_init.get("plugin_prefix", "") or pkg.split(".")[-1]).strip()

    raw_feats = project_init.get("initial_features") or []
    if isinstance(raw_feats, str):
        feats = [f.strip() for f in raw_feats.replace("，", ",").split(",") if f.strip()]
    else:
        feats = [str(f).strip() for f in raw_feats if str(f).strip()]
    feats_csv = ",".join(f for f in feats if f.lower() != "home")

    excluded = [str(k).strip().lower() for k in (project_init.get("excluded_kits") or []) if str(k).strip()]
    excluded_csv = ",".join(excluded)
    debug_panel = bool(project_init.get("debug_panel", True))

    cmd = [
        "bash",
        str(root / "init_project.sh"),
        "--package",
        pkg,
        "--name",
        app_name,
        "--app-id",
        formal_id,
        "--test-app-id",
        test_id,
        "--prefix",
        prefix,
        "--yes",
    ]
    if feats_csv:
        cmd.extend(["--features", feats_csv])
    if excluded_csv:
        cmd.extend(["--exclude-kits", excluded_csv])
    if not debug_panel:
        cmd.append("--no-debug")
    if dry_run:
        cmd.append("--dry-run")

    proc = subprocess.run(cmd, cwd=root, text=True, capture_output=True, check=False)
    if proc.returncode != 0:
        raise RuntimeError(f"init_project.sh 执行失败 (exit={proc.returncode}):\n{proc.stdout}\n{proc.stderr}")

    desc = f"[首次立项] init_project.sh ({pkg}, name={app_name})"
    return [f"[预演] {desc}" if dry_run else desc]


def _maybe_run_spotless(root: Path) -> None:
    gradlew = root / "gradlew"
    if gradlew.is_file():
        subprocess.run([str(gradlew), "spotlessApply"], cwd=root, capture_output=True, check=False)


def _is_template_repo(root: Path) -> bool:
    return (root / "androidApp" / "src" / "main" / "kotlin" / "xxx" / "yyy" / "zzz").is_dir()


def import_bundle(
    bundle_path: Path,
    expected_env: str,
    root: Path,
    dry_run: bool = False,
) -> tuple[ValidationResult, list[str]]:
    """校验并落盘离线导出的 `{dev|prod}-config-bundle.zip` 配置包。"""
    if not bundle_path.is_file():
        empty_res = ValidationResult([f"找不到配置包：{bundle_path}"], [], [], {}, {})
        return empty_res, []

    manifest: dict[str, Any] = {}
    firebase_files: dict[str, bytes] = {}
    try:
        with zipfile.ZipFile(bundle_path, "r") as zf:
            for info in zf.infolist():
                parts = Path(info.filename).parts
                if info.filename.startswith("/") or ".." in parts:
                    return ValidationResult([f"配置包存在非法路径：{info.filename}"], [], [], {}, {}), []
            if "manifest.json" not in zf.namelist():
                return ValidationResult(["配置包缺少 manifest.json"], [], [], {}, {}), []
            manifest = json.loads(zf.read("manifest.json").decode("utf-8"))
            if "firebase/google-services.json" in zf.namelist():
                firebase_files["android"] = zf.read("firebase/google-services.json")
            if "firebase/GoogleService-Info.plist" in zf.namelist():
                firebase_files["ios"] = zf.read("firebase/GoogleService-Info.plist")
    except Exception as exc:  # noqa: BLE001
        return ValidationResult([f"配置包解析失败：{exc}"], [], [], {}, {}), []

    env = str(manifest.get("env", "")).strip().lower()
    config_raw = manifest.get("config") or {}
    extras_raw = manifest.get("extras") or {}
    project_init = manifest.get("project_init")

    val_res = validate_config_payload(
        env=env,
        config=config_raw if isinstance(config_raw, dict) else {},
        extras=extras_raw if isinstance(extras_raw, dict) else {},
        project_init=project_init if isinstance(project_init, dict) else None,
        expected_env=expected_env,
    )
    if not val_res.is_valid:
        return val_res, []

    # 确定 Firebase 校验使用的预期 APP_ID
    current_public = get_public_config(env, root)
    expected_app_id = val_res.normalized_config.get("APP_ID") or ""
    if not expected_app_id and project_init and isinstance(project_init, dict):
        pkg = str(project_init.get("package_name", "")).strip()
        expected_app_id = str(project_init.get("test_app_id", "") or (f"{pkg}.dev" if pkg else ""))
    if not expected_app_id:
        expected_app_id = current_public.get("APP_ID", "")

    for platform, raw_bytes in firebase_files.items():
        _, fb_errs, fb_warns = validate_firebase_bytes(platform, raw_bytes, expected_app_id=expected_app_id)
        val_res.errors.extend(fb_errs)
        val_res.warnings.extend(fb_warns)

    if not val_res.is_valid:
        return val_res, []

    changes: list[str] = []
    if project_init and isinstance(project_init, dict):
        if _is_template_repo(root):
            changes.extend(run_project_init(root, project_init, dry_run=dry_run))
        else:
            val_res.infos.append("检测到工程已完成过包名初始化，已跳过 init_project.sh 改名，仅更新配置与资源")

    changes.extend(
        apply_env_changes(
            env=env,
            root=root,
            config=val_res.normalized_config,
            extras=val_res.normalized_extras,
            firebase_files=firebase_files,
            dry_run=dry_run,
        )
    )
    if not dry_run and val_res.normalized_extras.get("privacy_mode"):
        _maybe_run_spotless(root)

    return val_res, changes


def _inspect_workspace_state(root: Path, locked_env: str) -> dict[str, Any]:
    is_template = _is_template_repo(root)
    enabled_kits = [k for k in ALL_KITS if (root / "core" / k).is_dir()]
    debug_panel_enabled = (root / "features" / "debug").is_dir()

    app_kt = _find_single_file(root, "shared/src/commonMain/kotlin", "**/shared/App.kt")
    privacy_mode = "standard"
    if app_kt and app_kt.is_file():
        if "autoInitDeferredKits: Boolean = false" in app_kt.read_text(encoding="utf-8"):
            privacy_mode = "strict"

    app_name_zh = ""
    zh_xml = root / "core" / "ui" / "src" / "commonMain" / "composeResources" / "values-zh" / "strings.xml"
    if zh_xml.is_file():
        m = re.search(r'<string\s+name="app_name">([^<]*)</string>', zh_xml.read_text(encoding="utf-8"))
        if m:
            app_name_zh = m.group(1)

    fb_android = root / "androidApp" / "src" / ("dev" if locked_env == "dev" else "prod") / "google-services.json"
    fb_ios = root / "iosApp" / "Config" / "Firebase" / ("Dev" if locked_env == "dev" else "Prod") / "GoogleService-Info.plist"
    fb_state: dict[str, Any] = {"android": {"present": False}, "ios": {"present": False}}
    if fb_android.is_file():
        meta, _, _ = validate_firebase_bytes("android", fb_android.read_bytes())
        fb_state["android"] = {"present": True, **meta}
    if fb_ios.is_file():
        meta, _, _ = validate_firebase_bytes("ios", fb_ios.read_bytes())
        fb_state["ios"] = {"present": True, **meta}

    return {
        "mode": "server",
        "lockedEnv": locked_env,
        "isTemplate": is_template,
        "enabledKits": enabled_kits,
        "debugPanelEnabled": debug_panel_enabled,
        "config": get_public_config(locked_env, root),
        "extras": {"app_name_zh": app_name_zh, "privacy_mode": privacy_mode},
        "firebase": fb_state,
    }


class StudioHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    """线程安全的可锁定环境本地 HTTP 配置台服务。"""

    daemon_threads = True

    def __init__(self, server_address: tuple[str, int], handler_cls: type[http.server.BaseHTTPRequestHandler], root: Path, locked_env: str):
        self.repo_root = root
        self.locked_env = locked_env
        self.csrf_token = secrets.token_urlsafe(24)
        super().__init__(server_address, handler_cls)


class StudioRequestHandler(http.server.BaseHTTPRequestHandler):
    """处理本地 Web 配置台页面、状态读取、配置写入、Firebase 上传与初始化请求。"""

    server: StudioHTTPServer  # type: ignore[assignment]

    def log_message(self, format: str, *args: Any) -> None:  # noqa: A002
        return

    def _send_json(self, status: int, payload: dict[str, Any]) -> None:
        raw = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(raw)))
        self.end_headers()
        self.wfile.write(raw)

    def _read_json_body(self) -> dict[str, Any] | None:
        length = int(self.headers.get("Content-Length", "0") or "0")
        if length <= 0 or length > 20 * 1024 * 1024:
            self._send_json(400, {"ok": False, "errors": ["请求体大小非法或为空"]})
            return None
        try:
            return json.loads(self.rfile.read(length).decode("utf-8"))
        except Exception as exc:  # noqa: BLE001
            self._send_json(400, {"ok": False, "errors": [f"JSON 解析失败：{exc}"]})
            return None

    def _check_csrf_and_env(self, body: dict[str, Any]) -> bool:
        token = self.headers.get("X-Studio-Token", "")
        if not token or not secrets.compare_digest(token, self.server.csrf_token):
            self._send_json(403, {"ok": False, "errors": ["CSRF Token 校验失败，请刷新页面后重试"]})
            return False
        req_env = str(body.get("env", "")).strip().lower()
        if req_env != self.server.locked_env:
            self._send_json(
                403,
                {"ok": False, "errors": [f"环境隔离拦截：服务端已锁定 --env {self.server.locked_env}，拒绝 env={req_env}"]},
            )
            return False
        return True

    def do_GET(self) -> None:  # noqa: N802
        if self.path in {"/", "/index.html"}:
            html_path = self.server.repo_root / "tools" / "config-studio" / "index.html"
            if not html_path.is_file():
                self.send_error(404, "tools/config-studio/index.html not found")
                return
            bootstrap_json = json.dumps(
                {
                    "mode": "server",
                    "lockedEnv": self.server.locked_env,
                    "csrfToken": self.server.csrf_token,
                }
            )
            bootstrap_tag = f"<script>window.__STUDIO_BOOTSTRAP__ = {bootstrap_json};</script>"
            html = html_path.read_text(encoding="utf-8").replace("<!-- __STUDIO_BOOTSTRAP__ -->", bootstrap_tag, 1)
            raw = html.encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(raw)))
            self.end_headers()
            self.wfile.write(raw)
            return

        if self.path == "/api/state":
            self._send_json(200, _inspect_workspace_state(self.server.repo_root, self.server.locked_env))
            return

        self.send_error(404)

    def do_POST(self) -> None:  # noqa: N802
        body = self._read_json_body()
        if body is None or not self._check_csrf_and_env(body):
            return

        root = self.server.repo_root
        env = self.server.locked_env

        if self.path == "/api/save":
            val = validate_config_payload(
                env=env,
                config=body.get("config") or {},
                extras=body.get("extras") or {},
                expected_env=env,
            )
            if not val.is_valid:
                self._send_json(400, {"ok": False, "errors": val.errors, "warnings": val.warnings, "infos": val.infos})
                return
            changes = apply_env_changes(env=env, root=root, config=val.normalized_config, extras=val.normalized_extras)
            if val.normalized_extras.get("privacy_mode"):
                _maybe_run_spotless(root)
            self._send_json(200, {"ok": True, "changes": changes, "warnings": val.warnings, "infos": val.infos, "state": _inspect_workspace_state(root, env)})
            return

        if self.path == "/api/upload-firebase":
            platform = str(body.get("platform", "")).strip().lower()
            b64 = str(body.get("contentBase64", "") or "")
            expected_app_id = str(body.get("expectedAppId", "") or "").strip() or get_public_config(env, root).get("APP_ID", "")
            try:
                raw_bytes = base64.b64decode(b64, validate=True)
            except Exception as exc:  # noqa: BLE001
                self._send_json(400, {"ok": False, "errors": [f"文件编码无效：{exc}"]})
                return
            meta, fb_errs, fb_warns = validate_firebase_bytes(platform, raw_bytes, expected_app_id=expected_app_id)
            if fb_errs:
                self._send_json(400, {"ok": False, "errors": fb_errs, "warnings": fb_warns})
                return
            changes = apply_env_changes(env=env, root=root, config={}, extras={}, firebase_files={platform: raw_bytes})
            self._send_json(200, {"ok": True, "meta": meta, "changes": changes, "warnings": fb_warns, "state": _inspect_workspace_state(root, env)})
            return

        if self.path == "/api/init-project":
            if env != "dev" or not _is_template_repo(root):
                self._send_json(403, {"ok": False, "errors": ["仅允许在未初始化的 Dev 环境下执行一键立项！"]})
                return
            project_init = body.get("project_init") or {}
            cfg = body.get("config") or {}
            extras = body.get("extras") or {}
            val = validate_config_payload(env="dev", config=cfg, extras=extras, project_init=project_init, expected_env="dev")
            if not val.is_valid:
                self._send_json(400, {"ok": False, "errors": val.errors, "warnings": val.warnings, "infos": val.infos})
                return
            changes = []
            try:
                changes.extend(run_project_init(root, project_init))
            except Exception as exc:  # noqa: BLE001
                self._send_json(500, {"ok": False, "errors": [str(exc)]})
                return
            changes.extend(apply_env_changes(env="dev", root=root, config=val.normalized_config, extras=val.normalized_extras))
            if val.normalized_extras.get("privacy_mode"):
                _maybe_run_spotless(root)
            self._send_json(200, {"ok": True, "changes": changes, "warnings": val.warnings, "infos": val.infos, "state": _inspect_workspace_state(root, env)})
            return

        if self.path == "/api/import-bundle":
            b64 = str(body.get("bundleBase64", "") or "")
            try:
                zip_bytes = base64.b64decode(b64, validate=True)
            except Exception as exc:  # noqa: BLE001
                self._send_json(400, {"ok": False, "errors": [f"压缩包解码失败：{exc}"]})
                return
            with tempfile.NamedTemporaryFile(suffix=".zip", delete=False) as tf:
                tf.write(zip_bytes)
                tmp_zip = Path(tf.name)
            try:
                val, changes = import_bundle(tmp_zip, expected_env=env, root=root)
            finally:
                tmp_zip.unlink(missing_ok=True)
            if not val.is_valid:
                self._send_json(400, {"ok": False, "errors": val.errors, "warnings": val.warnings, "infos": val.infos})
                return
            self._send_json(200, {"ok": True, "changes": changes, "warnings": val.warnings, "infos": val.infos, "state": _inspect_workspace_state(root, env)})
            return

        self.send_error(404)


def create_studio_server(root: Path, locked_env: str, host: str = "127.0.0.1", port: int = 8765) -> StudioHTTPServer:
    """构造绑定到指定地址且锁定环境的 HTTP 服务实例。"""
    return StudioHTTPServer((host, port), StudioRequestHandler, root=root.resolve(), locked_env=locked_env)


def _cmd_serve(args: argparse.Namespace) -> int:
    root = Path(args.root).resolve()
    server = create_studio_server(root=root, locked_env=args.env, host="127.0.0.1", port=args.port)
    url = f"http://127.0.0.1:{server.server_address[1]}/"
    print(f"====================================================")
    print(f"  KMP Config Studio 已启动（环境已物理锁定: {args.env.upper()}）")
    print(f"  访问地址: {url}")
    print(f"  目标工程: {root}")
    print(f"  按 Ctrl+C 停止服务")
    print(f"====================================================", flush=True)
    if not args.no_open:
        webbrowser.open(url)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n[Config Studio] 服务已停止。")
    finally:
        server.server_close()
    return 0


def _cmd_import(args: argparse.Namespace) -> int:
    root = Path(args.root).resolve()
    val, changes = import_bundle(Path(args.bundle), expected_env=args.env, root=root, dry_run=args.dry_run)
    for info in val.infos:
        print(f"  ℹ {info}")
    for warn in val.warnings:
        print(f"  ⚠ {warn}")
    if not val.is_valid:
        print("\n[阻断错误]")
        for err in val.errors:
            print(f"  ✘ {err}")
        return 1
    prefix = "[预演] " if args.dry_run else "✔ "
    print(f"{prefix}配置包导入完成 (env={args.env})：")
    for item in changes:
        print(f"  - {item}")
    return 0


def main(argv: Iterable[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="KMP 双端单一真源配置台与离线包导入工具")
    sub = parser.add_subparsers(dest="cmd", required=True)

    p_serve = sub.add_parser("serve", help="启动本地 Web 配置界面（锁定单个环境）")
    p_serve.add_argument("--env", choices=("dev", "prod"), required=True)
    p_serve.add_argument("--port", type=int, default=8765)
    p_serve.add_argument("--no-open", action="store_true")
    p_serve.add_argument("--root", default=".")
    p_serve.set_defaults(func=_cmd_serve)

    p_imp = sub.add_parser("import", help="导入离线导出的 dev/prod-config-bundle.zip")
    p_imp.add_argument("bundle", help="zip 配置包路径")
    p_imp.add_argument("--env", choices=("dev", "prod"), required=True)
    p_imp.add_argument("--dry-run", action="store_true")
    p_imp.add_argument("--root", default=".")
    p_imp.set_defaults(func=_cmd_import)

    args = parser.parse_args(list(argv) if argv is not None else None)
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
