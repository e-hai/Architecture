package xxx.yyy.zzz.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import xxx.yyy.zzz.core.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String,
) {
    fun toDomainModel(): User =
        User(
            id = id,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
        )
}

fun User.toEntity(): UserEntity =
    UserEntity(
        id = id,
        name = name,
        email = email,
        avatarUrl = avatarUrl,
    )
