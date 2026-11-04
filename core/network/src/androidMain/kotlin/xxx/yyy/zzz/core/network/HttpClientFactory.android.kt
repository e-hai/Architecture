package xxx.yyy.zzz.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Android 端采用 OkHttp 作为底层网络引擎。
 */
actual fun createPlatformHttpClient(): HttpClient = HttpClient(OkHttp)
