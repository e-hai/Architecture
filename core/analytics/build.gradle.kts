plugins {
    alias(libs.plugins.myproject.kmp.library)
}

android {
    namespace = "xxx.yyy.zzz.core.analytics"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.analytics.kit.firebase)
        }
    }
}
