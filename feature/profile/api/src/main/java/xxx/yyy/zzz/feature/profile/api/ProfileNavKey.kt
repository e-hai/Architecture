package xxx.yyy.zzz.feature.profile.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 个人主页/用户主页导航公钥 (NavKey)。
 *
 * @property userId 用户 ID，为空时代表“我的”个人中心
 */
@Serializable
data class ProfileNavKey(
    val userId: String? = null,
) : NavKey
