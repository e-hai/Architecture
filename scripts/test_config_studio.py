#!/usr/bin/env python3
"""
统一 SSOT 配置引擎与双模式 Web 配置台后端单元测试（基于标准库 unittest）。

遵循 AGENTS.md §8 测试先行规范，覆盖：
1. `.xcconfig` 读写与 `#include?` 合并，严格保护并拦截 `ANDROID_STORE_*` 签名字段；
2. Dev / Prod 参数校验（Dev 允许测试 AdMob ID，Prod 禁用；`API_HOST` 协议自动拆解；Secret 红线扫描）；
3. `google-services.json` 与 `GoogleService-Info.plist` 解析及包名/BUNDLE_ID 一致性拦截；
4. 离线 ZIP 配置包导出格式导入与严格环境隔离（Dev 绝不碰 Prod；Prod 绝不碰 Dev 与已追踪模板）；
5. HTTP 服务接口锁定（CSRF 校验、跨环境请求 HTTP 403 拦截、`GET /api/state` 脱敏）。
"""

from __future__ import annotations

import base64
import io
import json
import plistlib
import shutil
import subprocess
import sys
import tempfile
import threading
import unittest
import urllib.error
import urllib.request
import zipfile
from pathlib import Path

SCRIPTS_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPTS_DIR.parent
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import config_studio as cs  # noqa: E402


def _make_firebase_json(pkg_name: str, project_id: str = "demo-firebase") -> bytes:
    payload = {
        "project_info": {"project_id": project_id, "project_number": "123456"},
        "client": [
            {
                "client_info": {
                    "mobilesdk_app_id": "1:123456:android:abcdef",
                    "android_client_info": {"package_name": pkg_name},
                }
            }
        ],
    }
    return json.dumps(payload).encode("utf-8")


def _make_firebase_plist(bundle_id: str, google_app_id: str = "1:123456:ios:abcdef") -> bytes:
    buf = io.BytesIO()
    plistlib.dump({"BUNDLE_ID": bundle_id, "GOOGLE_APP_ID": google_app_id, "PROJECT_ID": "demo-firebase"}, buf)
    return buf.getvalue()


def _make_bundle_zip(
    path: Path,
    manifest: dict[str, object],
    android_json: bytes | None = None,
    ios_plist: bytes | None = None,
) -> Path:
    with zipfile.ZipFile(path, "w") as zf:
        zf.writestr("manifest.json", json.dumps(manifest, ensure_ascii=False))
        if android_json is not None:
            zf.writestr("firebase/google-services.json", android_json)
        if ios_plist is not None:
            zf.writestr("firebase/GoogleService-Info.plist", ios_plist)
    return path


class ConfigStudioTest(unittest.TestCase):
    """覆盖配置真源读写、规则校验、离线包导入与本地 Web API 隔离测试。"""

    def setUp(self) -> None:
        self.tmp_dir = Path(tempfile.mkdtemp(prefix="test_config_studio_"))
        self.root = self._create_fixture_workspace()

    def tearDown(self) -> None:
        shutil.rmtree(self.tmp_dir, ignore_errors=True)

    def _create_fixture_workspace(self) -> Path:
        root = self.tmp_dir / "repo"
        (root / "config").mkdir(parents=True)
        (root / "androidApp" / "src" / "dev").mkdir(parents=True)
        (root / "androidApp" / "src" / "prod").mkdir(parents=True)
        (root / "androidApp" / "src" / "main" / "kotlin" / "com" / "acme" / "app" / "android" / "bridge").mkdir(
            parents=True
        )
        (root / "iosApp" / "Config" / "Firebase" / "Dev").mkdir(parents=True)
        (root / "iosApp" / "Config" / "Firebase" / "Prod").mkdir(parents=True)
        (root / "iosApp" / "Sources" / "Bridge").mkdir(parents=True)
        (root / "shared" / "src" / "commonMain" / "kotlin" / "com" / "acme" / "app" / "shared").mkdir(
            parents=True
        )
        (root / "core" / "ui" / "src" / "commonMain" / "composeResources" / "values-zh").mkdir(parents=True)
        (root / "core" / "log").mkdir(parents=True)
        (root / "core" / "ads").mkdir(parents=True)
        (root / "tools" / "config-studio").mkdir(parents=True)

        shutil.copy(REPO_ROOT / "config" / "dev.xcconfig", root / "config" / "dev.xcconfig")
        shutil.copy(REPO_ROOT / "config" / "prod.xcconfig", root / "config" / "prod.xcconfig")
        # 模拟已完成初始化的包名
        dev_txt = (root / "config" / "dev.xcconfig").read_text(encoding="utf-8").replace("xxx.yyy.zzz", "com.acme.app")
        (root / "config" / "dev.xcconfig").write_text(dev_txt, encoding="utf-8")
        prod_txt = (root / "config" / "prod.xcconfig").read_text(encoding="utf-8").replace("xxx.yyy.zzz", "com.acme.app")
        (root / "config" / "prod.xcconfig").write_text(prod_txt, encoding="utf-8")

        (root / "core" / "ui" / "src" / "commonMain" / "composeResources" / "values-zh" / "strings.xml").write_text(
            '<resources>\n    <string name="app_name">App</string>\n    <string name="welcome_message">欢迎体验 KMP 现代化架构</string>\n</resources>\n',
            encoding="utf-8",
        )
        (root / "shared" / "src" / "commonMain" / "kotlin" / "com" / "acme" / "app" / "shared" / "App.kt").write_text(
            "fun App(autoInitDeferredKits: Boolean = true) {}\n",
            encoding="utf-8",
        )
        (
            root / "androidApp" / "src" / "main" / "kotlin" / "com" / "acme" / "app" / "android" / "bridge" / "NativeKitBootstrap.kt"
        ).write_text("data class AndroidKitBootstrapConfig(val deferCrashReport: Boolean = false)\n", encoding="utf-8")
        (root / "iosApp" / "Sources" / "Bridge" / "NativeKitBootstrap.swift").write_text(
            "public init(deferCrashReport: Bool = false) {}\n", encoding="utf-8"
        )
        (root / "tools" / "config-studio" / "index.html").write_text(
            "<!doctype html><html><body><!-- __STUDIO_BOOTSTRAP__ -->Studio</body></html>",
            encoding="utf-8",
        )
        return root

    def test_repo_ssot_xcconfigs_parse_cleanly(self) -> None:
        dev_cfg = cs.parse_xcconfig(REPO_ROOT / "config" / "dev.xcconfig", resolve_vars=True)
        self.assertEqual(dev_cfg["APP_ID"], "xxx.yyy.zzz.dev")
        self.assertEqual(dev_cfg["PRODUCT_BUNDLE_IDENTIFIER"], "xxx.yyy.zzz.dev")
        self.assertEqual(dev_cfg["API_HOST"], "dev-api.example.com")

        prod_cfg = cs.parse_xcconfig(REPO_ROOT / "config" / "prod.xcconfig", resolve_vars=True)
        self.assertEqual(prod_cfg["APP_ID"], "xxx.yyy.zzz")
        self.assertEqual(prod_cfg["PRODUCT_BUNDLE_IDENTIFIER"], "xxx.yyy.zzz")
        self.assertEqual(prod_cfg["REMOTE_CONFIG_MIN_FETCH_INTERVAL_SECONDS"], "3600")

    def test_signing_keys_hidden_rejected_and_preserved_on_prod_merge(self) -> None:
        local_prod = self.root / "config" / "prod.local.xcconfig"
        local_prod.write_text(
            "ANDROID_STORE_FILE = config/keystore/release.jks\n"
            "ANDROID_STORE_PASSWORD = super_secret_pwd\n"
            "ANDROID_KEY_ALIAS = release_key\n"
            "ANDROID_KEY_PASSWORD = super_secret_pwd\n"
            "API_HOST = old.example.com\n",
            encoding="utf-8",
        )

        # GET state / public config 绝不暴露签名私钥
        pub = cs.get_public_config("prod", self.root)
        for forbidden in cs.FORBIDDEN_SIGNING_KEYS:
            self.assertNotIn(forbidden, pub)

        # 传入签名字段必须报错拦截
        res = cs.validate_config_payload(
            env="prod",
            config={"ANDROID_STORE_PASSWORD": "hack"},
            extras={},
        )
        self.assertFalse(res.is_valid)
        self.assertIn("ANDROID_STORE_PASSWORD", "\n".join(res.errors))

        # 合并非签名字段时，原有 ANDROID_STORE_* 必须原封不动保留
        cs.apply_env_changes(
            env="prod",
            root=self.root,
            config={"API_HOST": "api.acme.com", "APPSFLYER_KEY": "prod_af_key"},
            extras={},
        )
        merged = local_prod.read_text(encoding="utf-8")
        self.assertIn("ANDROID_STORE_FILE = config/keystore/release.jks", merged)
        self.assertIn("ANDROID_STORE_PASSWORD = super_secret_pwd", merged)
        self.assertIn("API_HOST = api.acme.com", merged)
        self.assertIn("APPSFLYER_KEY = prod_af_key", merged)

    def test_validation_dev_vs_prod_admob_url_and_firebase(self) -> None:
        # Dev 允许测试 AdMob ID，并自动拆解 https:// 前缀
        dev_res = cs.validate_config_payload(
            env="dev",
            config={
                "API_HOST": "https://dev.acme.com/api",
                "ANDROID_ADMOB_APP_ID": "ca-app-pub-3940256099942544~3347511713",
                "DEVELOPMENT_TEAM": "a1b2c3d4e5",
            },
            extras={"privacy_mode": "strict"},
        )
        self.assertTrue(dev_res.is_valid, msg=dev_res.errors)
        self.assertEqual(dev_res.normalized_config["API_SCHEME"], "https")
        self.assertEqual(dev_res.normalized_config["API_HOST"], "dev.acme.com/api")
        self.assertEqual(dev_res.normalized_config["DEVELOPMENT_TEAM"], "A1B2C3D4E5")

        # Prod 严禁 Google 官方测试广告 ID 与 Secret 明文
        prod_res = cs.validate_config_payload(
            env="prod",
            config={
                "IOS_ADMOB_APP_ID": "ca-app-pub-3940256099942544~1458002511",
                "ANDROID_ADMOB_BANNER_ID": "ca-app-pub-1111~2222",  # 错误使用 ~
                "APPSFLYER_KEY": "ghp_abcdefghijklmnopqrstuvwxyz123456",
            },
            extras={},
        )
        self.assertFalse(prod_res.is_valid)
        err_text = "\n".join(prod_res.errors)
        self.assertIn("3940256099942544", err_text)
        self.assertIn("ANDROID_ADMOB_BANNER_ID", err_text)
        self.assertIn("机密", err_text)

        # Firebase 包名错配检测
        _, json_errs, _ = cs.validate_firebase_bytes(
            "android", _make_firebase_json("com.wrong.pkg"), expected_app_id="com.acme.app"
        )
        self.assertTrue(any("com.wrong.pkg" in e for e in json_errs))

        meta, plist_errs, _ = cs.validate_firebase_bytes(
            "ios", _make_firebase_plist("com.acme.app"), expected_app_id="com.acme.app"
        )
        self.assertEqual(plist_errs, [])
        self.assertEqual(meta["bundle_id"], "com.acme.app")

    def test_import_bundle_isolation_and_cross_env_block(self) -> None:
        dev_before = (self.root / "config" / "dev.xcconfig").read_text(encoding="utf-8")
        prod_before = (self.root / "config" / "prod.xcconfig").read_text(encoding="utf-8")

        prod_zip = _make_bundle_zip(
            self.tmp_dir / "prod-config-bundle.zip",
            manifest={
                "schemaVersion": 1,
                "env": "prod",
                "config": {
                    "APP_ID": "com.acme.app.rel",
                    "APP_NAME": "Acme",
                    "API_SCHEME": "https",
                    "API_HOST": "api.acme.com",
                    "DEVELOPMENT_TEAM": "Z9Y8X7W6V5",
                    "APPSFLYER_KEY": "af_prod_key",
                    "IOS_APPSFLYER_APP_ID": "id99887766",
                },
                "extras": {"privacy_mode": "strict"},
            },
            android_json=_make_firebase_json("com.acme.app.rel"),
            ios_plist=_make_firebase_plist("com.acme.app.rel"),
        )

        # 跨环境拦截：prod bundle 不能导入 dev
        bad_val, _ = cs.import_bundle(prod_zip, expected_env="dev", root=self.root)
        self.assertFalse(bad_val.is_valid)
        self.assertFalse((self.root / "config" / "prod.local.xcconfig").exists())

        # 正确导入 prod bundle：仅写 config/prod.local.xcconfig 与 Prod Firebase，不动 Dev 与 tracked prod.xcconfig
        ok_val, changes = cs.import_bundle(prod_zip, expected_env="prod", root=self.root)
        self.assertTrue(ok_val.is_valid, msg=ok_val.errors)
        self.assertTrue(len(changes) > 0)
        self.assertEqual((self.root / "config" / "dev.xcconfig").read_text(encoding="utf-8"), dev_before)
        self.assertEqual((self.root / "config" / "prod.xcconfig").read_text(encoding="utf-8"), prod_before)

        prod_local = (self.root / "config" / "prod.local.xcconfig").read_text(encoding="utf-8")
        self.assertIn("APP_ID = com.acme.app.rel", prod_local)
        self.assertIn("API_HOST = api.acme.com", prod_local)
        self.assertIn("IOS_APPSFLYER_APP_ID = 99887766", prod_local)
        self.assertTrue((self.root / "androidApp" / "src" / "prod" / "google-services.json").is_file())
        self.assertTrue((self.root / "iosApp" / "Config" / "Firebase" / "Prod" / "GoogleService-Info.plist").is_file())
        self.assertFalse((self.root / "androidApp" / "src" / "dev" / "google-services.json").exists())
        self.assertIn(
            "autoInitDeferredKits: Boolean = false",
            (self.root / "shared" / "src" / "commonMain" / "kotlin" / "com" / "acme" / "app" / "shared" / "App.kt").read_text(
                encoding="utf-8"
            ),
        )

    def test_http_server_locked_env_csrf_and_upload(self) -> None:
        server = cs.create_studio_server(root=self.root, locked_env="dev", host="127.0.0.1", port=0)
        port = server.server_address[1]
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        try:
            base = f"http://127.0.0.1:{port}"
            with urllib.request.urlopen(f"{base}/") as html_resp:
                self.assertEqual(html_resp.status, 200)
                html_text = html_resp.read().decode("utf-8")
                self.assertIn('"lockedEnv": "dev"', html_text)
                self.assertIn(server.csrf_token, html_text)
            with urllib.request.urlopen(f"{base}/api/state") as resp:
                self.assertEqual(resp.status, 200)
                state = json.loads(resp.read().decode("utf-8"))
            self.assertEqual(state["lockedEnv"], "dev")
            for forbidden in cs.FORBIDDEN_SIGNING_KEYS:
                self.assertNotIn(forbidden, state["config"])

            # 缺少 CSRF token 必须返回 403
            req_no_csrf = urllib.request.Request(
                f"{base}/api/save",
                data=json.dumps({"env": "dev", "config": {"API_HOST": "x.com"}}).encode("utf-8"),
                headers={"Content-Type": "application/json"},
                method="POST",
            )
            with self.assertRaises(urllib.error.HTTPError) as ctx1:
                urllib.request.urlopen(req_no_csrf)
            self.assertEqual(ctx1.exception.code, 403)

            # 跨环境 env="prod" 必须返回 403
            req_wrong_env = urllib.request.Request(
                f"{base}/api/save",
                data=json.dumps({"env": "prod", "config": {"API_HOST": "x.com"}}).encode("utf-8"),
                headers={"Content-Type": "application/json", "X-Studio-Token": server.csrf_token},
                method="POST",
            )
            with self.assertRaises(urllib.error.HTTPError) as ctx2:
                urllib.request.urlopen(req_wrong_env)
            self.assertEqual(ctx2.exception.code, 403)

            # 正常保存 Dev 配置与上传 Dev Firebase plist
            req_save = urllib.request.Request(
                f"{base}/api/save",
                data=json.dumps(
                    {
                        "env": "dev",
                        "config": {"API_HOST": "dev2.acme.com", "APP_NAME": "Acme (Dev)"},
                        "extras": {"app_name_zh": "艾克米", "privacy_mode": "strict"},
                    }
                ).encode("utf-8"),
                headers={"Content-Type": "application/json", "X-Studio-Token": server.csrf_token},
                method="POST",
            )
            with urllib.request.urlopen(req_save) as resp:
                self.assertEqual(resp.status, 200)
            self.assertIn("API_HOST = dev2.acme.com", (self.root / "config" / "dev.xcconfig").read_text(encoding="utf-8"))

            plist_b64 = base64.b64encode(_make_firebase_plist("com.acme.app.dev")).decode("ascii")
            req_upload = urllib.request.Request(
                f"{base}/api/upload-firebase",
                data=json.dumps(
                    {
                        "env": "dev",
                        "platform": "ios",
                        "filename": "GoogleService-Info.plist",
                        "contentBase64": plist_b64,
                    }
                ).encode("utf-8"),
                headers={"Content-Type": "application/json", "X-Studio-Token": server.csrf_token},
                method="POST",
            )
            with urllib.request.urlopen(req_upload) as resp:
                self.assertEqual(resp.status, 200)
            self.assertTrue((self.root / "iosApp" / "Config" / "Firebase" / "Dev" / "GoogleService-Info.plist").is_file())
            self.assertFalse((self.root / "config" / "prod.local.xcconfig").exists())
        finally:
            server.shutdown()
            server.server_close()

    def test_shell_wrapper_import_without_optional_flags_and_relative_path(self) -> None:
        (self.root / "scripts").mkdir(parents=True, exist_ok=True)
        shutil.copy(REPO_ROOT / "scripts" / "config_studio.sh", self.root / "scripts" / "config_studio.sh")
        shutil.copy(REPO_ROOT / "scripts" / "config_studio.py", self.root / "scripts" / "config_studio.py")
        (self.root / "scripts" / "config_studio.sh").chmod(0o755)

        caller_dir = self.tmp_dir / "caller"
        caller_dir.mkdir(parents=True, exist_ok=True)
        _make_bundle_zip(
            caller_dir / "dev-config-bundle.zip",
            manifest={
                "schemaVersion": 1,
                "env": "dev",
                "config": {"API_HOST": "wrapper-dev.acme.com"},
                "extras": {},
            },
        )

        proc = subprocess.run(
            ["bash", str(self.root / "scripts" / "config_studio.sh"), "--import", "dev-config-bundle.zip", "--env", "dev"],
            cwd=caller_dir,
            text=True,
            capture_output=True,
            check=False,
        )
        self.assertEqual(proc.returncode, 0, msg=f"stdout={proc.stdout}\nstderr={proc.stderr}")
        self.assertIn("API_HOST = wrapper-dev.acme.com", (self.root / "config" / "dev.xcconfig").read_text(encoding="utf-8"))

    def test_dev_local_xcconfig_override_and_append_before_include(self) -> None:
        # 1. 无 dev.local.xcconfig 时回退 config/dev.xcconfig 默认值
        dev_cfg = self.root / "config" / "dev.xcconfig"
        parsed_default = cs.parse_xcconfig(dev_cfg)
        self.assertEqual(parsed_default.get("API_HOST"), "dev-api.example.com")
        self.assertEqual(parsed_default.get("DEVELOPMENT_TEAM"), "")

        # 2. 存在 dev.local.xcconfig 时仅个人覆写字段生效，并允许追加新键插入在 #include? 之前
        dev_local = self.root / "config" / "dev.local.xcconfig"
        dev_local.write_text(
            'DEVELOPMENT_TEAM = D1E2V3L4O5\nAPI_SCHEME = http\nAPI_HOST = 192.168.1.50:8080\nNEW_KEY = from_local\n',
            encoding="utf-8",
        )
        parsed_override = cs.parse_xcconfig(dev_cfg)
        self.assertEqual(parsed_override.get("DEVELOPMENT_TEAM"), "D1E2V3L4O5")
        self.assertEqual(parsed_override.get("API_SCHEME"), "http")
        self.assertEqual(parsed_override.get("API_HOST"), "192.168.1.50:8080")
        self.assertEqual(parsed_override.get("APP_ID"), "com.acme.app.dev")

        # 3. _update_xcconfig_text 新增缺失键时必须插入在 #include? "dev.local.xcconfig" 之前
        updated, changed = cs._update_xcconfig_text(
            dev_cfg.read_text(encoding="utf-8"),
            {"NEW_KEY": "from_shared"},
            append_missing=True,
        )
        self.assertEqual(changed, ["NEW_KEY"])
        self.assertLess(updated.index("NEW_KEY = from_shared"), updated.index('#include? "dev.local.xcconfig"'))
        dev_cfg.write_text(updated, encoding="utf-8")
        self.assertEqual(cs.parse_xcconfig(dev_cfg).get("NEW_KEY"), "from_local")


if __name__ == "__main__":
    unittest.main()
