package xxx.yyy.zzz.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import xxx.yyy.zzz.core.model.AppConfig

/** 默认单次请求总超时（毫秒）。 */
internal const val DEFAULT_REQUEST_TIMEOUT_MS = 30_000L

/** 默认 TCP/TLS 连接建立超时（毫秒）。 */
internal const val DEFAULT_CONNECT_TIMEOUT_MS = 15_000L

/** 默认 Socket 读写超时（毫秒）。 */
internal const val DEFAULT_SOCKET_TIMEOUT_MS = 30_000L

/**
 * 规范化基础 API 地址：去除首尾空白并确保以 `/` 结尾，防止相对路径拼接时丢失前缀路径段。
 */
internal fun normalizeBaseUrl(rawUrl: String?): String? {
    val trimmed = rawUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
}

/**
 * 跨平台平台引擎抽象，由 androidMain (OkHttp) 与 iosMain (Darwin) 具体提供。
 */
expect fun createPlatformHttpClient(): HttpClient

/**
 * 配置通用的 Ktor HTTP 客户端（基础域名绑定、超时治理、JSON 序列化、按环境动态配置日志级别）。
 */
fun configureHttpClient(
    client: HttpClient,
    appConfig: AppConfig? = null,
    requestTimeoutMillis: Long = DEFAULT_REQUEST_TIMEOUT_MS,
    connectTimeoutMillis: Long = DEFAULT_CONNECT_TIMEOUT_MS,
    socketTimeoutMillis: Long = DEFAULT_SOCKET_TIMEOUT_MS,
): HttpClient =
    client.config {
        normalizeBaseUrl(appConfig?.baseUrl)?.let { baseUrl ->
            install(DefaultRequest) {
                url(baseUrl)
            }
        }
        install(HttpTimeout) {
            this.requestTimeoutMillis = requestTimeoutMillis
            this.connectTimeoutMillis = connectTimeoutMillis
            this.socketTimeoutMillis = socketTimeoutMillis
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                },
            )
        }
        install(Logging) {
            level = if (appConfig?.isDebug == false) LogLevel.NONE else LogLevel.INFO
        }
    }
