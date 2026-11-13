plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.myproject.kmp.compose)
    alias(libs.plugins.myproject.koin)
}

kotlin {
    android {
        namespace = "xxx.yyy.zzz.shared"
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            binaryOption("bundleId", "xxx.yyy.zzz.shared")
            isStatic = true
            export(project(":core:model"))
            // [KIT_EXPORTS_START]
            // [KIT:log:START]
            export(project(":core:log"))
            // [KIT:log:END]
            // [KIT:crashreport:START]
            export(project(":core:crashreport"))
            // [KIT:crashreport:END]
            // [KIT:analytics:START]
            export(project(":core:analytics"))
            // [KIT:analytics:END]
            // [KIT:abtesting:START]
            export(project(":core:abtesting"))
            // [KIT:abtesting:END]
            // [KIT:mmp:START]
            export(project(":core:mmp"))
            // [KIT:mmp:END]
            // [KIT:ads:START]
            export(project(":core:ads"))
            // [KIT:ads:END]
            // [KIT:pay:START]
            export(project(":core:pay"))
            // [KIT:pay:END]
            // [KIT:push:START]
            export(project(":core:push"))
            // [KIT:push:END]
            // [KIT:video:START]
            export(project(":core:video"))
            // [KIT:video:END]
            // [KIT:pag:START]
            export(project(":core:pag"))
            // [KIT:pag:END]
            // [KIT_EXPORTS_END]
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

            // [KIT_APIS_START]
            // [KIT:log:START]
            api(project(":core:log"))
            // [KIT:log:END]
            // [KIT:crashreport:START]
            api(project(":core:crashreport"))
            // [KIT:crashreport:END]
            // [KIT:analytics:START]
            api(project(":core:analytics"))
            // [KIT:analytics:END]
            // [KIT:abtesting:START]
            api(project(":core:abtesting"))
            // [KIT:abtesting:END]
            // [KIT:mmp:START]
            api(project(":core:mmp"))
            // [KIT:mmp:END]
            // [KIT:ads:START]
            api(project(":core:ads"))
            // [KIT:ads:END]
            // [KIT:pay:START]
            api(project(":core:pay"))
            // [KIT:pay:END]
            // [KIT:push:START]
            api(project(":core:push"))
            // [KIT:push:END]
            // [KIT:video:START]
            api(project(":core:video"))
            // [KIT:video:END]
            // [KIT:pag:START]
            api(project(":core:pag"))
            // [KIT:pag:END]
            // [KIT_APIS_END]

            // [FEATURE_DEPENDENCIES_START]
            // [FEATURE:home:START]
            implementation(project(":features:home:api"))
            implementation(project(":features:home:impl"))
            // [FEATURE:home:END]
            // [FEATURE:debug:START]
            implementation(project(":features:debug:api"))
            implementation(project(":features:debug:impl"))
            // [FEATURE:debug:END]
            // [FEATURE_DEPENDENCIES_END]
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
