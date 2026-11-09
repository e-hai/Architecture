plugins {
    alias(libs.plugins.myproject.kmp.feature.impl)
}

android {
    namespace = "xxx.yyy.zzz.feature.home.impl"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:home:api"))
            implementation(project(":core:abtesting"))
        }
    }
}
