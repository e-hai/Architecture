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
            "PagKit",
            "LogKit-Android",
            "CrashReportKit-Android",
            "AnalyticsKit-Android",
            "AbTestingKit-Android",
            "MmpKit-Android",
            "AdsKit-Android",
            "PayKit-Android",
            "PushKit-Android",
            "VideoKit-Android",
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

// Core Commercial Kits (1:1 Native Passthrough)
include(":core:log")
include(":core:crashreport")
include(":core:analytics")
include(":core:abtesting")
include(":core:mmp")
include(":core:ads")
include(":core:pay")
include(":core:push")
include(":core:video")
include(":core:pag")

// Features
// [FEATURE_MODULES_START]
include(":features:home:api")
include(":features:home:impl")
// [FEATURE_MODULES_END]
