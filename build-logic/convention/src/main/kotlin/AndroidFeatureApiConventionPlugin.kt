import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import project.configureKotlinAndroid
import project.libsCatalog

class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("myproject.android.library")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.consumerProguardFiles("consumer-rules.pro")
            }

            dependencies {
                // Navigation & Serialization for Feature API modules
                add("implementation", libsCatalog.findLibrary("androidx-navigation3-runtime").get())
                add("implementation", libsCatalog.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}
