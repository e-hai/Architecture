package xxx.yyy.zzz.feature.debug.impl.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import xxx.yyy.zzz.core.ads.AdsKeys
import xxx.yyy.zzz.core.ads.CoreAds
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

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
        Text("商业化广告组件 (CoreAds):", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                CoreAds.loadAd(AdsKeys.Trigger.REWARD_VIDEO, "")
                emitLog(LogTags.ADS, "发起广告加载: trigger=${AdsKeys.Trigger.REWARD_VIDEO}", "I")
            }) { Text("请求激励广告") }
            ElevatedButton(onClick = {
                val ready = CoreAds.isReady(AdsKeys.Trigger.REWARD_VIDEO)
                emitLog(LogTags.ADS, "广告是否就绪: $ready", "I")
            }) { Text("检查广告就绪") }
            ElevatedButton(onClick = {
                CoreAds.showAd(AdsKeys.Trigger.REWARD_VIDEO)
                emitLog(LogTags.ADS, "发起广告展示", "I")
            }) { Text("展示激励广告") }
            ElevatedButton(onClick = {
                CoreAds.openDebug()
                emitLog(LogTags.ADS, "打开广告 SDK 调试面板", "I")
            }) { Text("广告 Debug 面板") }
        }
    }
}
