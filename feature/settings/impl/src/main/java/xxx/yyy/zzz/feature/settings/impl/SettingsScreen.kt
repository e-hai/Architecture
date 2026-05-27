package xxx.yyy.zzz.feature.settings.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    onItemClick: (SettingsItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel()
) {
    SettingsScreen(
        onItemClick = onItemClick,
        modifier = modifier
    )
}

@Composable
fun SettingsScreen(
    onItemClick: (SettingsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val settingsItems = listOf(
        SettingsItem.UserAgreement,
        SettingsItem.PrivacyPolicy,
        SettingsItem.AboutApp,
        SettingsItem.GoToHome
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        settingsItems.forEach { item ->
            SettingsRow(
                item = item,
                onClick = { onItemClick(item) }
            )

            HorizontalDivider()
        }
    }
}

@Composable
fun SettingsRow(
    item: SettingsItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(item.titleRes),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

enum class SettingsItem(val titleRes: Int) {
    UserAgreement(R.string.settings_user_agreement),
    PrivacyPolicy(R.string.settings_privacy_policy),
    AboutApp(R.string.settings_about_app),
    GoToHome(R.string.settings_go_to_home)
}

@Composable
fun DetailScreen(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
