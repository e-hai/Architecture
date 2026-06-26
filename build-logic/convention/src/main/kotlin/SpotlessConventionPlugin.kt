import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Spotless 代码格式化约定插件。
 * - Kotlin 文件：使用 ktlint 格式化
 * - Gradle Kotlin DSL：使用 ktlint 格式化
 * - 排除 build/ 目录
 *
 * 运行格式化：./gradlew spotlessApply
 * 格式检查（CI 门禁）：./gradlew spotlessCheck
 */
class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target("**/*.kt")
                    targetExclude("**/build/**/*.kt")
                    ktlint()
                }
                kotlinGradle {
                    target("*.gradle.kts")
                    ktlint()
                }
            }
        }
    }
}
