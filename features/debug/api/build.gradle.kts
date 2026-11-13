plugins {
    alias(libs.plugins.myproject.kmp.feature.api)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    android {
        namespace = "xxx.yyy.zzz.feature.debug.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
        }
    }
}
