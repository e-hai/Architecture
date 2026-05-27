plugins {
    alias(libs.plugins.myproject.android.feature.impl)
}

android {
    namespace = "xxx.yyy.zzz.feature.settings.impl"
}

dependencies {
    implementation(projects.feature.settings.api)
    implementation(projects.feature.home.api)
}
