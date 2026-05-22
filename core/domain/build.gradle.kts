plugins {
    alias(libs.plugins.myproject.kotlin.jvm)
    alias(libs.plugins.myproject.koin)
}

dependencies {
    implementation(project(":core:model"))
    
    // Core Coroutines & Dependency Injection
    implementation(libs.kotlinx.coroutines.core)
}
