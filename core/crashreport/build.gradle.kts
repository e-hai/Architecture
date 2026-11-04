plugins {
    alias(libs.plugins.myproject.kmp.library)
}

android {
    namespace = "xxx.yyy.zzz.core.crashreport"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.crashreport.kit)
        }
    }
}
