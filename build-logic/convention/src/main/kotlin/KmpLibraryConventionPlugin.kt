import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import project.configureKotlinAndroid
import project.libsCatalog

/**
 * 跨平台库模块（shared / core:* / feature:*）的基础约定插件。
 * 配置 Kotlin Multiplatform 多目标体系（Android + iOS arm64 + iOS Simulator arm64）。
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.library")
                apply("myproject.spotless")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget()
                iosArm64()
                iosSimulatorArm64()

                sourceSets.getByName("commonMain").dependencies {
                    implementation(libsCatalog.findLibrary("kotlinx-coroutines-core").get())
                }

                sourceSets.getByName("commonTest").dependencies {
                    implementation(libsCatalog.findLibrary("kotlin-test").get())
                }
            }
        }
    }
}
