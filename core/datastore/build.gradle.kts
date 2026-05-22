plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.koin)
}

android {
    namespace = "xxx.yyy.zzz.core.datastore"
}

dependencies {
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
}
