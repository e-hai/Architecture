package xxx.yyy.zzz.core.model

import kotlinx.serialization.Serializable

/**
 * 跨平台领域基础模型占位实体。
 *
 * @property id 唯一标识符
 * @property title 显示标题
 * @property description 详细描述内容
 * @property createdAt 实体创建时间戳（毫秒）
 */
@Serializable
data class DummyModel(
    val id: String,
    val title: String,
    val description: String,
    val createdAt: Long = 0L,
)
