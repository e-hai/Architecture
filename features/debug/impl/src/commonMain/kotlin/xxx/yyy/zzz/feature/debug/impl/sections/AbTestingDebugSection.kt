package xxx.yyy.zzz.feature.debug.impl.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import xxx.yyy.zzz.core.abtesting.AbTestingKeys
import xxx.yyy.zzz.core.abtesting.CoreAbTesting
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * CoreAbTesting A/B 实验与远程配置调试区块。
 */
internal class AbTestingDebugSection : DebugSection {
    override val id: String = "abtesting"
    override val category: DebugCategory = DebugCategory.KITS
    override val order: Int = 40

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        Text("A/B 实验与远程配置 (CoreAbTesting):", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                val welcome = CoreAbTesting.getString(AbTestingKeys.WELCOME_MESSAGE, "欢迎体验 KMP 现代化架构！")
                emitLog(LogTags.AB_TESTING, "实验键 '${AbTestingKeys.WELCOME_MESSAGE}' = $welcome", "I")
            }) { Text("读取 A/B 配置") }
        }
    }
}
