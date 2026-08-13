import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import project.configureKotlinAndroid
import project.libsCatalog

/**
 * App 模块的约定插件。
 * 自动应用：com.android.application + Compose + Spotless + Serialization + Firebase 插件 + Koin。
 * 从 Version Catalog 统一读取 SDK 版本和 targetSdk。
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.serialization")
                apply("com.google.gms.google-services")
                apply("com.google.firebase.crashlytics")
                apply("myproject.android.compose")
                apply("myproject.spotless")
                apply("myproject.koin")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libsCatalog.findVersion("targetSdk").get().requiredVersion.toInt()
                buildFeatures {
                    // AGP 8+ 默认关闭；Application 需 BuildConfig.DEBUG 等字段
                    buildConfig = true
                }
            }
        }
    }
}
