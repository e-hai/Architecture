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
        // Dev 签名配置
        create("dev") {
            storeFile = file(localProperties.getProperty("DEV_STORE_FILE", ""))
            storePassword = localProperties.getProperty("DEV_STORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("DEV_KEY_ALIAS", "")
            keyPassword = localProperties.getProperty("DEV_KEY_PASSWORD", "")
        }
        
        // Prod 签名配置
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
            // 如果是 dev 渠道，无论是 release 还是 debug，都强制用 dev 签名
            signingConfig = signingConfigs.getByName("dev")
        }

        create("prod") {
            dimension = "environment"
            // 如果是 prod 渠道，无论是 release 还是 debug，都强制用 prod 签名
            signingConfig = signingConfigs.getByName("prod")
        }
    }
}

// ================= 修改生成的 APK 文件名（可选） =================
androidComponents {
    onVariants { variant ->
        // 获取渠道名 (如果没有渠道，flavorableArtifacts 会为空)
        val flavorName = variant.flavorName ?: "universal"
        val buildType = variant.buildType ?: "release"

        variant.outputs.forEach { output ->
            val mainOutput = output as? com.android.build.api.variant.impl.VariantOutputImpl
            // 获取当前版本名（如果没定义则默认为 1.0）
            val versionName = android.defaultConfig.versionName ?: "1.0"
            // 重新定义 APK 文件名
            mainOutput?.outputFileName?.set("app-$flavorName-$buildType-v$versionName.apk")
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

    // Firebase BOM for version management
    implementation(platform(libs.firebase.bom))
}
