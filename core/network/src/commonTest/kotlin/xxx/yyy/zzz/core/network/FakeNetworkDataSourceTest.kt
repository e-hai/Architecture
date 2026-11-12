package xxx.yyy.zzz.core.network

import kotlinx.coroutines.test.runTest
import xxx.yyy.zzz.core.model.NetworkResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * FakeNetworkDataSource 契约与故障注入行为单元测试。
 */
class FakeNetworkDataSourceTest {
    private val dataSource = FakeNetworkDataSource()

    @Test
    fun testFetchDummyDataSuccess() =
        runTest {
            dataSource.shouldReturnError = false
            val result = dataSource.fetchDummyData()
            assertTrue(result is NetworkResult.Success)
            assertEquals(2, result.data.size)
            assertEquals("1", result.data.first().id)
        }

    @Test
    fun testFetchDummyDataError() =
        runTest {
            dataSource.shouldReturnError = true
            val result = dataSource.fetchDummyData()
            assertTrue(result is NetworkResult.Error)
            assertEquals("网络请求失败（模拟故障）", result.message)
        }
}
