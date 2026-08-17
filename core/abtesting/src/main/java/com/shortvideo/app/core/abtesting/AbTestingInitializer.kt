package com.shortvideo.app.core.abtesting

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kit.abtesting.AbTestingClient
import com.kit.abtesting.AbTestingConfig
import com.kit.abtesting.firebase.FirebaseAbTestingProvider
import kotlinx.coroutines.launch

/**
 * AbTestingKit 初始化入口。
 * install Firebase Provider → setDefaults → 异步 fetchAndActivate。
 */
object AbTestingInitializer {
    /**
     * 安装 Provider 并启动默认值写入与远端拉取。
     * 宿主须已配置 `google-services` / `google-services.json`（Kit 不初始化 FirebaseApp）。
     *
     * @param context Application Context
     * @param debug 为 true 时将最小拉取间隔设为 0，便于调试
     * @param defaults 本地兜底，默认 [AbTestingKeys.defaults]
     */
    fun initialize(
        context: Context,
        debug: Boolean,
        defaults: Map<String, Any> = AbTestingKeys.defaults(),
    ) {
        AbTestingClient.install(
            FirebaseAbTestingProvider(
                context = context.applicationContext,
                config =
                    AbTestingConfig(
                        minimumFetchIntervalInSeconds = if (debug) 0L else 3600L,
                    ),
            ),
        )

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            runCatching {
                AbTestingClient.setDefaults(defaults)
                AbTestingClient.fetchAndActivate()
            }
        }
    }
}
