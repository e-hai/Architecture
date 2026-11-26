#!/usr/bin/env python3
"""Unit tests for `scripts/hooks/compile_kmp_framework.sh` fingerprint fast-path."""

from __future__ import annotations

import os
import pathlib
import stat
import subprocess
import tempfile
import unittest

REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent.parent
COMPILE_SCRIPT = REPO_ROOT / "scripts" / "hooks" / "compile_kmp_framework.sh"


def setup_fake_repo(tmp_path: pathlib.Path) -> tuple[pathlib.Path, pathlib.Path, pathlib.Path]:
    """Create a minimal sandboxed KMP repo structure with a recording stub gradlew."""
    repo = tmp_path / "repo"
    (repo / "shared" / "src" / "commonMain" / "kotlin").mkdir(parents=True)
    (repo / "features" / "home" / "impl").mkdir(parents=True)
    (repo / "core" / "ui").mkdir(parents=True)
    (repo / "gradle").mkdir(parents=True)
    (repo / "build-logic").mkdir(parents=True)

    (repo / "build.gradle.kts").write_text("// root\n", encoding="utf-8")
    (repo / "settings.gradle.kts").write_text("// settings\n", encoding="utf-8")
    (repo / "gradle.properties").write_text("org.gradle.parallel=true\n", encoding="utf-8")
    kt_file = repo / "shared" / "src" / "commonMain" / "kotlin" / "Sample.kt"
    kt_file.write_text("class Sample\n", encoding="utf-8")

    invocations_log = tmp_path / "gradlew_calls.log"
    gradlew = repo / "gradlew"
    gradlew.write_text(
        "#!/usr/bin/env bash\n"
        f'printf "%s\\n" "$*" >> "{invocations_log}"\n'
        'cfg="${EXPECTED_CONFIG:-Debug}"\n'
        'sdk="${EXPECTED_SDK:-iphonesimulator}"\n'
        f'out="{repo}/shared/build/xcode-frameworks/${{cfg}}/${{sdk}}/Shared.framework/Shared"\n'
        'mkdir -p "$(dirname "$out")"\n'
        'printf "binary" > "$out"\n',
        encoding="utf-8",
    )
    gradlew.chmod(gradlew.stat().st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)
    return repo, kt_file, invocations_log


def run_compile_hook(
    repo: pathlib.Path,
    configuration: str = "Debug",
    sdk_name: str = "iphonesimulator",
    force: str = "0",
) -> subprocess.CompletedProcess[str]:
    """Execute compile_kmp_framework.sh against the fake sandbox repo."""
    env = {
        "PATH": os.environ.get("PATH", "/usr/bin:/bin"),
        "REPO_ROOT_OVERRIDE": str(repo),
        "CONFIGURATION": configuration,
        "SDK_NAME": sdk_name,
        "ARCHS": "arm64",
        "FORCE_KMP_BUILD": force,
        "GRADLEW_BIN": str(repo / "gradlew"),
        "EXPECTED_CONFIG": configuration,
        "EXPECTED_SDK": sdk_name,
    }
    return subprocess.run(
        ["bash", str(COMPILE_SCRIPT)],
        env=env,
        capture_output=True,
        text=True,
        check=False,
    )


class CompileKmpFrameworkHookTest(unittest.TestCase):
    """Covers cold build, fingerprint hit skip, Kotlin edit invalidation, Release flag, and force override."""

    def test_fingerprint_skips_unchanged_and_rebuilds_on_kt_or_config_change(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            repo, kt_file, log_file = setup_fake_repo(pathlib.Path(tmp))

            first = run_compile_hook(repo, configuration="Debug")
            self.assertEqual(first.returncode, 0, msg=first.stderr)
            calls = log_file.read_text(encoding="utf-8").splitlines()
            self.assertEqual(len(calls), 1)
            self.assertIn("-PenableDebugPanel=true", calls[0])

            second = run_compile_hook(repo, configuration="Debug")
            self.assertEqual(second.returncode, 0, msg=second.stderr)
            self.assertIn("跳过 Gradle 编排", second.stdout)
            self.assertEqual(len(log_file.read_text(encoding="utf-8").splitlines()), 1)

            kt_file.write_text("class Sample { fun ping() = 1 }\n", encoding="utf-8")
            third = run_compile_hook(repo, configuration="Debug")
            self.assertEqual(third.returncode, 0, msg=third.stderr)
            self.assertEqual(len(log_file.read_text(encoding="utf-8").splitlines()), 2)

            release_res = run_compile_hook(repo, configuration="Release")
            self.assertEqual(release_res.returncode, 0, msg=release_res.stderr)
            calls = log_file.read_text(encoding="utf-8").splitlines()
            self.assertEqual(len(calls), 3)
            self.assertIn("-PenableDebugPanel=false", calls[2])

            forced = run_compile_hook(repo, configuration="Debug", force="1")
            self.assertEqual(forced.returncode, 0, msg=forced.stderr)
            self.assertEqual(len(log_file.read_text(encoding="utf-8").splitlines()), 4)


if __name__ == "__main__":
    unittest.main()
