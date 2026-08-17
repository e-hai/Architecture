import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.shortvideo.android.application)
}

// 从 local.properties 加载签名配置
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.shortvideo.app"

    defaultConfig {
        applicationId = "com.shortvideo.app"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Kit 密钥：写入 local.properties，缺省用测试/空占位
        val mmpAppToken = localProperties.getProperty("mmp.appToken", "")
        val mmpAppId = localProperties.getProperty("mmp.appId", "")
        val admobAppId =
            localProperties.getProperty(
                "admob.app.id",
                "ca-app-pub-3940256099942544~3347511713",
            )
        val applovinSdkKey = localProperties.getProperty("applovin.sdk.key", "")

        buildConfigField("String", "MMP_APP_TOKEN", "\"$mmpAppToken\"")
        buildConfigField("String", "MMP_APP_ID", "\"$mmpAppId\"")
        buildConfigField("String", "ADMOB_APP_ID", "\"$admobAppId\"")
        buildConfigField("String", "APPLOVIN_SDK_KEY", "\"$applovinSdkKey\"")

        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
        manifestPlaceholders["APPLOVIN_SDK_KEY"] = applovinSdkKey
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

// Dev flavor 使用占位 google-services.json，跳过 Crashlytics 映射上传
tasks.configureEach {
    if (name.startsWith("uploadCrashlyticsMappingFile")) {
        enabled = false
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:network"))
    implementation(project(":core:analytics"))
    implementation(project(":core:abtesting"))
    implementation(project(":core:crashreport"))
    implementation(project(":core:mmp"))
    implementation(project(":core:ads"))
    implementation(project(":core:pay"))
    implementation(project(":core:push"))
    implementation(project(":core:log"))
    implementation(project(":core:video"))
    implementation(project(":feature:feed:api"))
    implementation(project(":feature:feed:impl"))
    implementation(project(":feature:comment:api"))
    implementation(project(":feature:comment:impl"))
    implementation(project(":feature:discover:api"))
    implementation(project(":feature:discover:impl"))
    implementation(project(":feature:profile:api"))
    implementation(project(":feature:profile:impl"))
    implementation(project(":feature:creator:api"))
    implementation(project(":feature:creator:impl"))
}
