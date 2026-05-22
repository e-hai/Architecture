plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.koin)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "xxx.yyy.zzz.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)
}
