plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.myproject.architecture)
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

// 将 composite build (build-logic) 一并纳入根工程 Spotless 门禁
tasks.named("spotlessCheck") {
    dependsOn(gradle.includedBuild("build-logic").task(":convention:spotlessCheck"))
}

tasks.named("spotlessApply") {
    dependsOn(gradle.includedBuild("build-logic").task(":convention:spotlessApply"))
}
