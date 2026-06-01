package xxx.yyy.zzz.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import xxx.yyy.zzz.core.database.dao.UserDao
import xxx.yyy.zzz.core.database.model.toEntity
import xxx.yyy.zzz.core.datastore.UserPreferencesDataSource
import xxx.yyy.zzz.core.model.User
import xxx.yyy.zzz.core.network.UserService

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val userService: UserService,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override fun getUserStream(userId: String): Flow<User?> {
        return userDao.getUserStream(userId)
            .map { entity -> entity?.toDomainModel() }
            .flowOn(ioDispatcher)
    }

    override suspend fun syncUser(userId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val response = userService.getUser(userId)
            val domainModel = response.toDomainModel()
            userDao.insertUser(domainModel.toEntity())
            userPreferencesDataSource.setLastSyncedUserId(userId)
        }
    }
}
