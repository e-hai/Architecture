package xxx.yyy.zzz.core.domain

import kotlinx.coroutines.flow.Flow
import xxx.yyy.zzz.core.model.User

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<User?> {
        return userRepository.getUserStream(userId)
    }
}
