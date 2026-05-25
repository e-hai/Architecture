package xxx.yyy.zzz.feature.settings.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SettingsNavKey : NavKey

@Serializable
data object UserAgreementNavKey : NavKey

@Serializable
data object PrivacyPolicyNavKey : NavKey

@Serializable
data object AboutAppNavKey : NavKey
