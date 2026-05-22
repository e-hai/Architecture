plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.android.room)
    alias(libs.plugins.myproject.koin)
}

android {
    namespace = "xxx.yyy.zzz.core.database"
}

dependencies {
    implementation(project(":core:model"))
}
