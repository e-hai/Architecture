plugins {
    alias(libs.plugins.shortvideo.android.library)
    alias(libs.plugins.shortvideo.koin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.shortvideo.app.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.lifecycle.viewModel.navigation3)
    implementation(libs.kotlinx.serialization.json)
}
