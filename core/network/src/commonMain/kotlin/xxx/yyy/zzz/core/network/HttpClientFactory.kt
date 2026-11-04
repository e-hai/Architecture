package xxx.yyy.zzz.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 跨平台平台引擎抽象，由 androidMain (OkHttp) 与 iosMain (Darwin) 具体提供。
 */
expect fun createPlatformHttpClient(): HttpClient

/**
 * 配置通用的 Ktor HTTP 客户端（JSON 序列化、日志追踪）。
 */
fun configureHttpClient(client: HttpClient): HttpClient =
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
            level = LogLevel.INFO
        }
    }
