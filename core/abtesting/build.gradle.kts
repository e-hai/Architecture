plugins {
    alias(libs.plugins.myproject.kmp.library)
}

android {
    namespace = "xxx.yyy.zzz.core.abtesting"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.abtesting.kit.firebase)
        }
    }
}
