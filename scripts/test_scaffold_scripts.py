#!/usr/bin/env python3
"""Sandboxed regression tests for root scaffold shell scripts.

Verifies create_feature.sh, remove_feature.sh, manage_kit.sh, and init_project.sh
inside isolated temporary copies of the repository without mutating the workspace.
"""

from __future__ import annotations

import pathlib
import shutil
import stat
import subprocess
import tempfile
import unittest

REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent

IGNORE_PATTERNS = shutil.ignore_patterns(
    ".git",
    ".gradle",
    ".idea",
    ".kotlin",
    ".swiftpm",
    ".build",
    "build",
    "DerivedData",
    "xcuserdata",
    "iosApp.xcodeproj",
    "__pycache__",
    "*.pyc",
    ".DS_Store",
)


def copy_repo_to_sandbox(sandbox_root: pathlib.Path) -> pathlib.Path:
    """Copy the repository into a fresh sandbox directory with a no-op gradlew."""
    repo_dir = sandbox_root / "repo"
    shutil.copytree(REPO_ROOT, repo_dir, ignore=IGNORE_PATTERNS, symlinks=True)

    # Stub gradlew so shell scripts don't launch slow nested Gradle daemons in fast tests.
    gradlew = repo_dir / "gradlew"
    gradlew.write_text("#!/usr/bin/env bash\nexit 0\n", encoding="utf-8")
    gradlew.chmod(gradlew.stat().st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

    for script in [*repo_dir.glob("*.sh"), *(repo_dir / "scripts").glob("*.sh")]:
        script.chmod(script.stat().st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

    return repo_dir


def run_script(repo_dir: pathlib.Path, args: list[str]) -> subprocess.CompletedProcess[str]:
    """Run a repository script inside the sandbox and fail on non-zero exit."""
    result = subprocess.run(
        args,
        cwd=repo_dir,
        capture_output=True,
        text=True,
        check=False,
    )
    if result.returncode != 0:
        raise AssertionError(
            f"Command {' '.join(args)} failed with exit code {result.returncode}\n"
            f"STDOUT:\n{result.stdout}\nSTDERR:\n{result.stderr}",
        )
    return result


class ScaffoldScriptsRegressionTest(unittest.TestCase):
    """End-to-end sandbox tests for the four root scaffold lifecycle scripts."""

    def test_create_and_remove_feature_roundtrip(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            repo_dir = copy_repo_to_sandbox(pathlib.Path(tmp))

            run_script(repo_dir, ["./create_feature.sh", "billing", "--with-data"])

            self.assertTrue((repo_dir / "features/billing/api/build.gradle.kts").is_file())
            self.assertTrue((repo_dir / "features/billing/impl/build.gradle.kts").is_file())
            self.assertTrue(
                (
                    repo_dir
                    / "features/billing/impl/src/commonMain/kotlin/xxx/yyy/zzz/feature/billing/impl/BillingScreen.kt"
                ).is_file(),
            )
            self.assertTrue(
                (
                    repo_dir
                    / "features/billing/impl/src/commonMain/kotlin/xxx/yyy/zzz/feature/billing/impl/BillingRepository.kt"
                ).is_file(),
            )

            settings = (repo_dir / "settings.gradle.kts").read_text(encoding="utf-8")
            shared_gradle = (repo_dir / "shared/build.gradle.kts").read_text(encoding="utf-8")
            bootstrap = (
                repo_dir / "shared/src/commonMain/kotlin/xxx/yyy/zzz/shared/di/AppBootstrap.kt"
            ).read_text(encoding="utf-8")
            nav_graph = (
                repo_dir
                / "shared/src/commonMain/kotlin/xxx/yyy/zzz/shared/navigation/AppNavGraph.kt"
            ).read_text(encoding="utf-8")

            self.assertIn('include(":features:billing:api")', settings)
            self.assertIn('include(":features:billing:impl")', settings)
            self.assertIn('project(":features:billing:api")', shared_gradle)
            self.assertIn("billingModule", bootstrap)
            self.assertIn("BillingNavKey", nav_graph)
            self.assertIn("BillingScreen", nav_graph)

            run_script(repo_dir, ["./remove_feature.sh", "billing"])

            self.assertFalse((repo_dir / "features/billing").exists())
            self.assertNotIn(
                ":features:billing:",
                (repo_dir / "settings.gradle.kts").read_text(encoding="utf-8"),
            )
            self.assertNotIn(
                ":features:billing:",
                (repo_dir / "shared/build.gradle.kts").read_text(encoding="utf-8"),
            )
            self.assertNotIn(
                "billingModule",
                (
                    repo_dir
                    / "shared/src/commonMain/kotlin/xxx/yyy/zzz/shared/di/AppBootstrap.kt"
                ).read_text(encoding="utf-8"),
            )
            self.assertNotIn(
                "BillingNavKey",
                (
                    repo_dir
                    / "shared/src/commonMain/kotlin/xxx/yyy/zzz/shared/navigation/AppNavGraph.kt"
                ).read_text(encoding="utf-8"),
            )

    def test_manage_kit_list_and_remove_video(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            repo_dir = copy_repo_to_sandbox(pathlib.Path(tmp))

            list_res = run_script(repo_dir, ["./manage_kit.sh", "list"])
            self.assertIn("video", list_res.stdout)

            run_script(repo_dir, ["./manage_kit.sh", "remove", "video"])

            self.assertFalse((repo_dir / "core/video").exists())
            self.assertFalse(
                (
                    repo_dir
                    / "androidApp/src/main/kotlin/xxx/yyy/zzz/android/bridge/delegates/AndroidVideoDelegate.kt"
                ).exists(),
            )
            self.assertFalse(
                (repo_dir / "iosApp/Sources/Bridge/Delegates/IosVideoDelegate.swift").exists(),
            )
            self.assertFalse(
                (
                    repo_dir
                    / "features/debug/impl/src/commonMain/kotlin/xxx/yyy/zzz/feature/debug/impl/sections/VideoDebugSection.kt"
                ).exists(),
            )
            self.assertNotIn(
                ":core:video",
                (repo_dir / "settings.gradle.kts").read_text(encoding="utf-8"),
            )
            proguard = (repo_dir / "androidApp/proguard-rules.pro").read_text(encoding="utf-8")
            self.assertNotIn("[KIT:video:", proguard)
            self.assertNotIn("androidx.media3", proguard)

    def test_init_project_renames_package_and_schemas(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            repo_dir = copy_repo_to_sandbox(pathlib.Path(tmp))

            run_script(
                repo_dir,
                [
                    "./init_project.sh",
                    "--package",
                    "com.acme.demo",
                    "--name",
                    "Acme Demo",
                    "--prefix",
                    "acme",
                    "--features",
                    "feed",
                    "--exclude-kits",
                    "video",
                    "--yes",
                ],
            )

            self.assertTrue(
                (
                    repo_dir / "androidApp/src/main/kotlin/com/acme/demo/android/MainActivity.kt"
                ).is_file(),
            )
            self.assertTrue(
                (repo_dir / "shared/src/commonMain/kotlin/com/acme/demo/shared/App.kt").is_file(),
            )
            self.assertTrue(
                (
                    repo_dir / "core/database/schemas/com.acme.demo.core.database.AppDatabase/1.json"
                ).is_file(),
            )
            self.assertTrue((repo_dir / "features/feed/impl/build.gradle.kts").is_file())
            self.assertFalse((repo_dir / "core/video").exists())
            self.assertFalse(
                (repo_dir / "androidApp/src/main/kotlin/xxx/yyy/zzz").exists(),
            )
            self.assertFalse(
                (
                    repo_dir / "core/database/schemas/xxx.yyy.zzz.core.database.AppDatabase"
                ).exists(),
            )

            dev_xcconfig = (repo_dir / "config/dev.xcconfig").read_text(encoding="utf-8")
            prod_xcconfig = (repo_dir / "config/prod.xcconfig").read_text(encoding="utf-8")
            proguard = (repo_dir / "androidApp/proguard-rules.pro").read_text(encoding="utf-8")
            self.assertIn("com.acme.demo", dev_xcconfig)
            self.assertIn("com.acme.demo", prod_xcconfig)
            self.assertIn("com.acme.demo.core.navigation.NavKey", proguard)

            # Confirm placeholder xxx.yyy.zzz is completely gone across source/config trees.
            for tree in ("androidApp", "shared", "core", "features", "config", "iosApp/Sources"):
                for file_path in (repo_dir / tree).rglob("*"):
                    if not file_path.is_file():
                        continue
                    try:
                        content = file_path.read_text(encoding="utf-8")
                    except UnicodeDecodeError:
                        continue
                    self.assertNotIn(
                        "xxx.yyy.zzz",
                        content,
                        f"Placeholder xxx.yyy.zzz leaked in {file_path.relative_to(repo_dir)}",
                    )


if __name__ == "__main__":
    unittest.main()
