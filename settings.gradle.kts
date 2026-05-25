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
    }
}

rootProject.name = "MyProjectScaffold"

// Enable Included Build for convention plugins
// The build-logic will be executed before assembling the project.

// Include application entrypoint
include(":app")

// Include Core Infrastructure Modules
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:navigation")
include(":core:ui")

// Include Feature Modules
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:settings:api")
include(":feature:settings:impl")
