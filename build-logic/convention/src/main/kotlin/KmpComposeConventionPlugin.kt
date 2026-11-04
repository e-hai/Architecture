import org.gradle.api.Plugin
import org.gradle.api.Project
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
