#!/usr/bin/env python3
"""
iOS Firebase (GoogleService-Info.plist) 环境隔离拷贝、增量签名失效与 BUNDLE_ID 校验单元测试。

遵循 AGENTS.md §8 测试先行规范：模拟 Xcode Build Phase 环境变量，验证：
1. 未放 plist 时自动清理残留（并失效 _CodeSignature），无变更时保留现有签名；
2. Dev / Prod 两套 plist 同时存在时，按 CONFIGURATION 仅物理复制当前环境 1 份并在变化时失效 _CodeSignature，完全相同时保留签名；
3. BUNDLE_ID 与 PRODUCT_BUNDLE_IDENTIFIER 不匹配时立即报错阻断，并清理错误与签名残留；
4. `iosApp/Sources/` 下残留未隔离 plist 时立即报错阻断。
"""

from __future__ import annotations

import os
import plistlib
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

SCRIPTS_DIR = Path(__file__).resolve().parent
SCRIPT_PATH = SCRIPTS_DIR / "copy_ios_firebase_plist.sh"


def _write_plist(path: Path, bundle_id: str | None, google_app_id: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    payload: dict[str, str] = {"GOOGLE_APP_ID": google_app_id}
    if bundle_id is not None:
        payload["BUNDLE_ID"] = bundle_id
    with path.open("wb") as fp:
        plistlib.dump(payload, fp)


class CopyIosFirebasePlistTest(unittest.TestCase):
    """验证 iOS Firebase plist 构建期隔离与增量签名失效脚本行为。"""

    def setUp(self) -> None:
        self.tmp_dir = Path(tempfile.mkdtemp(prefix="test_ios_firebase_"))
        self.src_root = self.tmp_dir / "iosApp"
        (self.src_root / "Sources").mkdir(parents=True)
        (self.src_root / "Config" / "Firebase" / "Dev").mkdir(parents=True)
        (self.src_root / "Config" / "Firebase" / "Prod").mkdir(parents=True)
        self.target_build_dir = self.tmp_dir / "build"
        self.resources_subpath = "iosApp.app"
        self.bundle_dir = self.target_build_dir / self.resources_subpath
        self.bundle_dir.mkdir(parents=True)

    def tearDown(self) -> None:
        shutil.rmtree(self.tmp_dir, ignore_errors=True)

    def _create_fake_codesig(self) -> Path:
        code_sig = self.bundle_dir / "_CodeSignature" / "CodeResources"
        code_sig.parent.mkdir(parents=True, exist_ok=True)
        code_sig.write_text("signed", encoding="utf-8")
        return code_sig.parent

    def _run_script(
        self,
        configuration: str = "Debug",
        product_bundle_id: str = "com.acme.app.dev",
    ) -> subprocess.CompletedProcess[str]:
        self.assertTrue(SCRIPT_PATH.is_file(), f"缺少脚本: {SCRIPT_PATH}")
        env = dict(os.environ)
        env.update(
            {
                "SRCROOT": str(self.src_root),
                "CONFIGURATION": configuration,
                "PRODUCT_BUNDLE_IDENTIFIER": product_bundle_id,
                "TARGET_BUILD_DIR": str(self.target_build_dir),
                "UNLOCALIZED_RESOURCES_FOLDER_PATH": self.resources_subpath,
            }
        )
        return subprocess.run(
            ["bash", str(SCRIPT_PATH)],
            env=env,
            text=True,
            capture_output=True,
            check=False,
        )

    def test_missing_plist_cleans_stale_and_invalidates_only_when_removed(self) -> None:
        code_sig_dir = self._create_fake_codesig()

        # 本身就没有 plist 时不触碰已有 _CodeSignature（保证增量零开销）
        noop_proc = self._run_script(configuration="Debug", product_bundle_id="com.acme.app.dev")
        self.assertEqual(noop_proc.returncode, 0, msg=noop_proc.stdout + noop_proc.stderr)
        self.assertTrue(code_sig_dir.exists(), "无 plist 变化时应保留已有 _CodeSignature")

        # 若产物内残留旧 plist，清理旧 plist 时必须一并失效 _CodeSignature
        stale_plist = self.bundle_dir / "GoogleService-Info.plist"
        _write_plist(stale_plist, "com.stale.app", "1:000:ios:stale")
        clean_proc = self._run_script(configuration="Debug", product_bundle_id="com.acme.app.dev")
        self.assertEqual(clean_proc.returncode, 0, msg=clean_proc.stdout + clean_proc.stderr)
        self.assertFalse(stale_plist.exists(), "未配置 plist 时必须清理产物内旧残留")
        self.assertFalse(code_sig_dir.exists(), "清理残留 plist 后必须失效 _CodeSignature 触发重签")

    def test_copy_invalidates_codesig_on_change_and_preserves_when_identical(self) -> None:
        dev_src = self.src_root / "Config" / "Firebase" / "Dev" / "GoogleService-Info.plist"
        prod_src = self.src_root / "Config" / "Firebase" / "Prod" / "GoogleService-Info.plist"
        _write_plist(dev_src, "com.acme.app.dev", "1:111:ios:dev")
        _write_plist(prod_src, "com.acme.app", "1:222:ios:prod")

        dest_plist = self.bundle_dir / "GoogleService-Info.plist"
        code_sig_dir = self._create_fake_codesig()

        # 首次新增复制到 .app 时必须失效 _CodeSignature
        dev_proc = self._run_script(configuration="Debug", product_bundle_id="com.acme.app.dev")
        self.assertEqual(dev_proc.returncode, 0, msg=dev_proc.stdout + dev_proc.stderr)
        self.assertTrue(dest_plist.is_file())
        self.assertFalse(code_sig_dir.exists(), "新增或更新 plist 后必须失效 _CodeSignature")
        with dest_plist.open("rb") as fp:
            loaded_dev = plistlib.load(fp)
        self.assertEqual(loaded_dev["GOOGLE_APP_ID"], "1:111:ios:dev")

        # 模拟 Xcode 重签生成新的 _CodeSignature 后再次以相同 plist 构建：应保留 _CodeSignature
        code_sig_dir = self._create_fake_codesig()
        repeat_proc = self._run_script(configuration="Debug", product_bundle_id="com.acme.app.dev")
        self.assertEqual(repeat_proc.returncode, 0, msg=repeat_proc.stdout + repeat_proc.stderr)
        self.assertTrue(code_sig_dir.exists(), "plist 未变化时应保留已有 _CodeSignature")

        # 修改 plist 或切换 Release 环境：必须再次失效 _CodeSignature 并复制 Prod plist
        prod_proc = self._run_script(configuration="Release", product_bundle_id="com.acme.app")
        self.assertEqual(prod_proc.returncode, 0, msg=prod_proc.stdout + prod_proc.stderr)
        self.assertFalse(code_sig_dir.exists(), "plist 内容变化后必须失效 _CodeSignature")
        with dest_plist.open("rb") as fp:
            loaded_prod = plistlib.load(fp)
        self.assertEqual(loaded_prod["GOOGLE_APP_ID"], "1:222:ios:prod")
        self.assertEqual(loaded_prod["BUNDLE_ID"], "com.acme.app")

    def test_bundle_id_mismatch_hard_fails_and_removes_stale_and_codesig(self) -> None:
        stale_plist = self.bundle_dir / "GoogleService-Info.plist"
        _write_plist(stale_plist, "com.acme.app", "1:222:ios:old")
        code_sig_dir = self._create_fake_codesig()

        wrong_prod_src = self.src_root / "Config" / "Firebase" / "Prod" / "GoogleService-Info.plist"
        _write_plist(wrong_prod_src, "com.acme.app.dev", "1:111:ios:dev")

        proc = self._run_script(configuration="Release", product_bundle_id="com.acme.app")
        self.assertNotEqual(proc.returncode, 0)
        combined = proc.stdout + proc.stderr
        self.assertIn("error:", combined)
        self.assertIn("BUNDLE_ID", combined)
        self.assertFalse(stale_plist.exists(), "校验失败不得在产物目录残留错误或旧版本 plist")
        self.assertFalse(code_sig_dir.exists(), "清理错误残留后应失效 _CodeSignature")

    def test_legacy_sources_plist_hard_fails(self) -> None:
        legacy_src = self.src_root / "Sources" / "GoogleService-Info.plist"
        _write_plist(legacy_src, "com.acme.app.dev", "1:111:ios:legacy")

        proc = self._run_script(configuration="Debug", product_bundle_id="com.acme.app.dev")
        self.assertNotEqual(proc.returncode, 0)
        combined = proc.stdout + proc.stderr
        self.assertIn("error:", combined)
        self.assertIn("Sources/", combined)


if __name__ == "__main__":
    unittest.main()
