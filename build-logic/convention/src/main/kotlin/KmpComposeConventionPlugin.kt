import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import project.libsCatalog

/**
 * 跨平台 Compose Multiplatform 约定插件。
 * 为 KMP 模块注入 CMP 基础依赖（Material3, Runtime, Foundation, UI, Lifecycle, Koin Compose）。
 */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            // AGP 9 KMP 库默认关闭 Android 资源处理，启用 Compose Resources 模块需显式开启
            pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
                extensions.configure<KotlinMultiplatformExtension> {
                    (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                        androidResources.enable = true
                    }
                }
            }

            val compose = extensions.getByType<ComposeExtension>().dependencies
            extensions.getByType<KotlinMultiplatformExtension>().apply {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(compose.runtime)
                    implementation(compose.foundation)
                    implementation(compose.material3)
                    implementation(compose.ui)
                    implementation(compose.components.resources)
                    implementation(libsCatalog.findLibrary("androidx-lifecycle-viewmodel").get())
                    implementation(libsCatalog.findLibrary("androidx-lifecycle-runtime-compose").get())
                    implementation(libsCatalog.findLibrary("koin-compose").get())
                    implementation(libsCatalog.findLibrary("koin-compose-viewmodel").get())
                }
            }
        }
    }
}
