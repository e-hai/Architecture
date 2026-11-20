package xxx.yyy.zzz.core.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * 基础领域模型与环境配置单元测试。
 */
class CoreModelTest {
    @Test
    fun testAppConfigDefaultsByEnvironment() {
        val devConfig = AppConfig(env = AppEnvironment.DEV)
        val prodConfig = AppConfig(env = AppEnvironment.PROD)

        assertEquals("https://dev-api.example.com", devConfig.baseUrl)
        assertEquals("https://api.example.com", prodConfig.baseUrl)
        assertTrue(devConfig.isDebug)
        assertEquals("1.0.0", prodConfig.versionName)
        assertEquals(1, prodConfig.versionCode)
    }

    @Test
    fun testDummyModelSerializationRoundTrip() {
        val model =
            DummyModel(
                id = "item_1",
                title = "标题",
                description = "描述",
                createdAt = 1_700_000_000_000L,
            )
        val json = Json.encodeToString(model)
        val decoded = Json.decodeFromString<DummyModel>(json)
        assertEquals(model, decoded)
    }

    @Test
    fun testNetworkResultVariants() {
        val success: NetworkResult<String> = NetworkResult.Success("ok")
        val error: NetworkResult<String> = NetworkResult.Error(message = "boom")
        val loading: NetworkResult<String> = NetworkResult.Loading

        assertIs<NetworkResult.Success<String>>(success)
        assertEquals("ok", success.data)
        assertIs<NetworkResult.Error>(error)
        assertEquals("boom", error.message)
        assertIs<NetworkResult.Loading>(loading)
    }
}
