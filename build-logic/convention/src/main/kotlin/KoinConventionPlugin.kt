import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import project.libsCatalog

/**
 * Koin 依赖注入约定插件。
 * 自动识别 Android Application 与 KMP Library 模块，注入对应的 Koin 运行时。
 */
class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val kmpExtension = extensions.findByType<KotlinMultiplatformExtension>()
            if (kmpExtension != null) {
                kmpExtension.sourceSets.getByName("commonMain").dependencies {
                    implementation(libsCatalog.findLibrary("koin-core").get())
                }
            } else {
                dependencies {
                    add("implementation", libsCatalog.findLibrary("koin-core").get())
                    add("implementation", libsCatalog.findLibrary("koin-android").get())
                }
            }
        }
    }
}
