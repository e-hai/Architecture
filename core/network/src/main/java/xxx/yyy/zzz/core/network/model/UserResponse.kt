package xxx.yyy.zzz.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xxx.yyy.zzz.core.model.User

@Serializable
data class UserResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("avatar_url") val avatarUrl: String,
) {
    fun toDomainModel(): User =
        User(
            id = id,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
        )
}
