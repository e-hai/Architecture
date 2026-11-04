plugins {
    alias(libs.plugins.myproject.kmp.library)
}

android {
    namespace = "xxx.yyy.zzz.core.push"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.push.kit)
        }
    }
}
