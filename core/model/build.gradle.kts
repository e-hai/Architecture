plugins {
    alias(libs.plugins.shortvideo.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.shortvideo.app.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
}
