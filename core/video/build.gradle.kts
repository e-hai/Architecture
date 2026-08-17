plugins {
    alias(libs.plugins.shortvideo.android.library)
    alias(libs.plugins.shortvideo.android.compose)
}

android {
    namespace = "com.shortvideo.app.core.video"
}

dependencies {
    // api：透出 VideoKit 核心库与 Media3 播放器基础设施
    api(libs.videokit.core)
    api(libs.androidx.media3.exoplayer)
    api(libs.androidx.media3.ui)
    api(libs.androidx.media3.datasource)
    api(libs.androidx.media3.datasource.okhttp)
    api(libs.androidx.media3.common)

    implementation(project(":core:log"))
    implementation(libs.coil.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)
}
