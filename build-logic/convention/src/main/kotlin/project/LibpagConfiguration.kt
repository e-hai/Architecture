package project

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File

/** `libpag` iOS XCFramework 缓存版本号。 */
const val LIBPAG_IOS_VERSION = "4.5.102"

/**
 * 为 `:core:pag` 自身及依赖 `:core:pag` 的下游 KMP 模块统一配置 `libpag.xcframework` 原生链接参数。
 *
 * @param project 当前 Gradle 模块工程
 * @param includeLinkLibraries 是否显式附加 `-framework libpag -lc++`（由 `:core:pag` 声明，下游消费方仅需搜索路径与 rpath）
 * @param ensureTask 提供 `libpag.xcframework` 缓存保障的 Gradle 任务引用
 */
fun KotlinMultiplatformExtension.configureLibpagNativeLinking(
    project: Project,
    includeLinkLibraries: Boolean = false,
    ensureTask: Any = ":core:pag:ensureLibpagXcframework",
) {
    val xcframeworkDir = project.rootProject.file(".gradle/libpag-ios/$LIBPAG_IOS_VERSION/libpag.xcframework")
    targets.withType(KotlinNativeTarget::class.java).configureEach {
        val sliceDir =
            if (name.contains("Simulator", ignoreCase = true)) {
                "ios-arm64_x86_64-simulator"
            } else {
                "ios-arm64"
            }
        val frameworkDir = File(xcframeworkDir, sliceDir)
        val linkArgs =
            buildList {
                add("-F${frameworkDir.absolutePath}")
                if (includeLinkLibraries) {
                    add("-framework")
                    add("libpag")
                    add("-lc++")
                }
                add("-rpath")
                add(frameworkDir.absolutePath)
            }
        binaries.all {
            linkTaskProvider.configure {
                dependsOn(ensureTask)
            }
            linkerOpts(linkArgs)
        }
    }
}
