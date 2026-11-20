package xxx.yyy.zzz.core.database

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Room 数据库实体与模块契约基础测试（iOS 端实际 SQLite 持久化由 [DatabaseBuilderIosTest] 覆盖）。
 */
class DummyEntityTest {
    @Test
    fun testDummyEntityEqualityAndCopy() {
        val entity = DummyEntity(id = "db_1", title = "标题", description = "描述")
        val updated = entity.copy(title = "新标题")

        assertEquals("db_1", updated.id)
        assertEquals("新标题", updated.title)
        assertEquals("描述", updated.description)
        assertNotEquals(entity, updated)
    }
}
