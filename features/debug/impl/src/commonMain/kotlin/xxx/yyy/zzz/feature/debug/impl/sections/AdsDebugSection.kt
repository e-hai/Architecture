package xxx.yyy.zzz.feature.debug.impl.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import xxx.yyy.zzz.core.ads.AdFormat
import xxx.yyy.zzz.core.ads.AdsKeys
import xxx.yyy.zzz.core.ads.CoreAds
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

private data class AdDebugPreset(
    val label: String,
    val triggerId: String,
    val format: AdFormat,
)

private val adDebugPresets =
    listOf(
        AdDebugPreset("激励 Rewarded", AdsKeys.Trigger.REWARD_VIDEO, AdFormat.REWARDED),
        AdDebugPreset("插屏 Interstitial", AdsKeys.Trigger.HOME_INTERSTITIAL, AdFormat.INTERSTITIAL),
        AdDebugPreset("开屏 AppOpen", AdsKeys.Trigger.APP_OPEN, AdFormat.APP_OPEN),
        AdDebugPreset("横幅 Banner", AdsKeys.Trigger.HOME_BANNER, AdFormat.BANNER),
    )

/**
 * CoreAds 商业化广告聚合调试区块。
 */
internal class AdsDebugSection : DebugSection {
    override val id: String = "ads"
    override val category: DebugCategory = DebugCategory.KITS
    override val order: Int = 50

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        var selectedPreset by remember { mutableStateOf(adDebugPresets.first()) }

        Text("商业化广告组件 (CoreAds):", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            adDebugPresets.forEach { preset ->
                FilterChip(
                    selected = selectedPreset == preset,
                    onClick = { selectedPreset = preset },
                    label = { Text(preset.label) },
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                CoreAds.loadAd(selectedPreset.triggerId, "", selectedPreset.format)
                emitLog(
                    LogTags.ADS,
                    "发起广告加载: trigger=${selectedPreset.triggerId}, format=${selectedPreset.format}",
                    "I",
                )
            }) { Text("请求广告") }
            ElevatedButton(onClick = {
                val ready = CoreAds.isReady(selectedPreset.triggerId)
                emitLog(
                    LogTags.ADS,
                    "广告是否就绪 (${selectedPreset.format}): $ready",
                    "I",
                )
            }) { Text("检查就绪") }
            ElevatedButton(onClick = {
                CoreAds.showAd(selectedPreset.triggerId)
                emitLog(
                    LogTags.ADS,
                    "发起广告展示: trigger=${selectedPreset.triggerId}, format=${selectedPreset.format}",
                    "I",
                )
            }) { Text("展示广告") }
            ElevatedButton(onClick = {
                CoreAds.openDebug()
                emitLog(LogTags.ADS, "打开广告 SDK 调试面板", "I")
            }) { Text("广告 Debug 面板") }
        }
    }
}
