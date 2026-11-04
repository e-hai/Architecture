import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import project.libsCatalog

/**
 * Room KMP 数据库约定插件。
 * 配置多平台 Room 编译与 SQLite Bundled 运行时。
 */
class KmpRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("androidx.room")
                apply("com.google.devtools.ksp")
            }

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            extensions.getByType<KotlinMultiplatformExtension>().apply {
                sourceSets.getByName("commonMain").dependencies {
                    implementation(libsCatalog.findLibrary("room-runtime").get())
                    implementation(libsCatalog.findLibrary("sqlite-bundled").get())
                }
            }

            dependencies {
                add("kspAndroid", libsCatalog.findLibrary("room-compiler").get())
                add("kspIosArm64", libsCatalog.findLibrary("room-compiler").get())
                add("kspIosSimulatorArm64", libsCatalog.findLibrary("room-compiler").get())
            }
        }
    }
}
