import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", libsCatalog.findLibrary("koin-core").get())

                pluginManager.withPlugin("com.android.library") {
                    add("implementation", libsCatalog.findLibrary("koin-android").get())
                }
                pluginManager.withPlugin("com.android.application") {
                    add("implementation", libsCatalog.findLibrary("koin-android").get())
                }
                pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
                    add("implementation", libsCatalog.findLibrary("koin-androidx-compose").get())
                }
            }
        }
    }
}
