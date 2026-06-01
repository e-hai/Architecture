plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.koin)
}

android {
    namespace = "xxx.yyy.zzz.core.analytics"
}

dependencies {
    // Firebase BOM for version management
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
}
