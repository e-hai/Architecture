plugins {
    `kotlin-dsl`
}

group = "xxx.yyy.zzz.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
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
        register("kmpLibrary") {
            id = "myproject.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "myproject.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("kmpFeatureApi") {
            id = "myproject.kmp.feature.api"
            implementationClass = "KmpFeatureApiConventionPlugin"
        }
        register("kmpFeatureImpl") {
            id = "myproject.kmp.feature.impl"
            implementationClass = "KmpFeatureImplConventionPlugin"
        }
        register("kmpRoom") {
            id = "myproject.kmp.room"
            implementationClass = "KmpRoomConventionPlugin"
        }
        register("koin") {
            id = "myproject.koin"
            implementationClass = "KoinConventionPlugin"
        }
        register("spotless") {
            id = "myproject.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
    }
}
