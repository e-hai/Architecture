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
            export(project(":core:model"))
            export(project(":core:log"))
            export(project(":core:crashreport"))
            export(project(":core:analytics"))
            export(project(":core:abtesting"))
            export(project(":core:mmp"))
            export(project(":core:ads"))
            export(project(":core:pay"))
            export(project(":core:push"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:navigation"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:datastore"))
            implementation(project(":core:ui"))
            api(project(":core:log"))
            api(project(":core:crashreport"))
            api(project(":core:analytics"))
            api(project(":core:abtesting"))
            api(project(":core:mmp"))
            api(project(":core:ads"))
            api(project(":core:pay"))
            api(project(":core:push"))
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
