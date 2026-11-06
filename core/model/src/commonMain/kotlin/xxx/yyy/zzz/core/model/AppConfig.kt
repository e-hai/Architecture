package xxx.yyy.zzz.core.model

/**
 * 跨平台运行环境枚举。
 */
enum class AppEnvironment {
    DEV,
    PROD,
}

/**
 * 跨平台应用运行时统一环境配置对象。
 * 由 Android/iOS 宿主在冷启动启动 KMP Koin 容器时注入。
 *
 * @property env 运行环境（开发/生产）
 * @property isDebug 是否为调试模式
 * @property baseUrl 基础网络 API 地址
 * @property versionName 应用版本名
 * @property versionCode 应用构建编号
 */
data class AppConfig(
    val env: AppEnvironment = AppEnvironment.DEV,
    val isDebug: Boolean = true,
    val baseUrl: String = if (env == AppEnvironment.DEV) "https://dev-api.example.com" else "https://api.example.com",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
)
