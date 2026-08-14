package xxx.yyy.zzz.feature.creator.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 创作中心/拍摄与发布导航公钥 (NavKey)。
 *
 * @property mode 初始打开模式："record"（直接拍摄）或 "publish"（直接发布）
 */
@Serializable
data class CreatorNavKey(
    val mode: String = "record",
) : NavKey
