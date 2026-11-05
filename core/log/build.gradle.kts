plugins {
    alias(libs.plugins.myproject.kmp.library)
}

android {
    namespace = "xxx.yyy.zzz.core.log"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.log.kit)
        }
    }
}
