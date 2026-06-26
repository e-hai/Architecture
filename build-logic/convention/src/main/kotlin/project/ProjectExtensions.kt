package project

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * 在约定插件中访问 Version Catalog（gradle/libs.versions.toml）的扩展属性。
 * 使用方式：`libsCatalog.findVersion("xxx")` 或 `libsCatalog.findLibrary("xxx")`
 */
val Project.libsCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
