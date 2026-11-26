#!/usr/bin/env python3
"""Unit tests for `scripts/hooks/check_artifact_size.sh`."""

from __future__ import annotations

import json
import os
import pathlib
import subprocess
import tempfile
import unittest

REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent.parent
SIZE_SCRIPT = REPO_ROOT / "scripts" / "hooks" / "check_artifact_size.sh"


def run_size_check(
    args: list[str],
    extra_env: dict[str, str] | None = None,
) -> subprocess.CompletedProcess[str]:
    """Execute check_artifact_size.sh with optional environment variables."""
    env = {"PATH": os.environ.get("PATH", "/usr/bin:/bin")}
    if extra_env:
        env.update(extra_env)
    return subprocess.run(
        ["bash", str(SIZE_SCRIPT), *args],
        env=env,
        capture_output=True,
        text=True,
        check=False,
    )


class CheckArtifactSizeTest(unittest.TestCase):
    """Covers file/bundle measurement, warn/fail thresholds, JSON report, and GITHUB_STEP_SUMMARY."""

    def test_file_under_threshold_writes_json_report(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = pathlib.Path(tmp)
            apk = tmp_path / "app-prod-release.apk"
            apk.write_bytes(b"0" * (2 * 1024 * 1024))
            report = tmp_path / "size.json"

            res = run_size_check(
                [
                    "--platform",
                    "android",
                    "--artifact",
                    str(apk),
                    "--warn-mb",
                    "5",
                    "--fail-mb",
                    "10",
                    "--report-json",
                    str(report),
                ],
            )
            self.assertEqual(res.returncode, 0, msg=res.stderr)
            self.assertIn("-> OK", res.stdout)
            data = json.loads(report.read_text(encoding="utf-8"))
            self.assertEqual(data["platform"], "android")
            self.assertEqual(data["status"], "OK")
            self.assertEqual(data["size_mb"], 2.0)

    def test_directory_bundle_warns_and_fails_on_thresholds(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = pathlib.Path(tmp)
            app_dir = tmp_path / "iosApp.app"
            fw_dir = app_dir / "Frameworks" / "Shared.framework"
            fw_dir.mkdir(parents=True)
            (fw_dir / "Shared").write_bytes(b"1" * (2 * 1024 * 1024))
            (app_dir / "iosApp").write_bytes(b"2" * (1 * 1024 * 1024))
            summary = tmp_path / "summary.md"

            warn_res = run_size_check(
                [
                    "--platform",
                    "ios",
                    "--artifact",
                    str(app_dir),
                    "--warn-mb",
                    "2.5",
                    "--fail-mb",
                    "5",
                ],
                extra_env={"GITHUB_STEP_SUMMARY": str(summary)},
            )
            self.assertEqual(warn_res.returncode, 0, msg=warn_res.stderr)
            self.assertIn("::warning::", warn_res.stdout)
            self.assertIn("Shared.framework=2.00 MB", warn_res.stdout)
            self.assertIn("**WARN**", summary.read_text(encoding="utf-8"))

            fail_res = run_size_check(
                [
                    "--platform",
                    "ios",
                    "--artifact",
                    str(app_dir),
                    "--warn-mb",
                    "2",
                    "--fail-mb",
                    "2.5",
                ],
            )
            self.assertEqual(fail_res.returncode, 1)
            self.assertIn("::error::", fail_res.stderr)

    def test_missing_artifact_fails_cleanly(self) -> None:
        res = run_size_check(
            [
                "--platform",
                "android",
                "--artifact",
                "/tmp/does-not-exist-artifact.apk",
                "--warn-mb",
                "10",
            ],
        )
        self.assertEqual(res.returncode, 1)


if __name__ == "__main__":
    unittest.main()
