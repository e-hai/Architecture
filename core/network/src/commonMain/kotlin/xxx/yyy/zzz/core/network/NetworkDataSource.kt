package xxx.yyy.zzz.core.network

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
 * 手写强类型 Fake 实现，遵循 "Fake over Mock" 全平台测试铁律。
 */
class FakeNetworkDataSource : NetworkDataSource {
    var shouldReturnError: Boolean = false

    override suspend fun fetchDummyData(): NetworkResult<List<DummyModel>> {
        delay(10)
        return if (shouldReturnError) {
            NetworkResult.Error(message = "Fake Network Error")
        } else {
            NetworkResult.Success(
                listOf(
                    DummyModel("1", "KMP Item 1", "Powered by Compose Multiplatform"),
                    DummyModel("2", "KMP Item 2", "Native Kit Delegation"),
                ),
            )
        }
    }
}
