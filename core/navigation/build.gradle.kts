plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    android {
        namespace = "xxx.yyy.zzz.core.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
