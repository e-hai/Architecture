plugins {
    alias(libs.plugins.myproject.android.feature.impl)
}

android {
    namespace = "xxx.yyy.zzz.feature.feed.impl"
}

dependencies {
    implementation(project(":feature:feed:api"))
    implementation(project(":feature:comment:api"))
    implementation(project(":core:video"))
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:analytics"))
    implementation(project(":core:log"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.coil.compose)
}
