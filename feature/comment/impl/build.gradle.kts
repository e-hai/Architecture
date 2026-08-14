plugins {
    alias(libs.plugins.myproject.android.feature.impl)
}

android {
    namespace = "xxx.yyy.zzz.feature.comment.impl"
}

dependencies {
    implementation(project(":feature:comment:api"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.coil.compose)
}
