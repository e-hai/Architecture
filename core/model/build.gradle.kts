plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "xxx.yyy.zzz.core.model"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
    }
}
