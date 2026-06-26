package project

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * 统一配置 Android 模块的 compileSdk / minSdk / Java 兼容性 / Kotlin 编译器选项。
 *
 * @param commonExtension 模块的扩展类型，支持 [ApplicationExtension] 和 [LibraryExtension]
 */
fun Project.configureKotlinAndroid(commonExtension: Any) {
    when (commonExtension) {
        is ApplicationExtension -> {
            commonExtension.apply {
                // 从 Version Catalog 读取 SDK 版本
                compileSdk = libsCatalog.findVersion("compileSdk").get().requiredVersion.toInt()
                defaultConfig.minSdk = libsCatalog.findVersion("minSdk").get().requiredVersion.toInt()
                compileOptions {
                    // 统一 Java 编译目标为 17
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
        is LibraryExtension -> {
            commonExtension.apply {
                compileSdk = libsCatalog.findVersion("compileSdk").get().requiredVersion.toInt()
                defaultConfig.minSdk = libsCatalog.findVersion("minSdk").get().requiredVersion.toInt()
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
    }

    // 统一 Kotlin JVM target 和编译器 opt-in 配置
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-opt-in=kotlin.OptIn"
                )
            )
        }
    }
}
