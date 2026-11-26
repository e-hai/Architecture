import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import project.configureDetekt
import project.configureKotlinAndroid
import project.configureLibpagNativeLinking
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
                apply("com.android.kotlin.multiplatform.library")
                apply("myproject.spotless")
            }

            configureDetekt()

            extensions.configure<KotlinMultiplatformExtension> {
                // 使用 AGP 9.0 原生 KMP 库扩展配置单变体 Android Target，并开启 JVM 单元测试与 Robolectric 资源支持
                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                    configureKotlinAndroid(this)
                    withHostTest {
                        isIncludeAndroidResources = true
                    }
                }
                iosArm64()
                iosSimulatorArm64()

                // 若启用了 :core:pag，自动为下游 iOS Test/Framework 链接提供 libpag.xcframework 搜索路径，
                // 确保任意引用 :core:pag 的 Feature/Shared 模块在运行 allTests 时无需重复声明 linkerOpts
                if (rootProject.findProject(":core:pag") != null && path != ":core:pag") {
                    configureLibpagNativeLinking(project = this@with, includeLinkLibraries = false)
                }

                sourceSets.getByName("commonMain").dependencies {
                    implementation(libsCatalog.findLibrary("kotlinx-coroutines-core").get())
                }

                sourceSets.getByName("commonTest").dependencies {
                    implementation(libsCatalog.findLibrary("kotlin-test").get())
                    implementation(libsCatalog.findLibrary("kotlinx-coroutines-test").get())
                    implementation(libsCatalog.findLibrary("turbine").get())
                }
            }
        }
    }
}
