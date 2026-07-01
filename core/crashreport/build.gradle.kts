plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.koin)
}

android {
    namespace = "xxx.yyy.zzz.core.crashreport"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
}
