plugins {
    alias(libs.plugins.shortvideo.android.feature.impl)
}

android {
    namespace = "com.shortvideo.app.feature.creator.impl"
}

dependencies {
    implementation(project(":feature:creator:api"))
    implementation(project(":feature:feed:api"))
    implementation(project(":core:video"))
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:analytics"))
    implementation(project(":core:log"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.coil.compose)
}
