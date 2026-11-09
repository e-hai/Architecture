import java.util.Properties

plugins {
    alias(libs.plugins.myproject.android.application)
}

/**
 * 封装环境配置读取器，支持 .properties 文件、环境特定环境变量 (如 DEV_BASE_URL) 与全局环境变量
 */
class EnvConfig(
    private val props: Properties,
    private val envPrefix: String,
) {
    fun get(
        key: String,
        default: String = "",
    ): String =
        props.getProperty(key)?.takeIf { it.isNotBlank() }
            ?: System.getenv("${envPrefix}_$key")?.takeIf { it.isNotBlank() }
            ?: System.getenv(key)?.takeIf { it.isNotBlank() }
            ?: default
}

fun loadEnvConfig(
    primaryPath: String,
    fallbackPath: String? = null,
    envPrefix: String,
): EnvConfig {
    val props = Properties()
    val primaryFile = rootProject.file(primaryPath)
    val targetFile =
        if (primaryFile.exists()) {
            primaryFile
        } else if (fallbackPath != null) {
            rootProject.file(fallbackPath).takeIf { it.exists() }
        } else {
            null
        }

    targetFile?.inputStream()?.use { props.load(it) }
    return EnvConfig(props, envPrefix)
}

val devEnv = loadEnvConfig("config/dev.properties", envPrefix = "DEV")
val prodEnv =
    loadEnvConfig(
        primaryPath = "config/prod.properties",
        fallbackPath = "config/prod.properties.example",
        envPrefix = "PROD",
    )

fun com.android.build.api.dsl.ApplicationProductFlavor.applyEnv(env: EnvConfig) {
    applicationId = env.get("APP_ID")
    manifestPlaceholders["appName"] = env.get("APP_NAME")
    manifestPlaceholders["admobAppId"] = env.get("ADMOB_APP_ID")

    buildConfigField("String", "BASE_URL", "\"${env.get("BASE_URL")}\"")
    buildConfigField("String", "ADMOB_APP_ID", "\"${env.get("ADMOB_APP_ID")}\"")
    buildConfigField("String", "ADMOB_BANNER_ID", "\"${env.get("ADMOB_BANNER_ID")}\"")
    buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${env.get("ADMOB_INTERSTITIAL_ID")}\"")
    buildConfigField("String", "ADMOB_REWARDED_ID", "\"${env.get("ADMOB_REWARDED_ID")}\"")
    buildConfigField("String", "ADMOB_APP_OPEN_ID", "\"${env.get("ADMOB_APP_OPEN_ID")}\"")
    buildConfigField("String", "APPSFLYER_KEY", "\"${env.get("APPSFLYER_KEY")}\"")
    buildConfigField("boolean", "ENABLE_HTTP_LOG", env.get("ENABLE_HTTP_LOG", "true"))
    buildConfigField("boolean", "ANALYTICS_DEBUG_MODE", env.get("ANALYTICS_DEBUG_MODE", "true"))
    buildConfigField(
        "int",
        "REMOTE_CONFIG_MIN_FETCH_INTERVAL",
        env.get("REMOTE_CONFIG_MIN_FETCH_INTERVAL_SECONDS", "0"),
    )
}

android {
    namespace = "xxx.yyy.zzz.android"

    defaultConfig {
        applicationId = prodEnv.get("APP_ID", "xxx.yyy.zzz")
        versionCode = devEnv.get("VERSION_CODE", "1").toIntOrNull() ?: 1
        versionName = devEnv.get("VERSION_NAME", "1.0.0")
    }

    signingConfigs {
        create("test") {
            val storeFilePath = devEnv.get("STORE_FILE")
            val storeFileObj = if (storeFilePath.isNotBlank()) rootProject.file(storeFilePath) else null
            if (storeFileObj != null && storeFileObj.exists()) {
                storeFile = storeFileObj
                storePassword = devEnv.get("STORE_PASSWORD", "android")
                keyAlias = devEnv.get("KEY_ALIAS", "androiddebugkey")
                keyPassword = devEnv.get("KEY_PASSWORD", "android")
            } else {
                initWith(getByName("debug"))
            }
        }

        create("prod") {
            val storeFilePath = prodEnv.get("STORE_FILE")
            val storeFileObj = if (storeFilePath.isNotBlank()) rootProject.file(storeFilePath) else null
            if (storeFileObj != null && storeFileObj.exists()) {
                storeFile = storeFileObj
                storePassword = prodEnv.get("STORE_PASSWORD")
                keyAlias = prodEnv.get("KEY_ALIAS")
                keyPassword = prodEnv.get("KEY_PASSWORD")
            } else {
                initWith(getByName("test"))
            }
        }
    }

    productFlavors {
        named("dev") {
            // 锁定 IDE 默认构建变体为 devDebug，点击运行按钮 100% 走测试配置
            isDefault = true
            signingConfig = signingConfigs.getByName("test")
            applyEnv(devEnv)
        }
        named("prod") {
            signingConfig = signingConfigs.getByName("prod")
            applyEnv(prodEnv)
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("test")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:log"))
    implementation(project(":core:crashreport"))
    implementation(project(":core:analytics"))
    implementation(project(":core:abtesting"))
    implementation(project(":core:mmp"))
    implementation(project(":core:ads"))
    implementation(project(":core:pay"))
    implementation(project(":core:push"))
    implementation(libs.log.kit)
    implementation(libs.crashreport.kit)
    implementation(libs.analytics.kit.firebase)
    implementation(libs.abtesting.kit.firebase)
    implementation(libs.mmp.kit.appsflyer)
    implementation(libs.ads.kit.admob)
    implementation(libs.pay.kit)
    implementation(libs.push.kit)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
