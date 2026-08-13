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
        // AdsKit / AppLovin mediation（若改用全量 AdsKit 或 AdsKit-applovin 时需要）
        maven { url = uri("https://artifacts.applovin.com/android") }
        maven { url = uri("https://artifactory.bidmachine.io/bidmachine") }
        maven { url = uri("https://maven.ogury.co") }
        maven { url = uri("https://s3.amazonaws.com/smaato-sdk-releases/") }
        maven { url = uri("https://verve.jfrog.io/artifactory/verve-gradle-release") }
    }
}

rootProject.name = "Architecture"

// Enable Included Build for convention plugins
// The build-logic will be executed before assembling the project.

// Include application entrypoint
include(":app")

// Include Core Infrastructure Modules
include(":core:model")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:navigation")
include(":core:ui")
include(":core:analytics")
include(":core:abtesting")
include(":core:crashreport")
include(":core:mmp")
include(":core:ads")
include(":core:pay")
include(":core:push")
include(":core:log")

// Include Feature Modules
// 开发真实项目时，请在此添加 feature 模块：
// include(":feature:xxx:api")
// include(":feature:xxx:impl")
