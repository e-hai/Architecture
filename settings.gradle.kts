enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        val gprUser = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR") ?: "e-hai"
        val gprKey = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN") ?: ""

        // e-hai 官方 SDK 仓库群 (GitHub Packages)
        listOf(
            // [KIT_REPOS_START]
            // [KIT:pag:START]
            "PagKit",
            // [KIT:pag:END]
            // [KIT:log:START]
            "LogKit-Android",
            // [KIT:log:END]
            // [KIT:crashreport:START]
            "CrashReportKit-Android",
            // [KIT:crashreport:END]
            // [KIT:analytics:START]
            "AnalyticsKit-Android",
            // [KIT:analytics:END]
            // [KIT:abtesting:START]
            "AbTestingKit-Android",
            // [KIT:abtesting:END]
            // [KIT:mmp:START]
            "MmpKit-Android",
            // [KIT:mmp:END]
            // [KIT:ads:START]
            "AdsKit-Android",
            // [KIT:ads:END]
            // [KIT:pay:START]
            "PayKit-Android",
            // [KIT:pay:END]
            // [KIT:push:START]
            "PushKit-Android",
            // [KIT:push:END]
            // [KIT:video:START]
            "VideoKit-Android",
            // [KIT:video:END]
            // [KIT_REPOS_END]
        ).forEach { repoName ->
            maven {
                name = "GitHubPackages-$repoName"
                url = uri("https://maven.pkg.github.com/e-hai/$repoName")
                credentials {
                    username = gprUser
                    password = gprKey
                }
            }
        }

        // AdsKit / AdMob mediation
        maven { url = uri("https://imobile.github.io/adnw-sdk-android") }
        maven { url = uri("https://android-sdk.is.com/") }
        maven { url = uri("https://imobile-maio.github.io/maven") }
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
        maven { url = uri("https://artifact.bytedance.com/repository/pangle/") }
        maven { url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/") }
        // AdsKit / AppLovin mediation
        maven { url = uri("https://artifacts.applovin.com/android") }
        maven { url = uri("https://artifactory.bidmachine.io/bidmachine") }
        maven { url = uri("https://maven.ogury.co") }
        maven { url = uri("https://s3.amazonaws.com/smaato-sdk-releases/") }
        maven { url = uri("https://verve.jfrog.io/artifactory/verve-gradle-release") }
    }
}

rootProject.name = "Architecture"

// JetBrains 2026 New Default Structure: Platform Launchers & Shared Root
include(":androidApp")
include(":shared")

// Core Foundation & Data
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:navigation")
include(":core:ui")

// Core Infrastructure & Commercial Kits
// [KIT_MODULES_START]
// [KIT:log:START]
include(":core:log")
// [KIT:log:END]
// [KIT:crashreport:START]
include(":core:crashreport")
// [KIT:crashreport:END]
// [KIT:analytics:START]
include(":core:analytics")
// [KIT:analytics:END]
// [KIT:abtesting:START]
include(":core:abtesting")
// [KIT:abtesting:END]
// [KIT:mmp:START]
include(":core:mmp")
// [KIT:mmp:END]
// [KIT:ads:START]
include(":core:ads")
// [KIT:ads:END]
// [KIT:pay:START]
include(":core:pay")
// [KIT:pay:END]
// [KIT:push:START]
include(":core:push")
// [KIT:push:END]
// [KIT:video:START]
include(":core:video")
// [KIT:video:END]
// [KIT:pag:START]
include(":core:pag")
// [KIT:pag:END]
// [KIT_MODULES_END]

// Features
// [FEATURE_MODULES_START]
// [FEATURE:home:START]
include(":features:home:api")
include(":features:home:impl")
// [FEATURE:home:END]
// [FEATURE:debug:START]
include(":features:debug:api")
include(":features:debug:impl")
// [FEATURE:debug:END]
// [FEATURE_MODULES_END]
