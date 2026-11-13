plugins {
    alias(libs.plugins.myproject.kmp.feature.impl)
}

kotlin {
    android {
        namespace = "xxx.yyy.zzz.feature.home.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:home:api"))
            implementation(project(":core:abtesting"))
        }
    }
}
