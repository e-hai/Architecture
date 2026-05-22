import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val extension = extensions.findByType(ApplicationExtension::class.java)
                ?: extensions.findByType(LibraryExtension::class.java)
                ?: return

            configureAndroidCompose(extension)
        }
    }
}

fun Project.configureAndroidCompose(
    commonExtension: Any,
) {
    val buildFeatures = when (commonExtension) {
        is ApplicationExtension -> commonExtension.buildFeatures
        is LibraryExtension -> commonExtension.buildFeatures
        else -> return
    }

    buildFeatures.compose = true

    dependencies {
        val bom = libsCatalog.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        add("implementation", libsCatalog.findLibrary("androidx-compose-ui").get())
        add("implementation", libsCatalog.findLibrary("androidx-compose-ui-graphics").get())
        add("implementation", libsCatalog.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("implementation", libsCatalog.findLibrary("androidx-compose-material3").get())
        add("implementation", libsCatalog.findLibrary("androidx-compose-foundation").get())
        add("implementation", libsCatalog.findLibrary("androidx-lifecycle-runtime-compose").get())
        add("implementation", libsCatalog.findLibrary("androidx-lifecycle-viewmodel-compose").get())

        add("debugImplementation", libsCatalog.findLibrary("androidx-compose-ui-tooling").get())
        add("debugImplementation", libsCatalog.findLibrary("androidx-compose-ui-test-manifest").get())
    }
}
