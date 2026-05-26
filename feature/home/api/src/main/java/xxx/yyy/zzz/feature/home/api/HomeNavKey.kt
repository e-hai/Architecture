package xxx.yyy.zzz.feature.home.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavKey : NavKey

@Serializable
data class HomeDetailNavKey(
    val id: String,
    val title: String
) : NavKey


data class TitleEditResult(
    val id: String, //唯一标识
    val title: String
)