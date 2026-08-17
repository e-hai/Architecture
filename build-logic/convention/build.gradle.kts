plugins {
    `kotlin-dsl`
}

group = "com.shortvideo.app.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
    compileOnly(libs.google.services.gradlePlugin)
    compileOnly(libs.firebase.crashlytics.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "shortvideo.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "shortvideo.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeatureApi") {
            id = "shortvideo.android.feature.api"
            implementationClass = "AndroidFeatureApiConventionPlugin"
        }
        register("androidFeatureImpl") {
            id = "shortvideo.android.feature.impl"
            implementationClass = "AndroidFeatureImplConventionPlugin"
        }
        register("androidRoom") {
            id = "shortvideo.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("koin") {
            id = "shortvideo.koin"
            implementationClass = "KoinConventionPlugin"
        }
        register("spotless") {
            id = "shortvideo.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
        register("androidCompose") {
            id = "shortvideo.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}
