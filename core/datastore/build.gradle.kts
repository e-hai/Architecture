plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.myproject.koin)
}

android {
    namespace = "xxx.yyy.zzz.core.datastore"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.datastore.preferences.core)
        }
    }
}
