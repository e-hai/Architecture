import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import project.libsCatalog

/**
 * Koin 依赖注入约定插件。
 * 智能添加依赖：自动检测模块类型（Library / Application）和是否启用 Compose。
 * - 基础：koin-core（所有模块）
 * - Android：koin-android（Library / Application 模块）
 * - Compose：koin-androidx-compose（已应用 Compose 插件的模块）
 */
class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", libsCatalog.findLibrary("koin-core").get())

                pluginManager.withPlugin("com.android.library") {
                    add("implementation", libsCatalog.findLibrary("koin-android").get())
                }
                pluginManager.withPlugin("com.android.application") {
                    add("implementation", libsCatalog.findLibrary("koin-android").get())
                }
                pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
                    add("implementation", libsCatalog.findLibrary("koin-androidx-compose").get())
                }
            }
        }
    }
}
