plugins {
    alias(libs.plugins.shortvideo.android.feature.impl)
}

android {
    namespace = "com.shortvideo.app.feature.profile.impl"
}

dependencies {
    implementation(project(":feature:profile:api"))
    implementation(project(":feature:feed:api"))
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:analytics"))
    implementation(project(":core:log"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.coil.compose)
}
