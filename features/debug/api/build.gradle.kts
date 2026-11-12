plugins {
    alias(libs.plugins.myproject.kmp.feature.api)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

android {
    namespace = "xxx.yyy.zzz.feature.debug.api"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
        }
    }
}
