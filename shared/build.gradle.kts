plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.myproject.kmp.compose)
    alias(libs.plugins.myproject.koin)
}

android {
    namespace = "xxx.yyy.zzz.shared"
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            binaryOption("bundleId", "xxx.yyy.zzz.shared")
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:navigation"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:datastore"))
            implementation(project(":core:ui"))
            implementation(project(":core:log"))
            implementation(project(":core:crashreport"))
            implementation(project(":core:analytics"))
            implementation(project(":core:abtesting"))
            implementation(project(":core:mmp"))
            implementation(project(":core:ads"))
            implementation(project(":core:pay"))
            implementation(project(":core:push"))
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
