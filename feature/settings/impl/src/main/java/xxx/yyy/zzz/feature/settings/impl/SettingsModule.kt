package xxx.yyy.zzz.feature.settings.impl

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureSettingsModule =
    module {
        viewModel { SettingsViewModel() }
    }
