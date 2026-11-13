plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.myproject.koin)
}

kotlin {
    android {
        namespace = "xxx.yyy.zzz.core.network"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
