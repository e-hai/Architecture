plugins {
    alias(libs.plugins.shortvideo.android.library)
}

android {
    namespace = "com.shortvideo.app.core.abtesting"
}

dependencies {
    // api：业务直接使用 AbTestingClient
    api(libs.abtesting.kit.firebase)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
