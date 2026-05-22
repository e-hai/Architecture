plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "xxx.yyy.zzz.feature.home.api"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.json)
}
