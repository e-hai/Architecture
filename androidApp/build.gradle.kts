import java.io.File

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
 * 封装双端统一 `.xcconfig` 配置读取器，支持 `#include?`、环境变量回退及 `$(VAR)` 引用展开。
 */
class EnvConfig(
    private val values: Map<String, String>,
    private val envPrefix: String,
) {
    private val varPattern = Regex("""\$\(([A-Za-z0-9_]+)\)""")

    fun get(
        key: String,
        default: String = "",
    ): String {
        val raw = rawLookup(key) ?: return default
        return expandVars(raw, depth = 0).ifBlank { default }
    }

    fun getBool(
        key: String,
        default: Boolean,
    ): Boolean =
        when (get(key).lowercase()) {
            "true", "yes", "1" -> true
            "false", "no", "0" -> false
            else -> default
        }

    fun getBaseUrl(): String {
        val rawHost = get("API_HOST").trim()
        if (rawHost.isBlank()) return ""
        if (rawHost.startsWith("http://") || rawHost.startsWith("https://")) {
            return rawHost
        }
        val scheme = get("API_SCHEME", "https").ifBlank { "https" }
        return "$scheme://$rawHost"
    }

    private fun rawLookup(key: String): String? {
        val fileVal = values[key]?.takeIf { it.isNotBlank() }
        if (fileVal != null) return fileVal
        val directEnv = System.getenv("${envPrefix}_$key")?.takeIf { it.isNotBlank() }
        if (directEnv != null) return directEnv
        if (key.startsWith("ANDROID_")) {
            val legacyKey = key.removePrefix("ANDROID_")
            val legacyEnv = System.getenv("${envPrefix}_$legacyKey")?.takeIf { it.isNotBlank() }
            if (legacyEnv != null) return legacyEnv
        }
        return null
    }

    private fun expandVars(
        input: String,
        depth: Int,
    ): String {
        if (depth >= MAX_EXPANSION_DEPTH || !input.contains("$(")) return input
        return varPattern.replace(input) { match ->
            val refKey = match.groupValues[1]
            val resolved = rawLookup(refKey) ?: ""
            expandVars(resolved, depth + 1)
        }
    }

    private companion object {
        const val MAX_EXPANSION_DEPTH = 5
    }
}

private val xcconfigIncludeRegex = Regex("""^#include(\?)?\s+"([^"]+)"""")
private val xcconfigInlineCommentRegex = Regex("""\s+//.*$""")

private fun readXcconfigInto(
    file: File,
    target: MutableMap<String, String>,
    visited: MutableSet<String>,
) {
    if (!file.exists()) return
    val canonical = file.canonicalPath
    if (!visited.add(canonical)) return

    file.readLines(Charsets.UTF_8).forEach { rawLine ->
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("//")) return@forEach
        val includeMatch = xcconfigIncludeRegex.matchEntire(trimmed)
        if (includeMatch != null) {
            val includedFile = File(file.parentFile, includeMatch.groupValues[2])
            readXcconfigInto(includedFile, target, visited)
            return@forEach
        }
        val eqIdx = trimmed.indexOf('=')
        if (eqIdx > 0) {
            val key = trimmed.substring(0, eqIdx).trim()
            val rawVal = trimmed.substring(eqIdx + 1).trim()
            val cleanVal =
                if (rawVal.startsWith("//")) {
                    ""
                } else {
                    rawVal.replace(xcconfigInlineCommentRegex, "").trim()
                }
            target[key] = cleanVal
        }
    }
}

fun loadXcconfig(
    relativePath: String,
    envPrefix: String,
): EnvConfig {
    val values = mutableMapOf<String, String>()
    readXcconfigInto(rootProject.file(relativePath), values, mutableSetOf())
    return EnvConfig(values, envPrefix)
}

val devEnv = loadXcconfig("config/dev.xcconfig", envPrefix = "DEV")
val prodEnv = loadXcconfig("config/prod.xcconfig", envPrefix = "PROD")

fun com.android.build.api.dsl.ApplicationProductFlavor.applyEnv(env: EnvConfig) {
    applicationId = env.get("APP_ID")
    versionCode = env.get("VERSION_CODE", "1").toIntOrNull() ?: 1
    versionName = env.get("VERSION_NAME", "1.0.0")
    manifestPlaceholders["appName"] = env.get("APP_NAME")
    manifestPlaceholders["admobAppId"] = env.get("ANDROID_ADMOB_APP_ID")

    buildConfigField("String", "BASE_URL", "\"${env.getBaseUrl()}\"")
    buildConfigField("String", "ADMOB_APP_ID", "\"${env.get("ANDROID_ADMOB_APP_ID")}\"")
    buildConfigField("String", "ADMOB_BANNER_ID", "\"${env.get("ANDROID_ADMOB_BANNER_ID")}\"")
    buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${env.get("ANDROID_ADMOB_INTERSTITIAL_ID")}\"")
    buildConfigField("String", "ADMOB_REWARDED_ID", "\"${env.get("ANDROID_ADMOB_REWARDED_ID")}\"")
    buildConfigField("String", "ADMOB_APP_OPEN_ID", "\"${env.get("ANDROID_ADMOB_APP_OPEN_ID")}\"")
    buildConfigField("String", "APPSFLYER_KEY", "\"${env.get("APPSFLYER_KEY")}\"")
    buildConfigField("String", "PAY_SUBS_IDS", "\"${env.get("PAY_SUBS_IDS")}\"")
    buildConfigField("String", "PAY_CONSUMABLE_IDS", "\"${env.get("PAY_CONSUMABLE_IDS")}\"")
    buildConfigField("String", "PAY_NON_CONSUMABLE_IDS", "\"${env.get("PAY_NON_CONSUMABLE_IDS")}\"")
    buildConfigField("boolean", "ENABLE_HTTP_LOG", env.getBool("ENABLE_HTTP_LOG", default = true).toString())
    buildConfigField("boolean", "ANALYTICS_DEBUG_MODE", env.getBool("ANALYTICS_DEBUG_MODE", default = true).toString())
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

    val devStoreFilePath = devEnv.get("ANDROID_STORE_FILE")
    val devStoreFileObj = if (devStoreFilePath.isNotBlank()) rootProject.file(devStoreFilePath) else null
    val devSigningConfig =
        signingConfigs.run {
            if (devStoreFileObj != null && devStoreFileObj.exists()) {
                create("test") {
                    storeFile = devStoreFileObj
                    storePassword = devEnv.get("ANDROID_STORE_PASSWORD", "android")
                    keyAlias = devEnv.get("ANDROID_KEY_ALIAS", "androiddebugkey")
                    keyPassword = devEnv.get("ANDROID_KEY_PASSWORD", "android")
                }
            } else {
                // 未配置自定义测试签名库时直接使用 AGP 原生 debug 配置，兼容无 ~/.android/debug.keystore 的全新主机与 Linux CI
                getByName("debug")
            }
        }

    signingConfigs {
        create("prod") {
            val storeFilePath = prodEnv.get("ANDROID_STORE_FILE")
            val storeFileObj = if (storeFilePath.isNotBlank()) rootProject.file(storeFilePath) else null
            if (storeFileObj != null && storeFileObj.exists()) {
                storeFile = storeFileObj
                storePassword = prodEnv.get("ANDROID_STORE_PASSWORD")
                keyAlias = prodEnv.get("ANDROID_KEY_ALIAS")
                keyPassword = prodEnv.get("ANDROID_KEY_PASSWORD")
            } else {
                // 严禁回退给 test 签名！当执行正式 Release 打包任务时，强制阻断报错
                gradle.taskGraph.whenReady {
                    val isBuildingProdRelease =
                        allTasks.any { it.name.contains("ProdRelease") }
                    if (isBuildingProdRelease) {
                        throw GradleException(
                            "【安全隔离拦截】检测到正在打包正式 Release 应用，但未配置有效正式证书 (ANDROID_STORE_FILE)！" +
                                "严禁混用测试证书打包正式应用，请在 config/prod.local.xcconfig 中配置正式签名参数（或注入 PROD_STORE_* 环境变量）后再打包。",
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
            signingConfig = devSigningConfig
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
            signingConfig = devSigningConfig
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
        implementation(libs.androidx.work.runtime)
        implementation(libs.androidx.work.runtime.ktx)
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
