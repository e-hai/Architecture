package xxx.yyy.zzz.feature.debug.impl

import org.koin.dsl.koinApplication
import xxx.yyy.zzz.feature.debug.api.DebugSection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Debug 模块 Koin 插件化调试区块注册测试。
 */
class DebugModuleTest {
    @Test
    fun testDebugSectionsRegisteredInKoin() {
        val koinApp =
            koinApplication {
                modules(debugModule)
            }
        val sections = koinApp.koin.getAll<DebugSection>()
        val ids = sections.map { it.id }.toSet()

        assertTrue("infra" in ids, "InfraDebugSection should be registered")
        // [KIT:pag:START]
        assertTrue("pag" in ids, "PagDebugSection should be registered")
        // [KIT:pag:END]
        // [KIT:video:START]
        assertTrue("video" in ids, "VideoDebugSection should be registered")
        // [KIT:video:END]
        assertEquals(sections.size, ids.size, "Section ids should be unique")
        koinApp.close()
    }
}
