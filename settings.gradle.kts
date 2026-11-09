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
        maven { url = uri("https://jitpack.io") }

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

// Core 8 Commercial Kits (1:1 Native Passthrough)
include(":core:log")
include(":core:crashreport")
include(":core:analytics")
include(":core:abtesting")
include(":core:mmp")
include(":core:ads")
include(":core:pay")
include(":core:push")

// Features
// [FEATURE_MODULES_START]
include(":features:home:api")
include(":features:home:impl")
// [FEATURE_MODULES_END]
