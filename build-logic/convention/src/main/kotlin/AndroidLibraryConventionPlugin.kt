import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import project.configureKotlinAndroid

/**
 * Android Library 模块的基础约定插件。
 * 自动应用：com.android.library + Spotless。
 * 配置 compileSdk / minSdk / consumerProguardFiles。
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("myproject.spotless")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.consumerProguardFiles("consumer-rules.pro")
            }
        }
    }
}
