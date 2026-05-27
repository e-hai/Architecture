plugins {
    `kotlin-dsl`
}

group = "xxx.yyy.zzz.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "myproject.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "myproject.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeatureApi") {
            id = "myproject.android.feature.api"
            implementationClass = "AndroidFeatureApiConventionPlugin"
        }
        register("androidFeatureImpl") {
            id = "myproject.android.feature.impl"
            implementationClass = "AndroidFeatureImplConventionPlugin"
        }
        register("kotlinJvm") {
            id = "myproject.kotlin.jvm"
            implementationClass = "KotlinJvmConventionPlugin"
        }
        register("androidRoom") {
            id = "myproject.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("koin") {
            id = "myproject.koin"
            implementationClass = "KoinConventionPlugin"
        }
        register("spotless") {
            id = "myproject.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
        register("androidCompose") {
            id = "myproject.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}
