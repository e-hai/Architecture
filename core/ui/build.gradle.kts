plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.android.compose)
    alias(libs.plugins.myproject.koin)
}

android {
    namespace = "xxx.yyy.zzz.core.ui"
}

dependencies {
}
