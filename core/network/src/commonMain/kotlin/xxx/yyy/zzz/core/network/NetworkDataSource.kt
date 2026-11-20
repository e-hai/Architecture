package xxx.yyy.zzz.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import xxx.yyy.zzz.core.model.DummyModel
import xxx.yyy.zzz.core.model.NetworkResult

/**
 * 网络数据源通用接口。
 */
interface NetworkDataSource {
    suspend fun fetchDummyData(): NetworkResult<List<DummyModel>>
}

/**
 * 基于真实 Ktor [HttpClient] 的参考网络数据源实现（自动复用 [configureHttpClient] 注入的 baseUrl 与超时配置）。
 */
class KtorNetworkDataSource(
    private val httpClient: HttpClient,
    private val endpointPath: String = "dummies",
) : NetworkDataSource {
    override suspend fun fetchDummyData(): NetworkResult<List<DummyModel>> =
        try {
            val response: List<DummyModel> = httpClient.get(endpointPath).body()
            NetworkResult.Success(response)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            NetworkResult.Error(
                exception = t,
                message = t.message ?: "网络请求失败",
            )
        }
}

/**
 * 手写强类型 Fake 实现，遵循 "Fake over Mock" 全平台测试铁律。
 */
class FakeNetworkDataSource : NetworkDataSource {
    var shouldReturnError: Boolean = false

    override suspend fun fetchDummyData(): NetworkResult<List<DummyModel>> {
        delay(10)
        return if (shouldReturnError) {
            NetworkResult.Error(message = "网络请求失败（模拟故障）")
        } else {
            NetworkResult.Success(
                listOf(
                    DummyModel("1", "模拟数据条目 1", "基于 Compose Multiplatform 跨平台呈现"),
                    DummyModel("2", "模拟数据条目 2", "原生 Kit 双端对称委托驱动"),
                ),
            )
        }
    }
}
