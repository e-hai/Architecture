package xxx.yyy.zzz.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

/**
 * iOS 端采用苹果原生 URLSession (Darwin) 作为底层网络引擎。
 */
actual fun createPlatformHttpClient(): HttpClient = HttpClient(Darwin)
