import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import project.libsCatalog

/**
 * Room 数据库约定插件。
 * 自动应用：androidx.room + KSP。
 * 自动依赖：room-runtime、room-ktx，room-compiler 通过 KSP 引入。
 * 配置 Schema 导出目录为 $projectDir/schemas。
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("androidx.room")
                apply("com.google.devtools.ksp")
            }

            extensions.configure<RoomExtension> {
                // Schema 导出用于数据迁移测试和版本管理
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                add("implementation", libsCatalog.findLibrary("room-runtime").get())
                add("implementation", libsCatalog.findLibrary("room-ktx").get())
                add("ksp", libsCatalog.findLibrary("room-compiler").get())
            }
        }
    }
}
