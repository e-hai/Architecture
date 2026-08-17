plugins {
    alias(libs.plugins.shortvideo.android.library)
    alias(libs.plugins.shortvideo.koin)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.shortvideo.app.core.network"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
}
