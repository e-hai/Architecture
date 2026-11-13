import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import project.configureKotlinAndroid
import project.libsCatalog

/**
 * 独立 Android 宿主应用模块（androidApp）的约定插件。
 * 对应 JetBrains 2026 最新 KMP 工程结构与 AGP 9.0 规范。
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("myproject.spotless")
                apply("myproject.koin")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libsCatalog.findVersion("targetSdk").get().requiredVersion.toInt()

                buildFeatures {
                    buildConfig = true
                    compose = true
                }

                packaging {
                    resources {
                        excludes += listOf(
                            "/META-INF/{AL2.0,LGPL2.1}",
                            "/META-INF/INDEX.LIST",
                            "/META-INF/DEPENDENCIES",
                        )
                    }
                }
            }
        }
    }
}
