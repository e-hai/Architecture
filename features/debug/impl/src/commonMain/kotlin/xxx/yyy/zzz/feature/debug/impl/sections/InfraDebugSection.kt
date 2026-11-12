package xxx.yyy.zzz.feature.debug.impl.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import xxx.yyy.zzz.core.database.DummyDao
import xxx.yyy.zzz.core.database.DummyEntity
import xxx.yyy.zzz.core.datastore.KeyValueStorage
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.model.NetworkResult
import xxx.yyy.zzz.core.network.FakeNetworkDataSource
import xxx.yyy.zzz.core.network.NetworkDataSource
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * 基础数据与网络沙盒调试区块（Fake 网络故障注入、DataStore 读写、Room KMP CRUD）。
 */
internal class InfraDebugSection : DebugSection {
    override val id: String = "infra"
    override val category: DebugCategory = DebugCategory.SANDBOX
    override val order: Int = 10

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        val scope = rememberCoroutineScope()
        val networkDataSource: NetworkDataSource = koinInject()
        val storage: KeyValueStorage = koinInject()
        val dummyDao: DummyDao = koinInject()

        val storedValue by storage.stringFlow("debug_sample_key").collectAsState(initial = null)
        val dummyList by dummyDao.getAllDummies().collectAsState(initial = emptyList())
        var fakeErrorEnabled by remember {
            mutableStateOf((networkDataSource as? FakeNetworkDataSource)?.shouldReturnError ?: false)
        }

        // Fake 网络故障开关
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("模拟弱网/网络异常 (Fake):", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = fakeErrorEnabled,
                onCheckedChange = { checked ->
                    fakeErrorEnabled = checked
                    (networkDataSource as? FakeNetworkDataSource)?.shouldReturnError = checked
                    emitLog(LogTags.NETWORK, "弱网开关设置为: $checked", "W")
                },
            )
        }

        ElevatedButton(onClick = {
            scope.launch {
                emitLog(LogTags.NETWORK, "正在发起模拟数据请求...", "I")
                when (val result = networkDataSource.fetchDummyData()) {
                    is NetworkResult.Success -> {
                        emitLog(LogTags.NETWORK, "收到 ${result.data.size} 条数据: ${result.data.map { it.title }}", "I")
                    }

                    is NetworkResult.Error -> {
                        emitLog(LogTags.NETWORK, "错误信息=${result.message}", "E")
                    }

                    is NetworkResult.Loading -> {
                        emitLog(LogTags.NETWORK, "数据请求处理中...", "I")
                    }
                }
            }
        }) {
            Text("执行模拟数据请求 (fetchDummyData)")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // DataStore 沙盒
        Text("DataStore 键值偏好存储:", style = MaterialTheme.typography.labelMedium)
        Text("• 当前 debug_sample_key: ${storedValue ?: "(空)"}", style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = {
                scope.launch {
                    val timestamp = "ts_${kotlin.random.Random.nextInt(1000, 9999)}"
                    storage.putString("debug_sample_key", timestamp)
                    emitLog(LogTags.DATA, "写入 debug_sample_key = $timestamp", "I")
                }
            }) { Text("写入时间戳") }
            FilledTonalButton(onClick = {
                scope.launch {
                    storage.remove("debug_sample_key")
                    emitLog(LogTags.DATA, "已移除 debug_sample_key", "I")
                }
            }) { Text("清除 Key") }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Room 数据库沙盒
        Text("Room KMP 本地数据库:", style = MaterialTheme.typography.labelMedium)
        Text("• 当前 dummy_table 记录条数: ${dummyList.size}", style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = {
                scope.launch {
                    val id = "id_${dummyList.size + 1}"
                    dummyDao.insertDummies(listOf(DummyEntity(id, "模拟数据 #$id", "通过调试控制台插入")))
                    emitLog(LogTags.DATA, "成功插入 DummyEntity: id=$id", "I")
                }
            }) { Text("插入一条数据") }
            FilledTonalButton(onClick = {
                scope.launch {
                    dummyDao.clearAll()
                    emitLog(LogTags.DATA, "已清空 dummy_table 数据表", "W")
                }
            }) { Text("清空数据库表") }
        }
    }
}
