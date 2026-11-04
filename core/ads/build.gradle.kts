plugins {
    alias(libs.plugins.myproject.kmp.library)
}

android {
    namespace = "xxx.yyy.zzz.core.ads"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ads.kit.admob)
        }
    }
}
