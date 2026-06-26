package xxx.yyy.zzz.core.data

import kotlinx.coroutines.flow.Flow
import xxx.yyy.zzz.core.model.User

interface UserRepository {
    fun getUserStream(userId: String): Flow<User?>

    suspend fun syncUser(userId: String): Result<Unit>
}
