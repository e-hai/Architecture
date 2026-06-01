import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import project.configureKotlinAndroid

class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("myproject.android.library")
                apply("myproject.android.compose")
                apply("myproject.koin")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.consumerProguardFiles("consumer-rules.pro")
            }

            dependencies {
                // Global Navigation & UI Design System
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:navigation"))
            }
        }
    }
}
