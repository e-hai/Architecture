#!/usr/bin/env python3
"""Unit tests for `scripts/hooks/upload_ios_dsym.sh` and `PrivacyInfo.xcprivacy` compliance."""

from __future__ import annotations

import os
import pathlib
import plistlib
import stat
import subprocess
import tempfile
import unittest

REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent.parent
UPLOAD_SCRIPT = REPO_ROOT / "scripts" / "hooks" / "upload_ios_dsym.sh"
PRIVACY_INFO = REPO_ROOT / "iosApp" / "Sources" / "PrivacyInfo.xcprivacy"


def write_plist(path: pathlib.Path, google_app_id: str) -> None:
    """Create a minimal GoogleService-Info.plist with the given GOOGLE_APP_ID."""
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("wb") as fp:
        plistlib.dump(
            {
                "BUNDLE_ID": "xxx.yyy.zzz",
                "GOOGLE_APP_ID": google_app_id,
            },
            fp,
        )


def run_upload_script(env_overrides: dict[str, str]) -> subprocess.CompletedProcess[str]:
    """Invoke upload_ios_dsym.sh in a controlled environment."""
    env = {
        "PATH": os.environ.get("PATH", "/usr/bin:/bin"),
        **env_overrides,
    }
    return subprocess.run(
        ["bash", str(UPLOAD_SCRIPT)],
        env=env,
        capture_output=True,
        text=True,
        check=False,
    )


class UploadIosDsymAndPrivacyTest(unittest.TestCase):
    """Covers dSYM upload gating conditions and iOS Privacy Manifest alignment."""

    def test_skips_debug_and_simulator_builds(self) -> None:
        debug_res = run_upload_script({"CONFIGURATION": "Debug"})
        self.assertEqual(debug_res.returncode, 0)
        self.assertIn("跳过 dSYM 上传", debug_res.stdout)

        sim_res = run_upload_script(
            {
                "CONFIGURATION": "Release",
                "EFFECTIVE_PLATFORM_NAME": "-iphonesimulator",
            },
        )
        self.assertEqual(sim_res.returncode, 0)
        self.assertIn("Simulator", sim_res.stdout)

    def test_skips_missing_or_placeholder_plist(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = pathlib.Path(tmp)
            dsym_dir = tmp_path / "dSYMs"
            dsym_dir.mkdir()

            missing_res = run_upload_script(
                {
                    "CONFIGURATION": "Release",
                    "EFFECTIVE_PLATFORM_NAME": "-iphoneos",
                    "DWARF_DSYM_FOLDER_PATH": str(dsym_dir),
                    "GOOGLE_SERVICE_PLIST": str(tmp_path / "Missing.plist"),
                },
            )
            self.assertEqual(missing_res.returncode, 0)
            self.assertIn("未检测到 GoogleService-Info.plist", missing_res.stdout)

            placeholder_plist = tmp_path / "Placeholder.plist"
            write_plist(placeholder_plist, "1:000000000000:ios:placeholder")
            placeholder_res = run_upload_script(
                {
                    "CONFIGURATION": "Release",
                    "EFFECTIVE_PLATFORM_NAME": "-iphoneos",
                    "DWARF_DSYM_FOLDER_PATH": str(dsym_dir),
                    "GOOGLE_SERVICE_PLIST": str(placeholder_plist),
                    "PLIST_BUDDY_BIN": "/nonexistent/PlistBuddy",
                },
            )
            self.assertEqual(placeholder_res.returncode, 0)
            self.assertIn("占位符", placeholder_res.stdout)

    def test_warns_gracefully_when_upload_symbols_binary_missing(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = pathlib.Path(tmp)
            dsym_dir = tmp_path / "dSYMs"
            dsym_dir.mkdir()
            plist_path = tmp_path / "GoogleService-Info.plist"
            write_plist(plist_path, "1:987654321000:ios:abcdef123456")

            res = run_upload_script(
                {
                    "CONFIGURATION": "Release",
                    "EFFECTIVE_PLATFORM_NAME": "-iphoneos",
                    "DWARF_DSYM_FOLDER_PATH": str(dsym_dir),
                    "GOOGLE_SERVICE_PLIST": str(plist_path),
                    "PLIST_BUDDY_BIN": "/nonexistent/PlistBuddy",
                    "FIREBASE_UPLOAD_SYMBOLS_BIN": "/nonexistent/upload-symbols",
                },
            )
            self.assertEqual(res.returncode, 0)
            self.assertIn("upload-symbols", res.stdout)

    def test_invokes_upload_symbols_on_release_device_with_valid_plist(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = pathlib.Path(tmp)
            dsym_dir = tmp_path / "dSYMs"
            dsym_dir.mkdir()
            plist_path = tmp_path / "GoogleService-Info.plist"
            write_plist(plist_path, "1:987654321000:ios:abcdef123456")

            args_capture = tmp_path / "captured_args.txt"
            stub_bin = tmp_path / "upload-symbols"
            stub_bin.write_text(
                f"#!/usr/bin/env bash\nprintf '%s\\n' \"$@\" > {str(args_capture)!r}\n",
                encoding="utf-8",
            )
            stub_bin.chmod(stub_bin.stat().st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

            res = run_upload_script(
                {
                    "CONFIGURATION": "Release",
                    "EFFECTIVE_PLATFORM_NAME": "-iphoneos",
                    "DWARF_DSYM_FOLDER_PATH": str(dsym_dir),
                    "GOOGLE_SERVICE_PLIST": str(plist_path),
                    "PLIST_BUDDY_BIN": "/nonexistent/PlistBuddy",
                    "FIREBASE_UPLOAD_SYMBOLS_BIN": str(stub_bin),
                },
            )
            self.assertEqual(res.returncode, 0, msg=f"STDOUT:\n{res.stdout}\nSTDERR:\n{res.stderr}")
            self.assertIn("dSYM 上传完成", res.stdout)
            recorded = args_capture.read_text(encoding="utf-8").splitlines()
            self.assertEqual(recorded, ["-gsp", str(plist_path), "-p", "ios", str(dsym_dir)])

    def test_privacy_manifest_valid_and_aligned_with_att_and_sdks(self) -> None:
        self.assertTrue(PRIVACY_INFO.is_file())
        with PRIVACY_INFO.open("rb") as fp:
            manifest = plistlib.load(fp)

        self.assertIs(manifest.get("NSPrivacyTracking"), True)
        collected = manifest.get("NSPrivacyCollectedDataTypes")
        self.assertIsInstance(collected, list)
        self.assertGreaterEqual(len(collected), 4)

        api_types = {
            item.get("NSPrivacyAccessedAPIType"): item.get("NSPrivacyAccessedAPITypeReasons")
            for item in manifest.get("NSPrivacyAccessedAPITypes", [])
            if isinstance(item, dict)
        }
        self.assertEqual(api_types.get("NSPrivacyAccessedAPICategoryUserDefaults"), ["CA92.1"])
        self.assertEqual(api_types.get("NSPrivacyAccessedAPICategoryFileTimestamp"), ["C617.1"])
        self.assertEqual(api_types.get("NSPrivacyAccessedAPICategorySystemBootTime"), ["35F9.1"])
        self.assertEqual(api_types.get("NSPrivacyAccessedAPICategoryDiskSpace"), ["E174.1"])


if __name__ == "__main__":
    unittest.main()
