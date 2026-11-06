package xxx.yyy.zzz.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import xxx.yyy.zzz.core.model.AppConfig

/**
 * 跨平台平台引擎抽象，由 androidMain (OkHttp) 与 iosMain (Darwin) 具体提供。
 */
expect fun createPlatformHttpClient(): HttpClient

/**
 * 配置通用的 Ktor HTTP 客户端（JSON 序列化、按环境动态配置日志级别）。
 */
fun configureHttpClient(
    client: HttpClient,
    appConfig: AppConfig? = null,
): HttpClient =
    client.config {
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
