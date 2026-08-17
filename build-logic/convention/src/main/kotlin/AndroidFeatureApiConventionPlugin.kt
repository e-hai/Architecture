import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import project.libsCatalog

/**
 * Feature API 模块的约定插件。
 * 自动应用：Library + kotlinx.serialization。
 * 自动依赖：Navigation3 Runtime + kotlinx-serialization-json。
 */
class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("shortvideo.android.library")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            dependencies {
                // Navigation & Serialization for Feature API modules
                add("implementation", libsCatalog.findLibrary("androidx-navigation3-runtime").get())
                add("implementation", libsCatalog.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}
