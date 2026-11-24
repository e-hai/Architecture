package xxx.yyy.zzz.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineCapability
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.test.runTest
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.model.AppEnvironment
import xxx.yyy.zzz.core.model.NetworkResult
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 基于内存 Fake [HttpClientEngineBase] 验证 Ktor 客户端插件配置与 [KtorNetworkDataSource] 行为。
 */
class KtorNetworkDataSourceTest {
    @Test
    fun testNormalizeBaseUrl() {
        assertNull(normalizeBaseUrl(null))
        assertNull(normalizeBaseUrl("   "))
        assertEquals("https://api.example.com/", normalizeBaseUrl("https://api.example.com"))
        assertEquals("https://api.example.com/v1/", normalizeBaseUrl("  https://api.example.com/v1/ "))
    }

    @Test
    fun testResolveHttpLogLevelFollowsEnableHttpLog() {
        assertEquals(LogLevel.INFO, resolveHttpLogLevel(null))
        assertEquals(LogLevel.NONE, resolveHttpLogLevel(AppConfig(isDebug = false)))
        assertEquals(LogLevel.NONE, resolveHttpLogLevel(AppConfig(isDebug = true, enableHttpLog = false)))
        assertEquals(LogLevel.INFO, resolveHttpLogLevel(AppConfig(isDebug = false, enableHttpLog = true)))
    }

    @Test
    fun testConfigureHttpClientBindsBaseUrlAndTimeoutAndParsesResponse() =
        runTest {
            val fakeEngine =
                FakeHttpClientEngine { request ->
                    respondJson(
                        request = request,
                        body = """[{"id":"42","title":"Ktor Item","description":"Loaded via HttpClient"}]""",
                    )
                }
            val appConfig =
                AppConfig(
                    env = AppEnvironment.PROD,
                    isDebug = false,
                    baseUrl = "https://api.example.com/v1",
                )
            val client = configureHttpClient(HttpClient(fakeEngine), appConfig)
            try {
                assertNotNull(client.pluginOrNull(HttpTimeout))
                val dataSource = KtorNetworkDataSource(client, endpointPath = "dummies")
                val result = dataSource.fetchDummyData()

                assertTrue(result is NetworkResult.Success, "Expected Success but got $result")
                assertEquals("https://api.example.com/v1/dummies", fakeEngine.lastRequestUrl)
                assertEquals(1, result.data.size)
                assertEquals("42", result.data.first().id)
                assertEquals("Ktor Item", result.data.first().title)
            } finally {
                client.close()
            }
        }

    @Test
    fun testKtorNetworkDataSourceReturnsErrorOnFailure() =
        runTest {
            val fakeEngine =
                FakeHttpClientEngine {
                    error("Simulated transport failure")
                }
            val client = configureHttpClient(HttpClient(fakeEngine), AppConfig(baseUrl = "https://dev-api.example.com"))
            try {
                val dataSource = KtorNetworkDataSource(client)
                val result = dataSource.fetchDummyData()

                assertTrue(result is NetworkResult.Error, "Expected Error but got $result")
                assertTrue(
                    result.message.contains("Simulated transport failure"),
                    "Unexpected error message: ${result.message} / ${result.exception}",
                )
            } finally {
                client.close()
            }
        }
}

private class FakeHttpClientEngine(
    private val handler: suspend FakeHttpClientEngine.(HttpRequestData) -> HttpResponseData,
) : HttpClientEngineBase("fake-ktor-engine") {
    override val config: HttpClientEngineConfig = HttpClientEngineConfig()
    override val supportedCapabilities: Set<HttpClientEngineCapability<*>> = setOf(HttpTimeoutCapability)
    var lastRequestUrl: String? = null

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        lastRequestUrl = data.url.toString()
        return handler(data)
    }

    fun respondJson(
        request: HttpRequestData,
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        headers: Headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    ): HttpResponseData =
        HttpResponseData(
            statusCode = status,
            requestTime = GMTDate(),
            headers = headers,
            version = HttpProtocolVersion.HTTP_1_1,
            body = ByteReadChannel(body.encodeToByteArray()),
            callContext = request.executionContext as CoroutineContext,
        )
}
