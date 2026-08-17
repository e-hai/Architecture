plugins {
    alias(libs.plugins.shortvideo.android.library)
    alias(libs.plugins.shortvideo.koin)
}

android {
    namespace = "com.shortvideo.app.core.datastore"
}

dependencies {
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
}
