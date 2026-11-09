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
import androidx.compose.ui.text.style.TextAlign
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
import xxx.yyy.zzz.core.ui.generated.resources.clear_log
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.debug_console_title),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = { consoleLogs.clear() }) {
                    Text(stringResource(Res.string.clear_log))
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
                        text = "控制台实时输出 (${consoleLogs.size})",
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
                                    text = "点击下方按钮触发各 Kit 接口联调...",
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
                    Text("1. 运行环境与 App 配置", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• 运行环境: ${appConfig.env}", style = MaterialTheme.typography.bodyMedium)
                    Text("• 调试模式 (isDebug): ${appConfig.isDebug}", style = MaterialTheme.typography.bodyMedium)
                    Text("• 服务端基地址: ${appConfig.baseUrl}", style = MaterialTheme.typography.bodyMedium)
                    Text("• 应用版本: ${appConfig.versionName} (${appConfig.versionCode})", style = MaterialTheme.typography.bodyMedium)
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
                    Text("2. 八大商业化 Kit 联调实验室", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreLog
                    Text("日志组件 (CoreLog):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            CoreLog.d(LogTags.APP, "Debug 调试日志已分发")
                            logToConsole("[CoreLog][D] Debug 调试日志已分发")
                        }) { Text("调试日志 (D)") }
                        ElevatedButton(onClick = {
                            CoreLog.i(LogTags.APP, "Info 普通日志已分发")
                            logToConsole("[CoreLog][I] Info 普通日志已分发")
                        }) { Text("普通日志 (I)") }
                        ElevatedButton(onClick = {
                            CoreLog.w(LogTags.APP, "Warn 警告日志已分发")
                            logToConsole("[CoreLog][W] Warn 警告日志已分发")
                        }) { Text("警告日志 (W)") }
                        ElevatedButton(onClick = {
                            CoreLog.e(LogTags.APP, "Error 错误日志已分发")
                            logToConsole("[CoreLog][E] Error 错误日志已分发")
                        }) { Text("错误日志 (E)") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreCrashReport
                    Text("崩溃分析组件 (CoreCrashReport):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            CoreCrashReport.setUserId("user_debug_001")
                            CoreCrashReport.setCustomKey(CrashReportKeys.FLAVOR, "debug_dev")
                            logToConsole("[CrashReport] 已设置 userId=user_debug_001, key=debug_dev")
                        }) { Text("设置用户与标签") }
                        ElevatedButton(onClick = {
                            CoreCrashReport.recordException(RuntimeException("调试模拟非致命异常"))
                            logToConsole("[CrashReport] 非致命异常已成功记录")
                        }) { Text("上报非致命异常") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreAnalytics & CoreMmp
                    Text("数据埋点与归因 (CoreAnalytics & CoreMmp):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            CoreAnalytics.logEvent(
                                AnalyticsEvents.Common.BUTTON_CLICK,
                                mapOf(AnalyticsParams.ITEM_ID to "debug_btn"),
                            )
                            logToConsole("[Analytics] 上报埋点事件: button_click (item_id=debug_btn)")
                        }) { Text("上报埋点事件") }
                        ElevatedButton(onClick = {
                            CoreMmp.setCustomerUserId("mmp_user_99")
                            CoreMmp.trackEvent(MmpEvents.PURCHASE, mapOf(MmpParams.SKU to "subs_pro"))
                            logToConsole("[Mmp] 上报归因事件: purchase (sku=subs_pro)")
                        }) { Text("上报 MMP 归因") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreAbTesting
                    Text("A/B 实验与远程配置 (CoreAbTesting):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            val welcome = CoreAbTesting.getString(AbTestingKeys.WELCOME_MESSAGE, "欢迎体验 KMP 现代化架构！")
                            logToConsole("[AbTesting] 实验键 '${AbTestingKeys.WELCOME_MESSAGE}' = $welcome")
                        }) { Text("读取 A/B 配置") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreAds
                    Text("商业化广告组件 (CoreAds):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            CoreAds.loadAd(AdsKeys.Trigger.REWARD_VIDEO, AdsKeys.AdMobTest.REWARDED)
                            logToConsole("[Ads] 发起广告加载: trigger=${AdsKeys.Trigger.REWARD_VIDEO}")
                        }) { Text("请求激励广告") }
                        ElevatedButton(onClick = {
                            val ready = CoreAds.isReady(AdsKeys.Trigger.REWARD_VIDEO)
                            logToConsole("[Ads] 广告是否就绪: $ready")
                        }) { Text("检查广告就绪") }
                        ElevatedButton(onClick = {
                            CoreAds.showAd(AdsKeys.Trigger.REWARD_VIDEO)
                            logToConsole("[Ads] 发起广告展示")
                        }) { Text("展示激励广告") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CorePay & CorePush
                    Text("应用内购与推送 (CorePay & CorePush):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            val sub = CorePay.isSubscribed(PayProducts.SUBS_PRO)
                            logToConsole("[Pay] 订阅状态(${PayProducts.SUBS_PRO}): $sub")
                        }) { Text("查询订阅状态") }
                        ElevatedButton(onClick = {
                            logToConsole("[Pay] 发起购买 ${PayProducts.SUBS_PRO}...")
                            CorePay.purchase(PayProducts.SUBS_PRO) { success, error ->
                                logToConsole("[Pay] 购买结果: 成功=$success, 错误=$error")
                            }
                        }) { Text("发起订阅购买") }
                        ElevatedButton(onClick = {
                            CorePush.registerPush()
                            CorePush.setPushToken("mock_fcm_token_12345")
                            logToConsole("[Push] 已调用 registerPush，Token 设置为 mock_fcm_token_12345")
                        }) { Text("注册远程推送") }
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
                    Text("3. 弱网模拟与本地存储沙盒", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

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
                                logToConsole("[FakeNetwork] 弱网开关设置为: $checked")
                            },
                        )
                    }

                    ElevatedButton(onClick = {
                        scope.launch {
                            logToConsole("[Network] 正在发起模拟数据请求...")
                            when (val result = networkDataSource.fetchDummyData()) {
                                is NetworkResult.Success -> {
                                    logToConsole("[Network][成功] 收到 ${result.data.size} 条数据: ${result.data.map { it.title }}")
                                }

                                is NetworkResult.Error -> {
                                    logToConsole("[Network][失败] 错误信息=${result.message}")
                                }

                                is NetworkResult.Loading -> {
                                    logToConsole("[Network][加载中] 数据请求处理中...")
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
                                logToConsole("[DataStore] 写入 debug_sample_key = $timestamp")
                            }
                        }) { Text("写入时间戳") }
                        FilledTonalButton(onClick = {
                            scope.launch {
                                storage.remove("debug_sample_key")
                                logToConsole("[DataStore] 已移除 debug_sample_key")
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
                                logToConsole("[Room] 成功插入 DummyEntity: id=$id")
                            }
                        }) { Text("插入一条数据") }
                        FilledTonalButton(onClick = {
                            scope.launch {
                                dummyDao.clearAll()
                                logToConsole("[Room] 已清空 dummy_table 数据表")
                            }
                        }) { Text("清空数据库表") }
                    }
                }
            }
        }
    }
}
