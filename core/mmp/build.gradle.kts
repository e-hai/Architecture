plugins {
    alias(libs.plugins.myproject.kmp.library)
}

android {
    namespace = "xxx.yyy.zzz.core.mmp"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.mmp.kit.appsflyer)
        }
    }
}
