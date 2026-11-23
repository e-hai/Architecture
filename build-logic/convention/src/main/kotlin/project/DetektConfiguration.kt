package project

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.extensions.FailOnSeverity
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * 统一配置 Detekt 静态代码质量与 Compose Rules 门禁。
 * 与 Spotless/ktlint 分工：Spotless 负责排版格式化，Detekt 负责复杂度、潜在缺陷与 Compose 规范。
 */
fun Project.configureDetekt() {
    pluginManager.apply("dev.detekt")

    extensions.configure<DetektExtension> {
        source.setFrom(files("src"))
        buildUponDefaultConfig.set(true)
        parallel.set(true)
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        ignoreFailures.set(false)
        failOnSeverity.set(FailOnSeverity.Warning)
    }

    dependencies {
        add("detektPlugins", libsCatalog.findLibrary("detekt-compose").get())
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget.set("17")
        reports {
            sarif.required.set(true)
            checkstyle.required.set(false)
            html.required.set(false)
            markdown.required.set(false)
        }
    }
}
