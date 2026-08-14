plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "xxx.yyy.zzz.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
}
