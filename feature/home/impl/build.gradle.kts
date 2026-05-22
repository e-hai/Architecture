plugins {
    alias(libs.plugins.myproject.android.feature)
}

android {
    namespace = "xxx.yyy.zzz.feature.home.impl"
}

dependencies {
    implementation(project(":feature:home:api"))
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
}
