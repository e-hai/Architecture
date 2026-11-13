plugins {
    alias(libs.plugins.myproject.kmp.feature.impl)
}

kotlin {
    android {
        namespace = "xxx.yyy.zzz.feature.debug.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:debug:api"))
            implementation(project(":core:database"))
            implementation(project(":core:datastore"))
            implementation(project(":core:network"))

            // [KIT_DEPENDENCIES_START]
            // [KIT:log:START]
            implementation(project(":core:log"))
            // [KIT:log:END]
            // [KIT:crashreport:START]
            implementation(project(":core:crashreport"))
            // [KIT:crashreport:END]
            // [KIT:analytics:START]
            implementation(project(":core:analytics"))
            // [KIT:analytics:END]
            // [KIT:abtesting:START]
            implementation(project(":core:abtesting"))
            // [KIT:abtesting:END]
            // [KIT:mmp:START]
            implementation(project(":core:mmp"))
            // [KIT:mmp:END]
            // [KIT:ads:START]
            implementation(project(":core:ads"))
            // [KIT:ads:END]
            // [KIT:pay:START]
            implementation(project(":core:pay"))
            // [KIT:pay:END]
            // [KIT:push:START]
            implementation(project(":core:push"))
            // [KIT:push:END]
            // [KIT:video:START]
            implementation(project(":core:video"))
            // [KIT:video:END]
            // [KIT:pag:START]
            implementation(project(":core:pag"))
            // [KIT:pag:END]
            // [KIT_DEPENDENCIES_END]
        }
    }
}
