plugins {
    alias(libs.plugins.shortvideo.android.library)
    alias(libs.plugins.shortvideo.android.room)
    alias(libs.plugins.shortvideo.koin)
}

android {
    namespace = "com.shortvideo.app.core.database"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.datetime)
}
