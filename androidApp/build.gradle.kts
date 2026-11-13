import java.util.Properties

plugins {
    alias(libs.plugins.myproject.android.application)
}

// 仅当宿主已放置 google-services.json 时按需激活 Google Services 插件，避免缺失配置时阻塞 CI / 模板编译
if (listOf(
        file("google-services.json"),
        file("src/dev/google-services.json"),
        file("src/prod/google-services.json"),
    ).any { it.exists() }
) {
    apply(
        plugin =
            libs.plugins.google.services
                .get()
                .pluginId,
    )
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
    buildConfigField("String", "PAY_SUBS_IDS", "\"${env.get("PAY_SUBS_IDS")}\"")
    buildConfigField("String", "PAY_CONSUMABLE_IDS", "\"${env.get("PAY_CONSUMABLE_IDS")}\"")
    buildConfigField("String", "PAY_NON_CONSUMABLE_IDS", "\"${env.get("PAY_NON_CONSUMABLE_IDS")}\"")
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
                // 严禁回退给 test 签名！当执行正式 Release 打包任务时，强制阻断报错
                gradle.taskGraph.whenReady {
                    val isBuildingProdRelease =
                        allTasks.any {
                            it.name.contains("Prod", ignoreCase = true) &&
                                it.name.contains("Release", ignoreCase = true)
                        }
                    if (isBuildingProdRelease) {
                        throw GradleException(
                            "【安全隔离拦截】检测到正在打包正式 Release 应用，但未配置有效正式证书 (STORE_FILE)！" +
                                "严禁混用测试证书打包正式应用，请在 config/prod.properties 中配置正确的正式签名参数后再打包。",
                        )
                    }
                }
            }
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            // 锁定 IDE 默认构建变体为 devDebug，点击运行按钮 100% 走测试配置
            isDefault = true
            signingConfig = signingConfigs.getByName("test")
            applyEnv(devEnv)
        }
        create("prod") {
            dimension = "environment"
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
    // [KIT:ads:START]
    constraints {
        // unity-ads:4.19.0 (via AdsKit-Android) 传递引入了过时的 work-runtime:2.7.0，强制提升至 2.8.1 避免 Android 12+ PendingIntent 崩溃
        implementation("androidx.work:work-runtime:2.8.1")
        implementation("androidx.work:work-runtime-ktx:2.8.1")
    }
    // [KIT:ads:END]
    implementation(project(":shared"))
    // [KIT_DEPENDENCIES_START]
    // [KIT:log:START]
    implementation(project(":core:log"))
    implementation(libs.log.kit)
    // [KIT:log:END]
    // [KIT:crashreport:START]
    implementation(project(":core:crashreport"))
    implementation(libs.crashreport.kit)
    // [KIT:crashreport:END]
    // [KIT:analytics:START]
    implementation(project(":core:analytics"))
    implementation(libs.analytics.kit.firebase)
    // [KIT:analytics:END]
    // [KIT:abtesting:START]
    implementation(project(":core:abtesting"))
    implementation(libs.abtesting.kit.firebase)
    // [KIT:abtesting:END]
    // [KIT:mmp:START]
    implementation(project(":core:mmp"))
    implementation(libs.mmp.kit.appsflyer)
    // [KIT:mmp:END]
    // [KIT:ads:START]
    implementation(project(":core:ads"))
    implementation(libs.ads.kit.admob)
    // [KIT:ads:END]
    // [KIT:pay:START]
    implementation(project(":core:pay"))
    implementation(libs.pay.kit)
    // [KIT:pay:END]
    // [KIT:push:START]
    implementation(project(":core:push"))
    implementation(libs.push.kit)
    // [KIT:push:END]
    // [KIT:video:START]
    implementation(project(":core:video"))
    implementation(libs.video.kit)
    // [KIT:video:END]
    // [KIT:pag:START]
    implementation(project(":core:pag"))
    // [KIT:pag:END]
    // [KIT_DEPENDENCIES_END]
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
