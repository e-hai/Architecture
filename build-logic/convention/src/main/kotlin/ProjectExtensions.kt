import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val Project.libsCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.configureKotlinAndroid(commonExtension: Any) {
    when (commonExtension) {
        is com.android.build.api.dsl.ApplicationExtension -> {
            commonExtension.apply {
                compileSdk = libsCatalog.findVersion("compileSdk").get().requiredVersion.toInt()
                defaultConfig.minSdk = libsCatalog.findVersion("minSdk").get().requiredVersion.toInt()
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
        is com.android.build.api.dsl.LibraryExtension -> {
            commonExtension.apply {
                compileSdk = libsCatalog.findVersion("compileSdk").get().requiredVersion.toInt()
                defaultConfig.minSdk = libsCatalog.findVersion("minSdk").get().requiredVersion.toInt()
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-opt-in=kotlin.OptIn"
                )
            )
        }
    }
}
