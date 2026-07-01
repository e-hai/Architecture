package xxx.yyy.zzz

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 骨架占位导航键。
 *
 * 当项目添加真实 feature 模块后，用对应的 NavKey 替换此占位键。
 */
@Serializable
data object SkeletonNavKey : NavKey
