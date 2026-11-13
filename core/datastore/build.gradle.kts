plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.myproject.koin)
}

kotlin {
    android {
        namespace = "xxx.yyy.zzz.core.datastore"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.datastore.preferences.core)
        }
    }
}
