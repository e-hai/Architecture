plugins {
    alias(libs.plugins.myproject.android.feature)
}

android {
    namespace = "xxx.yyy.zzz.feature.settings.impl"
}

dependencies {
    implementation(project(":feature:settings:api"))
}
