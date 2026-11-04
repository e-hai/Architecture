plugins {
    alias(libs.plugins.myproject.android.application)
}

android {
    namespace = "xxx.yyy.zzz.android"

    defaultConfig {
        applicationId = "xxx.yyy.zzz"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
