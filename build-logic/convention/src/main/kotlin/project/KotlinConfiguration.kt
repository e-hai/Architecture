package project

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.dsl.Lint
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * 统一配置 Android 与 KMP Android 库的严格 Lint 策略（复用 `config/lint/lint.xml` 并开启 `warningsAsErrors`）。
 */
private fun Project.configureAndroidLint(
    lint: Lint,
    checkDependencies: Boolean,
) {
    lint.apply {
        this.checkDependencies = checkDependencies
        abortOnError = true
        warningsAsErrors = true
        lintConfig = rootProject.file("config/lint/lint.xml")
    }
}

/**
 * 统一配置 Android compileSdk / minSdk / Java 兼容性 / Lint / Kotlin 编译器选项。
 */
fun Project.configureKotlinAndroid(commonExtension: Any) {
    val compileSdkMajor =
        libsCatalog
            .findVersion("compileSdk")
            .get()
            .requiredVersion
            .toInt()
    val compileSdkMinor =
        libsCatalog
            .findVersion("compileSdkMinor")
            .get()
            .requiredVersion
            .toInt()
    val minSdkVersion =
        libsCatalog
            .findVersion("minSdk")
            .get()
            .requiredVersion
            .toInt()

    when (commonExtension) {
        is ApplicationExtension -> {
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
                configureAndroidLint(lint, checkDependencies = true)
            }
        }

        is KotlinMultiplatformAndroidLibraryTarget -> {
            commonExtension.apply {
                compileSdk {
                    version =
                        release(compileSdkMajor) {
                            minorApiLevel = compileSdkMinor
                        }
                }
                minSdk = minSdkVersion
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
                configureAndroidLint(lint, checkDependencies = false)
            }
        }
    }

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
