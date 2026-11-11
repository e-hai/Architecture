package xxx.yyy.zzz.core.pag

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * CorePag 门面契约单元测试。
 */
class CorePagTest {
    @Test
    fun testDoInitAndReady() {
        assertFalse(CorePag.isReady())
        CorePag.doInit(128L * 1024 * 1024)
        assertTrue(CorePag.isReady())
    }

    @Test
    fun testClearCache() {
        CorePag.clearCache()
        // 验证执行无异常
        assertTrue(true)
    }
}
