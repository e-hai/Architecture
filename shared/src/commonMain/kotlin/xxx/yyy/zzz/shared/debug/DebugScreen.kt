package xxx.yyy.zzz.shared.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pagkit.compose.PagView
import io.github.pagkit.compose.rememberPagState
import io.github.pagkit.model.PagSource
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
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.mmp.CoreMmp
import xxx.yyy.zzz.core.mmp.MmpEvents
import xxx.yyy.zzz.core.mmp.MmpParams
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.model.NetworkResult
import xxx.yyy.zzz.core.navigation.BackHandler
import xxx.yyy.zzz.core.network.FakeNetworkDataSource
import xxx.yyy.zzz.core.network.NetworkDataSource
import xxx.yyy.zzz.core.pag.CorePag
import xxx.yyy.zzz.core.pay.CorePay
import xxx.yyy.zzz.core.pay.PayProducts
import xxx.yyy.zzz.core.push.CorePush
import xxx.yyy.zzz.core.push.PushChannels
import xxx.yyy.zzz.core.ui.generated.resources.Res
import xxx.yyy.zzz.core.ui.generated.resources.back
import xxx.yyy.zzz.core.ui.generated.resources.clear_log
import xxx.yyy.zzz.core.ui.generated.resources.debug_console_title
import xxx.yyy.zzz.core.video.CoreVideo

/**
 * 开发者调试面板（DebugScreen）。
 * 支持一键联调 8 大商业化 Kit、操作 Fake 数据源故障注入、检视 DataStore 与 Room 本地存储状态，
 * 并与平台原生 LogKit 专业日志控制台全链路打通。
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

    // 屏幕实时日志回显列表（轻量级即时反馈）
    val consoleLogs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    /**
     * 统一日志输出助手：
     * 1. 写入页面轻量级回显列表中，提供免切页面的即时操作反馈；
     * 2. 100% 真实调用 CoreLog 门面，按对应模块 Tag 写入 LogKit（同时支持控制台与持久化磁盘 CSV）。
     */
    fun emitLog(
        tag: String,
        message: String,
        level: String = "I",
    ) {
        val displayEntry = "[$tag][$level] $message"
        consoleLogs.add(displayEntry)
        if (consoleLogs.size > 100) consoleLogs.removeAt(0)

        when (level.uppercase()) {
            "V", "VERBOSE" -> CoreLog.v(tag, message)
            "D", "DEBUG" -> CoreLog.d(tag, message)
            "W", "WARN" -> CoreLog.w(tag, message)
            "E", "ERROR" -> CoreLog.e(tag, message)
            "WTF" -> CoreLog.wtf(tag, message)
            else -> CoreLog.i(tag, message)
        }
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

        // 1. 屏幕实时日志控制台面板 (In-App Mini Console + LogKit 原生控制台入口)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "轻量回显 (${consoleLogs.size})",
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                        Button(
                            onClick = { CoreLog.showLogUi() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text("打开 LogKit 原生控制台 ↗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 4.dp))
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (consoleLogs.isEmpty()) {
                            item {
                                Text(
                                    text = "点击下方按钮触发各 Kit 接口联调（自动同步写入 LogKit 磁盘）...",
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

        // 3. PAG 动效专区卡片 (CorePag & PagKit)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("2. PAG 动效渲染专区 (CorePag)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val pagState =
                        rememberPagState(
                            source = PagSource.Asset("like.pag"),
                            autoPlay = true,
                            repeatCount = 0,
                        )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        PagView(
                            state = pagState,
                            modifier =
                                Modifier
                                    .size(72.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "动效状态: ${if (pagState.isPlaying) "播放中" else "暂停"} (${if (pagState.isLoaded) "已装载" else "加载中"})",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "播放进度: ${(pagState.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            pagState.play()
                            emitLog(LogTags.PAG, "PAG动效: 播放 (like.pag)", "I")
                        }) { Text("播放") }

                        ElevatedButton(onClick = {
                            pagState.pause()
                            emitLog(LogTags.PAG, "PAG动效: 暂停", "I")
                        }) { Text("暂停") }

                        ElevatedButton(onClick = {
                            pagState.stop()
                            emitLog(LogTags.PAG, "PAG动效: 停止重置", "I")
                        }) { Text("停止") }

                        ElevatedButton(onClick = {
                            val next = (pagState.progress + 0.25).let { if (it > 1.0) 0.0 else it }
                            pagState.seekTo(next)
                            emitLog(LogTags.PAG, "PAG动效: 跳转进度 ${(next * 100).toInt()}%", "I")
                        }) { Text("+25%") }

                        ElevatedButton(onClick = {
                            CorePag.clearCache()
                            emitLog(LogTags.PAG, "已清空 PAG 磁盘缓存", "W")
                        }) { Text("清空缓存") }
                    }
                }
            }
        }

        // 4. 八大商业化 Kit 联调实验室卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("3. 八大商业化 Kit 联调实验室", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreCrashReport
                    Text("崩溃分析组件 (CoreCrashReport):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            CoreCrashReport.setUserId("user_debug_001")
                            CoreCrashReport.setCustomKey(CrashReportKeys.FLAVOR, "debug_dev")
                            emitLog(LogTags.CRASH, "已设置 userId=user_debug_001, key=debug_dev", "I")
                        }) { Text("设置用户与标签") }
                        ElevatedButton(onClick = {
                            CoreCrashReport.recordException(RuntimeException("调试模拟非致命异常"))
                            emitLog(LogTags.CRASH, "非致命异常已记录并上报", "W")
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
                            emitLog(LogTags.ANALYTICS, "上报埋点事件: button_click (item_id=debug_btn)", "I")
                        }) { Text("上报埋点事件") }
                        ElevatedButton(onClick = {
                            CoreMmp.setCustomerUserId("mmp_user_99")
                            CoreMmp.trackEvent(MmpEvents.PURCHASE, mapOf(MmpParams.SKU to "subs_pro"))
                            emitLog(LogTags.MMP, "上报归因事件: purchase (sku=subs_pro)", "I")
                        }) { Text("上报 MMP 归因") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreAbTesting
                    Text("A/B 实验与远程配置 (CoreAbTesting):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            val welcome = CoreAbTesting.getString(AbTestingKeys.WELCOME_MESSAGE, "欢迎体验 KMP 现代化架构！")
                            emitLog(LogTags.AB_TESTING, "实验键 '${AbTestingKeys.WELCOME_MESSAGE}' = $welcome", "I")
                        }) { Text("读取 A/B 配置") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreAds
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
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CorePay & CorePush
                    Text("应用内购与推送 (CorePay & CorePush):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(onClick = {
                            val sub = CorePay.isSubscribed(PayProducts.SUBS_PRO)
                            emitLog(LogTags.PAY, "订阅状态(${PayProducts.SUBS_PRO}): $sub", "I")
                        }) { Text("查询订阅状态") }
                        ElevatedButton(onClick = {
                            emitLog(LogTags.PAY, "发起购买 ${PayProducts.SUBS_PRO}...", "I")
                            CorePay.purchase(PayProducts.SUBS_PRO) { success, error ->
                                emitLog(LogTags.PAY, "购买结果: 成功=$success, 错误=$error", if (success) "I" else "E")
                            }
                        }) { Text("发起订阅购买") }
                        ElevatedButton(onClick = {
                            CorePush.registerPush()
                            CorePush.setPushToken("mock_fcm_token_12345")
                            emitLog(LogTags.PUSH, "已调用 registerPush，Token 设置为 mock_fcm_token_12345", "I")
                        }) { Text("注册远程推送") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CoreVideo (视频播放与缓存)
                    Text("音视频SDK组件 (CoreVideo):", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val sampleVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        val sampleVideoId = "video_sample_001"

                        ElevatedButton(onClick = {
                            CoreVideo.preload(sampleVideoUrl, 2 * 1024 * 1024)
                            emitLog(LogTags.VIDEO, "触发视频预加载 (2MB): $sampleVideoUrl", "I")
                        }) { Text("预加载视频 (2MB)") }

                        ElevatedButton(onClick = {
                            val cached = CoreVideo.isCached(sampleVideoUrl)
                            val bytes = CoreVideo.getCachedBytes(sampleVideoUrl)
                            emitLog(LogTags.VIDEO, "查询视频缓存: cached=$cached, bytes=${bytes / 1024}KB", "I")
                        }) { Text("检查视频缓存") }

                        ElevatedButton(onClick = {
                            val nextPos = (CoreVideo.getProgress(sampleVideoId) + 15000L) % 120000L
                            CoreVideo.saveProgress(sampleVideoId, nextPos)
                            emitLog(LogTags.VIDEO, "保存播放进度: videoId=$sampleVideoId, pos=${nextPos / 1000}s", "I")
                        }) { Text("保存断点进度(+15s)") }

                        ElevatedButton(onClick = {
                            val pos = CoreVideo.getProgress(sampleVideoId)
                            emitLog(LogTags.VIDEO, "读取播放进度: videoId=$sampleVideoId, pos=${pos / 1000}s", "I")
                        }) { Text("读取播放进度") }

                        ElevatedButton(onClick = {
                            CoreVideo.clearCache()
                            emitLog(LogTags.VIDEO, "已清空视频磁盘缓存", "W")
                        }) { Text("清空视频缓存") }
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
        }
    }
}
