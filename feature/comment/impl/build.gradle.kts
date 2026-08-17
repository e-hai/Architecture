plugins {
    alias(libs.plugins.shortvideo.android.feature.impl)
}

android {
    namespace = "com.shortvideo.app.feature.comment.impl"
}

dependencies {
    implementation(project(":feature:comment:api"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.coil.compose)
}
