package xxx.yyy.zzz.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val LAST_SYNCED_USER_ID = stringPreferencesKey("last_synced_user_id")
    }

    val lastSyncedUserId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_SYNCED_USER_ID]
    }

    suspend fun setLastSyncedUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNCED_USER_ID] = userId
        }
    }
}
