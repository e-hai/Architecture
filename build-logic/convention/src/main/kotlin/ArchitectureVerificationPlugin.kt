import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * 工程架构红线自动化校验插件（L5 门禁）。
 * 挂载于根工程，提供 `./gradlew verifyArchitecture` 任务，强制守护 AGENTS.md 中的五项核心架构边界。
 */
class ArchitectureVerificationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("verifyArchitecture") {
            group = "verification"
            description = "校验 KMP 三级模块化隔离、9 大原生委托 Kit 纯 commonMain 约束、Fake over Mock 与 AGP 9.0 规范。"

            val rootDir = target.rootDir
            doLast {
                val violations = mutableListOf<String>()
                checkPureCommonDelegateKits(rootDir, violations)
                checkNativeSdkIsolation(rootDir, violations)
                checkModuleDependencyDirection(rootDir, violations)
                checkFakeOverMock(rootDir, violations)
                checkAgp9Compliance(rootDir, violations)

                if (violations.isNotEmpty()) {
                    val report = violations.joinToString(separator = "\n") { "  - $it" }
                    throw GradleException(
                        "架构红线校验失败（发现 ${violations.size} 处违规）：\n$report",
                    )
                }
                logger.lifecycle("[verifyArchitecture] 全部 5 项架构红线校验通过。")
            }
        }
    }

    private fun checkPureCommonDelegateKits(
        rootDir: File,
        violations: MutableList<String>,
    ) {
        val delegateKits =
            listOf("log", "crashreport", "analytics", "abtesting", "mmp", "ads", "pay", "push", "video")
        delegateKits.forEach { kit ->
            val kitDir = File(rootDir, "core/$kit")
            if (!kitDir.isDirectory) return@forEach
            listOf("androidMain", "iosMain").forEach { forbiddenSourceSet ->
                val srcDir = File(kitDir, "src/$forbiddenSourceSet")
                if (srcDir.exists()) {
                    violations.add(
                        "[范式 A 违规] :core:$kit 必须为 100% 纯 commonMain 契约模块，禁止存在 src/$forbiddenSourceSet 目录",
                    )
                }
            }
        }
    }

    private fun checkNativeSdkIsolation(
        rootDir: File,
        violations: MutableList<String>,
    ) {
        val scanRoots = mutableListOf(File(rootDir, "shared/src"), File(rootDir, "features"))
        File(rootDir, "core")
            .listFiles { file -> file.isDirectory && file.name != "pag" }
            ?.forEach { scanRoots.add(File(it, "src")) }

        val forbiddenImportPrefixes =
            listOf(
                "import io.github.pagkit.",
                "import com.kit.",
                "import com.google.android.gms.ads.",
                "import com.appsflyer.",
                "import com.google.firebase.",
                "import org.libpag.",
            )

        scanKotlinFiles(rootDir, scanRoots) { file, lineNumber, line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("import ")) {
                val matched = forbiddenImportPrefixes.firstOrNull { trimmed.startsWith(it) }
                if (matched != null) {
                    val relPath = file.relativeTo(rootDir).path
                    violations.add(
                        "[原生 SDK 泄露] $relPath:$lineNumber 禁止在宿主 delegates 之外直接导入原生包 ($matched)",
                    )
                }
            }
        }
    }

    private fun checkModuleDependencyDirection(
        rootDir: File,
        violations: MutableList<String>,
    ) {
        File(rootDir, "core")
            .listFiles { file -> file.isDirectory }
            ?.map { File(it, "build.gradle.kts") }
            ?.filter { it.isFile }
            ?.forEach { buildFile ->
                scanNonCommentLines(buildFile) { lineNumber, line ->
                    if (FORBIDDEN_CORE_DEP_REGEX.containsMatchIn(line)) {
                        val rel = buildFile.relativeTo(rootDir).path
                        violations.add("[反向依赖违规] $rel:$lineNumber :core 模块严禁依赖 :features / :shared / :androidApp")
                    }
                }
            }

        File(rootDir, "features")
            .listFiles { file -> file.isDirectory }
            ?.forEach { featureDir ->
                val featureName = featureDir.name
                val apiBuild = File(featureDir, "api/build.gradle.kts")
                if (apiBuild.isFile) {
                    scanNonCommentLines(apiBuild) { lineNumber, line ->
                        if (FEATURE_IMPL_DEP_REGEX.containsMatchIn(line)) {
                            val rel = apiBuild.relativeTo(rootDir).path
                            violations.add("[API 隔离违规] $rel:$lineNumber :api 模块严禁依赖任何 :impl 模块")
                        }
                    }
                }

                val implBuild = File(featureDir, "impl/build.gradle.kts")
                if (implBuild.isFile) {
                    scanNonCommentLines(implBuild) { lineNumber, line ->
                        val match = OTHER_FEATURE_IMPL_REGEX.find(line)
                        if (match != null) {
                            val targetFeature = match.groupValues[1].ifEmpty { match.groupValues[2] }
                            if (targetFeature != featureName) {
                                val rel = implBuild.relativeTo(rootDir).path
                                violations.add(
                                    "[Feature 越界违规] $rel:$lineNumber 禁止依赖其他特性的 impl ($targetFeature)，仅允许依赖对应 api",
                                )
                            }
                        }
                    }
                }
            }
    }

    private fun checkFakeOverMock(
        rootDir: File,
        violations: MutableList<String>,
    ) {
        val candidateFiles = mutableListOf<File>()
        File(rootDir, "gradle/libs.versions.toml").takeIf { it.isFile }?.let(candidateFiles::add)
        rootDir
            .walkTopDown()
            .onEnter { dir -> dir.name !in IGNORED_DIRS }
            .filter { file ->
                file.isFile &&
                    (file.name.endsWith(".gradle.kts") || file.name.endsWith(".kt"))
            }.forEach(candidateFiles::add)

        candidateFiles.forEach { file ->
            scanNonCommentLines(file) { lineNumber, line ->
                val isImportOrBuildConfig = !file.name.endsWith(".kt") || line.startsWith("import ")
                if (isImportOrBuildConfig && FORBIDDEN_MOCK_REGEX.containsMatchIn(line)) {
                    val rel = file.relativeTo(rootDir).path
                    violations.add("[Fake over Mock 违规] $rel:$lineNumber 严禁引入动态 Mock 框架 (mockk/mockito/powermock)")
                }
            }
        }
    }

    private fun checkAgp9Compliance(
        rootDir: File,
        violations: MutableList<String>,
    ) {
        val gradleProps = File(rootDir, "gradle.properties")
        if (gradleProps.isFile) {
            gradleProps.readLines().forEachIndexed { idx, raw ->
                val line = raw.trim()
                if (!line.startsWith("#") &&
                    (line.contains("android.builtInKotlin=false") || line.contains("android.newDsl=false"))
                ) {
                    violations.add("[AGP 9.0 规范违规] gradle.properties:${idx + 1} 严禁开启降级开关 ($line)")
                }
            }
        }

        rootDir
            .walkTopDown()
            .onEnter { dir -> dir.name !in IGNORED_DIRS }
            .filter { it.isFile && it.name.endsWith(".gradle.kts") }
            .forEach { buildFile ->
                scanNonCommentLines(buildFile) { lineNumber, line ->
                    if (FORBIDDEN_AGP_LEGACY_REGEX.containsMatchIn(line)) {
                        val rel = buildFile.relativeTo(rootDir).path
                        violations.add(
                            "[AGP 9.0 规范违规] $rel:$lineNumber 严禁使用旧插件 com.android.library / org.jetbrains.kotlin.android",
                        )
                    }
                }
            }
    }

    private fun scanKotlinFiles(
        rootDir: File,
        roots: List<File>,
        onLine: (file: File, lineNumber: Int, line: String) -> Unit,
    ) {
        roots
            .filter { it.exists() }
            .forEach { root ->
                root
                    .walkTopDown()
                    .onEnter { dir -> dir.name !in IGNORED_DIRS }
                    .filter { it.isFile && it.name.endsWith(".kt") && it.parentFile != rootDir }
                    .forEach { file ->
                        file.readLines().forEachIndexed { idx, line ->
                            onLine(file, idx + 1, line)
                        }
                    }
            }
    }

    private fun scanNonCommentLines(
        file: File,
        onLine: (lineNumber: Int, line: String) -> Unit,
    ) {
        file.readLines().forEachIndexed { idx, raw ->
            val trimmed = raw.trim()
            if (trimmed.isNotEmpty() &&
                !trimmed.startsWith("//") &&
                !trimmed.startsWith("#") &&
                !trimmed.startsWith("*") &&
                !trimmed.startsWith("/**")
            ) {
                onLine(idx + 1, trimmed)
            }
        }
    }

    private companion object {
        val IGNORED_DIRS = setOf(".git", ".gradle", ".build", ".kotlin", "build", "build-logic")
        val FORBIDDEN_CORE_DEP_REGEX =
            Regex("""projects\.(features|shared)\b|project\(":(features|shared|androidApp)""")
        val FEATURE_IMPL_DEP_REGEX =
            Regex("""projects\.features\.[a-zA-Z0-9_]+\.impl\b|project\(":features:[^"]+:impl"\)""")
        val OTHER_FEATURE_IMPL_REGEX =
            Regex("""projects\.features\.([a-zA-Z0-9_]+)\.impl\b|project\(":features:([^":]+):impl"\)""")
        val FORBIDDEN_MOCK_REGEX =
            Regex("""\b(io\.mockk|org\.mockito|org\.powermock|mockk|mockito)\b""")
        val FORBIDDEN_AGP_LEGACY_REGEX =
            Regex("""com\.android\.library|org\.jetbrains\.kotlin\.android""")
    }
}
