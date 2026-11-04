import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import project.libsCatalog

/**
 * Feature API 模块约定插件。
 * 强制仅依赖抽象与领域模型，提供导航 Key 与业务能力接口定义。
 */
class KmpFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("myproject.kmp.library")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            extensions.getByType<KotlinMultiplatformExtension>().apply {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(project(":core:model"))
                    implementation(project(":core:navigation"))
                    implementation(libsCatalog.findLibrary("kotlinx-serialization-json").get())
                }
            }
        }
    }
}
