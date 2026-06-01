plugins {
    alias(libs.plugins.myproject.android.feature.impl)
}

android {
    namespace = "xxx.yyy.zzz.feature.home.impl"
}

dependencies {
    implementation(projects.feature.home.api)
    implementation(projects.core.model)
    implementation(projects.core.data)
}
