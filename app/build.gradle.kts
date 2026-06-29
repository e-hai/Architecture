import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.myproject.android.application)
    alias(libs.plugins.myproject.koin)
    alias(libs.plugins.google.services)
}

// 从 local.properties 加载签名配置
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "xxx.yyy.zzz"

    defaultConfig {
        applicationId = "xxx.yyy.zzz"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 签名配置
    signingConfigs {
        // 开发环境签名
        create("dev") {
            storeFile = file(localProperties.getProperty("DEV_STORE_FILE", ""))
            storePassword = localProperties.getProperty("DEV_STORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("DEV_KEY_ALIAS", "")
            keyPassword = localProperties.getProperty("DEV_KEY_PASSWORD", "")
        }

        // 正式环境签名
        create("prod") {
            storeFile = file(localProperties.getProperty("PROD_STORE_FILE", ""))
            storePassword = localProperties.getProperty("PROD_STORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("PROD_KEY_ALIAS", "")
            keyPassword = localProperties.getProperty("PROD_KEY_PASSWORD", "")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            // 开发环境强制使用 dev 签名
            signingConfig = signingConfigs.getByName("dev")
        }

        create("prod") {
            dimension = "environment"
            // 正式环境强制使用 prod 签名
            signingConfig = signingConfigs.getByName("prod")
        }
    }
}

// 修改生成的 APK 文件名
androidComponents {
    onVariants { variant ->
        val flavorName = variant.flavorName ?: "universal"
        val buildType = variant.buildType ?: "release"

        variant.outputs.forEach { output ->
            val versionName = android.defaultConfig.versionName ?: "1.0"
            output.outputFileName.set("app-$flavorName-$buildType-v$versionName.apk")
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
    implementation(project(":core:analytics"))
}
