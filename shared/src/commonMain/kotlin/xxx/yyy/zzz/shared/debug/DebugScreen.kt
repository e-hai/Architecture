package xxx.yyy.zzz.shared.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import xxx.yyy.zzz.core.abtesting.AbTestingKeys
import xxx.yyy.zzz.core.abtesting.CoreAbTesting
import xxx.yyy.zzz.core.ads.AdsKeys
import xxx.yyy.zzz.core.ads.CoreAds
import xxx.yyy.zzz.core.analytics.AnalyticsEvents
import xxx.yyy.zzz.core.analytics.AnalyticsParams
import xxx.yyy.zzz.core.analytics.CoreAnalytics
import xxx.yyy.zzz.core.crashreport.CoreCrashReport
import xxx.yyy.zzz.core.crashreport.CrashReportKeys
import xxx.yyy.zzz.core.database.DummyDao
import xxx.yyy.zzz.core.database.DummyEntity
import xxx.yyy.zzz.core.datastore.KeyValueStorage
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.FakeLogDelegate
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.mmp.CoreMmp
import xxx.yyy.zzz.core.mmp.MmpEvents
import xxx.yyy.zzz.core.mmp.MmpParams
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.model.NetworkResult
import xxx.yyy.zzz.core.navigation.BackHandler
import xxx.yyy.zzz.core.network.FakeNetworkDataSource
import xxx.yyy.zzz.core.network.NetworkDataSource
import xxx.yyy.zzz.core.pay.CorePay
import xxx.yyy.zzz.core.pay.PayProducts
import xxx.yyy.zzz.core.push.CorePush
import xxx.yyy.zzz.core.push.PushChannels
import xxx.yyy.zzz.core.ui.generated.resources.Res
import xxx.yyy.zzz.core.ui.generated.resources.back
import xxx.yyy.zzz.core.ui.generated.resources.debug_console_title

/**
 * 开发者调试面板（DebugScreen）。
 * 支持一键联调 8 大商业化 Kit、操作 Fake 数据源故障注入、以及检视 DataStore 与 Room 本地存储状态。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DebugScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    val scope = rememberCoroutineScope()
    val appConfig: AppConfig = koinInject()
    val networkDataSource: NetworkDataSource = koinInject()
    val storage: KeyValueStorage = koinInject()
    val dummyDao: DummyDao = koinInject()

    // 屏幕实时日志回显列表
    val consoleLogs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    fun logToConsole(message: String) {
        consoleLogs.add(message)
        if (consoleLogs.size > 80) consoleLogs.removeAt(0)
    }

    // 监听本地 DataStore 调试 Key
    val storedValue by storage.stringFlow("debug_sample_key").collectAsState(initial = null)
    // 监听本地 Room 数据库记录条数
    val dummyList by dummyDao.getAllDummies().collectAsState(initial = emptyList())

    // Fake 故障开关
    var fakeErrorEnabled by remember {
        mutableStateOf((networkDataSource as? FakeNetworkDataSource)?.shouldReturnError ?: false)
    }

    LaunchedEffect(consoleLogs.size) {
        if (consoleLogs.isNotEmpty()) {
            listState.animateScrollToItem(consoleLogs.size - 1)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 顶部操作栏
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onBack) {
                    Text(stringResource(Res.string.back))
                }
                Text(
                    text = stringResource(Res.string.debug_console_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedButton(onClick = { consoleLogs.clear() }) {
                    Text("Clear Log")
                }
            }
        }

        // 1. 屏幕实时日志控制台面板 (In-App Mini Console)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Text(
                        text = "CONSOLE OUTPUT (${consoleLogs.size})",
                        color = Color(0xFF888888),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                    HorizontalDivider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 4.dp))
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (consoleLogs.isEmpty()) {
                            item {
                                Text(
                                    text = "Tap buttons below to execute Kit actions...",
                                    color = Color(0xFF555555),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                        items(consoleLogs.size) { index ->
                            Text(
                                text = consoleLogs[index],
                                color = Color(0xFF00FF66),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }

        // 2. 环境配置卡片 (AppConfig Inspector)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("1. Environment & AppConfig", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Environment: ${appConfig.env}", style = MaterialTheme.typography.bodyMedium)
                    Text("• isDebug: ${appConfig.isDebug}", style = MaterialTheme.typography.bodyMedium)
                    Text("• Base URL: ${appConfig.baseUrl}", style = MaterialTheme.typography.bodyMedium)
                    Text("• Version: ${appConfig.versionName} (${appConfig.versionCode})", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 3. 八大商业化 Kit 联调实验室卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("2. Commercial Kits Playground", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreLog
                    Text("CoreLog:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            CoreLog.d(LogTags.APP, "Debug log dispatched")
                            logToConsole("[CoreLog][D] Debug log dispatched")
                        }) { Text("Log D") }
                        ElevatedButton(onClick = {
                            CoreLog.i(LogTags.APP, "Info log dispatched")
                            logToConsole("[CoreLog][I] Info log dispatched")
                        }) { Text("Log I") }
                        ElevatedButton(onClick = {
                            CoreLog.w(LogTags.APP, "Warn log dispatched")
                            logToConsole("[CoreLog][W] Warn log dispatched")
                        }) { Text("Log W") }
                        ElevatedButton(onClick = {
                            CoreLog.e(LogTags.APP, "Error log dispatched")
                            logToConsole("[CoreLog][E] Error log dispatched")
                        }) { Text("Log E") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreCrashReport
                    Text("CoreCrashReport:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            CoreCrashReport.setUserId("user_debug_001")
                            CoreCrashReport.setCustomKey(CrashReportKeys.FLAVOR, "debug_dev")
                            logToConsole("[CrashReport] Set userId=user_debug_001, key=debug_dev")
                        }) { Text("Set User & Key") }
                        ElevatedButton(onClick = {
                            CoreCrashReport.recordException(RuntimeException("Non-fatal debug crash test"))
                            logToConsole("[CrashReport] Non-fatal exception recorded")
                        }) { Text("Record Exception") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreAnalytics & CoreMmp
                    Text("CoreAnalytics & CoreMmp:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            CoreAnalytics.logEvent(
                                AnalyticsEvents.Common.BUTTON_CLICK,
                                mapOf(AnalyticsParams.ITEM_ID to "debug_btn"),
                            )
                            logToConsole("[Analytics] Log event: button_click (item_id=debug_btn)")
                        }) { Text("Log Event") }
                        ElevatedButton(onClick = {
                            CoreMmp.setCustomerUserId("mmp_user_99")
                            CoreMmp.trackEvent(MmpEvents.PURCHASE, mapOf(MmpParams.SKU to "subs_pro"))
                            logToConsole("[Mmp] Track event: purchase (sku=subs_pro)")
                        }) { Text("MMP Track") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreAbTesting
                    Text("CoreAbTesting:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            val welcome = CoreAbTesting.getString(AbTestingKeys.WELCOME_MESSAGE, "Default Welcome")
                            logToConsole("[AbTesting] Key '${AbTestingKeys.WELCOME_MESSAGE}' = $welcome")
                        }) { Text("Read Welcome Key") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreAds
                    Text("CoreAds:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            CoreAds.loadAd(AdsKeys.Trigger.REWARD_VIDEO, AdsKeys.AdMobTest.REWARDED)
                            logToConsole("[Ads] loadAd requested: trigger=${AdsKeys.Trigger.REWARD_VIDEO}")
                        }) { Text("Load Ad") }
                        ElevatedButton(onClick = {
                            val ready = CoreAds.isReady(AdsKeys.Trigger.REWARD_VIDEO)
                            logToConsole("[Ads] isReady: $ready")
                        }) { Text("Check Ready") }
                        ElevatedButton(onClick = {
                            CoreAds.showAd(AdsKeys.Trigger.REWARD_VIDEO)
                            logToConsole("[Ads] showAd requested")
                        }) { Text("Show Ad") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CorePay & CorePush
                    Text("CorePay & CorePush:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            val sub = CorePay.isSubscribed(PayProducts.SUBS_PRO)
                            logToConsole("[Pay] isSubscribed(${PayProducts.SUBS_PRO}): $sub")
                        }) { Text("Check Sub") }
                        ElevatedButton(onClick = {
                            logToConsole("[Pay] Purchase initiating for ${PayProducts.SUBS_PRO}...")
                            CorePay.purchase(PayProducts.SUBS_PRO) { success, error ->
                                logToConsole("[Pay] Purchase result: success=$success, error=$error")
                            }
                        }) { Text("Buy Sub Pro") }
                        ElevatedButton(onClick = {
                            CorePush.registerPush()
                            CorePush.setPushToken("mock_fcm_token_12345")
                            logToConsole("[Push] registerPush called, token set to mock_fcm_token_12345")
                        }) { Text("Push Register") }
                    }
                }
            }
        }

        // 4. Fake 数据与存储沙盒卡片 (Chaos & Storage Sandbox)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("3. Fake Network & Storage Sandbox", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Fake 网络故障开关
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Simulate Network Failure (Fake):", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = fakeErrorEnabled,
                            onCheckedChange = { checked ->
                                fakeErrorEnabled = checked
                                (networkDataSource as? FakeNetworkDataSource)?.shouldReturnError = checked
                                logToConsole("[FakeNetwork] shouldReturnError set to: $checked")
                            },
                        )
                    }

                    ElevatedButton(onClick = {
                        scope.launch {
                            logToConsole("[Network] Fetching dummy data...")
                            when (val result = networkDataSource.fetchDummyData()) {
                                is NetworkResult.Success -> {
                                    logToConsole("[Network][Success] Received ${result.data.size} items: ${result.data.map { it.title }}")
                                }

                                is NetworkResult.Error -> {
                                    logToConsole("[Network][Error] Msg=${result.message}")
                                }

                                is NetworkResult.Loading -> {
                                    logToConsole("[Network][Loading] Request in progress...")
                                }
                            }
                        }
                    }) {
                        Text("Execute fetchDummyData()")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // DataStore 沙盒
                    Text("DataStore Preferences:", style = MaterialTheme.typography.labelMedium)
                    Text("• Current debug_sample_key: ${storedValue ?: "(null)"}", style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = {
                            scope.launch {
                                val timestamp = "ts_${kotlin.random.Random.nextInt(1000, 9999)}"
                                storage.putString("debug_sample_key", timestamp)
                                logToConsole("[DataStore] Put debug_sample_key = $timestamp")
                            }
                        }) { Text("Write Timestamp") }
                        FilledTonalButton(onClick = {
                            scope.launch {
                                storage.remove("debug_sample_key")
                                logToConsole("[DataStore] Key debug_sample_key removed")
                            }
                        }) { Text("Clear Key") }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Room 数据库沙盒
                    Text("Room KMP Database:", style = MaterialTheme.typography.labelMedium)
                    Text("• Current dummy_table row count: ${dummyList.size}", style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = {
                            scope.launch {
                                val id = "id_${dummyList.size + 1}"
                                dummyDao.insertDummies(listOf(DummyEntity(id, "Item #$id", "Inserted via Debug Console")))
                                logToConsole("[Room] Inserted DummyEntity with id: $id")
                            }
                        }) { Text("Insert DummyEntity") }
                        FilledTonalButton(onClick = {
                            scope.launch {
                                dummyDao.clearAll()
                                logToConsole("[Room] dummy_table cleared")
                            }
                        }) { Text("Clear Room Table") }
                    }
                }
            }
        }
    }
}
