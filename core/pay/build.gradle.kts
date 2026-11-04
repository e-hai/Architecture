plugins {
    alias(libs.plugins.myproject.kmp.library)
}

android {
    namespace = "xxx.yyy.zzz.core.pay"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.pay.kit)
        }
    }
}
