plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.myproject.kmp.room)
    alias(libs.plugins.myproject.koin)
}

kotlin {
    android {
        namespace = "xxx.yyy.zzz.core.database"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
        }
    }
}
