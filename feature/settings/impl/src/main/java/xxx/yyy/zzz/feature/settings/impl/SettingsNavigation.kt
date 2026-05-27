package xxx.yyy.zzz.feature.settings.impl

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xxx.yyy.zzz.feature.home.api.HomeNavKey
import xxx.yyy.zzz.feature.settings.api.AboutAppNavKey
import xxx.yyy.zzz.feature.settings.api.PrivacyPolicyNavKey
import xxx.yyy.zzz.feature.settings.api.SettingsNavKey
import xxx.yyy.zzz.feature.settings.api.UserAgreementNavKey

fun EntryProviderScope<NavKey>.settingsEntry(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    entry<SettingsNavKey> {
        SettingsRoute(
            onItemClick = { item ->
                val destination = when (item) {
                    SettingsItem.UserAgreement -> UserAgreementNavKey
                    SettingsItem.PrivacyPolicy -> PrivacyPolicyNavKey
                    SettingsItem.AboutApp -> AboutAppNavKey
                    SettingsItem.GoToHome -> HomeNavKey
                }
                onNavigate(destination)
            }
        )
    }

    entry<UserAgreementNavKey> {
        DetailScreen(title = "User Agreement")
    }

    entry<PrivacyPolicyNavKey> {
        DetailScreen(title = "Privacy Policy")
    }

    entry<AboutAppNavKey> {
        DetailScreen(title = "About App")
    }
}
