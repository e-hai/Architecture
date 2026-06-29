import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Feature Impl 模块的约定插件。
 * 自动应用：Library + Compose + Koin。
 * 自动依赖：core:ui（主题/公共组件）+ core:navigation（导航基础设施）。
 */
class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("myproject.android.library")
                apply("myproject.android.compose")
                apply("myproject.koin")
            }

            dependencies {
                // Global Navigation & UI Design System
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:navigation"))
            }
        }
    }
}
