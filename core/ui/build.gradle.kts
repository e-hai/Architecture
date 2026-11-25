plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.myproject.kmp.compose)
}

kotlin {
    android {
        namespace = "xxx.yyy.zzz.core.ui"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.collections.immutable)
        }
    }
}

compose.resources {
    packageOfResClass = "xxx.yyy.zzz.core.ui.generated.resources"
    publicResClass = true
}
