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
    val compileSdkMajor = libsCatalog.findVersion("compileSdk").get().requiredVersion.toInt()
    val compileSdkMinor =
        libsCatalog
            .findVersion("compileSdkMinor")
            .get()
            .requiredVersion
            .toInt()
    val minSdkVersion = libsCatalog.findVersion("minSdk").get().requiredVersion.toInt()

    when (commonExtension) {
        is ApplicationExtension -> {
            commonExtension.apply {
                // 从 Version Catalog 读取 SDK 版本（含 minor，满足 AnalyticsKit 等 AAR 元数据）
                compileSdk {
                    version =
                        release(compileSdkMajor) {
                            minorApiLevel = compileSdkMinor
                        }
                }
                defaultConfig.minSdk = minSdkVersion
                compileOptions {
                    // 统一 Java 编译目标为 17
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
        is LibraryExtension -> {
            commonExtension.apply {
                compileSdk {
                    version =
                        release(compileSdkMajor) {
                            minorApiLevel = compileSdkMinor
                        }
                }
                defaultConfig.minSdk = minSdkVersion
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
                ),
            )
        }
    }
}
