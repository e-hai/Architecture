import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Feature Impl 模块约定插件。
 * 业务特性的具体 CMP 界面呈现、ViewModel 与数据装配。
 */
class KmpFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("myproject.kmp.library")
                apply("myproject.kmp.compose")
                apply("myproject.koin")
            }

            extensions.getByType<KotlinMultiplatformExtension>().apply {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(project(":core:ui"))
                    implementation(project(":core:navigation"))
                    implementation(project(":core:model"))
                }
            }
        }
    }
}
