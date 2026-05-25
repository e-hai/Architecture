plugins {
    alias(libs.plugins.myproject.android.application)
    alias(libs.plugins.myproject.koin)
}

android {
    namespace = "xxx.yyy.zzz"

    defaultConfig {
        applicationId = "xxx.yyy.zzz"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":feature:home:api"))
    implementation(project(":feature:home:impl"))
    implementation(project(":feature:settings:api"))
    implementation(project(":feature:settings:impl"))
    
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:network"))
    implementation(project(":core:domain"))

}
