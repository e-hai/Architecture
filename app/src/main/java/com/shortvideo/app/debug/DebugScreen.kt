package com.shortvideo.app.debug

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kit.ads.AdsEntity
import com.kit.ads.AdsListener
import com.kit.ads.AdsManager
import com.kit.ads.AdsRequest
import com.kit.ads.AdsType
import com.kit.ads.provider.AdsProviderType
import com.kit.log.LogKit
import com.kit.pay.PayKit
import com.kit.pay.interfaces.PayKitError
import com.kit.pay.interfaces.PurchaseCallback
import com.kit.pay.models.CustomerInfo
import com.kit.pay.models.StoreTransaction
import com.shortvideo.app.BuildConfig
import com.shortvideo.app.core.ads.AdsKeys
import com.shortvideo.app.core.pay.PayProducts
import kotlinx.coroutines.launch

/**
 * 开发者调试中心页面 Composable。
 *
 * 聚合应用内核心基础设施的调试入口：
 * 1. 广告入口 (AdsKit 官方调试面板与广告单元测试)
 * 2. 订阅/支付入口 (PayKit 买家信息、恢复购买、历史订单与沙盒订阅测试)
 * 3. 日志入口 (LogKit 应用内日志悬浮控制台与磁盘日志抓取)
 *
 * @param onBackClick 返回回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DebugScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var payStatusText by remember { mutableStateOf("未执行查询") }
    var adStatusText by remember { mutableStateOf("等待触发测试") }
    var recentLogs by remember { mutableStateOf<List<String>>(emptyList()) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF0E0D0C))
                .statusBarsPadding(),
    ) {
        // 顶部标题栏
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFFFAF6EE),
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column {
                Text(
                    text = "开发者调试中心",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAF6EE),
                )
                Text(
                    text = "${BuildConfig.BUILD_TYPE} · ${BuildConfig.FLAVOR} · v${BuildConfig.VERSION_NAME}",
                    fontSize = 11.sp,
                    color = Color(0xFFE5C384).copy(alpha = 0.8f),
                )
            }
        }

        HorizontalDivider(color = Color(0xFFFAF6EE).copy(alpha = 0.08f))

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // ==================== 1. 广告入口 (AdsKit) ====================
            DebugSectionCard(
                title = "广告调试 (AdsKit)",
                icon = Icons.Default.AdsClick,
                badge = "${AdsManager.getInitState()} · ${AdsManager.getInitializedProviderType() ?: "AdMob"}",
            ) {
                Text(
                    text = "最新回调: $adStatusText",
                    fontSize = 12.sp,
                    color = Color(0xFFFAF6EE).copy(alpha = 0.65f),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 打开 AdsKit 官方调试面板
                Button(
                    onClick = {
                        if (activity != null) {
                            AdsManager.openDebug(activity)
                            Toast.makeText(context, "正在打开 AdsKit 调试面板...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Activity 实例不可用", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5C384),
                            contentColor = Color(0xFF0E0D0C),
                        ),
                ) {
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "打开 AdsKit 官方调试面板", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 快捷广告测试按钮
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            adStatusText = "正在请求插屏广告..."
                            AdsManager.loadAd(
                                context = context,
                                request =
                                    AdsRequest(
                                        triggerId = AdsKeys.Trigger.HOME_INTERSTITIAL,
                                        adUnitId = AdsKeys.AdMobTest.INTERSTITIAL,
                                        adType = AdsType.INTERSTITIAL,
                                        providerType = AdsProviderType.ADMOB,
                                    ),
                                adListener =
                                    object : AdsListener {
                                        override fun onAdLoaded(ad: AdsEntity) {
                                            adStatusText = "插屏广告加载成功"
                                            if (activity != null) {
                                                val container = activity.findViewById<android.view.ViewGroup>(android.R.id.content)
                                                ad.show(activity, container)
                                            }
                                        }

                                        override fun onAdFailedToLoad(error: String) {
                                            adStatusText = "插屏加载失败: $error"
                                        }

                                        override fun onAdShown() {
                                            adStatusText = "插屏广告已展示"
                                        }

                                        override fun onAdClicked() {}

                                        override fun onAdPaidEvent() {}

                                        override fun onAdUserEarnedReward() {}

                                        override fun onAdClosed() {
                                            adStatusText = "插屏广告已关闭"
                                        }
                                    },
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFAF6EE)),
                    ) {
                        Text(text = "测试插屏广告", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            adStatusText = "正在请求激励视频..."
                            AdsManager.loadAd(
                                context = context,
                                request =
                                    AdsRequest(
                                        triggerId = AdsKeys.Trigger.REWARD_VIDEO,
                                        adUnitId = AdsKeys.AdMobTest.REWARDED,
                                        adType = AdsType.REWARDED,
                                        providerType = AdsProviderType.ADMOB,
                                    ),
                                adListener =
                                    object : AdsListener {
                                        override fun onAdLoaded(ad: AdsEntity) {
                                            adStatusText = "激励视频加载成功"
                                            if (activity != null) {
                                                val container = activity.findViewById<android.view.ViewGroup>(android.R.id.content)
                                                ad.show(activity, container)
                                            }
                                        }

                                        override fun onAdFailedToLoad(error: String) {
                                            adStatusText = "激励视频失败: $error"
                                        }

                                        override fun onAdShown() {}

                                        override fun onAdClicked() {}

                                        override fun onAdPaidEvent() {}

                                        override fun onAdUserEarnedReward() {
                                            adStatusText = "🎉 用户已获得激励奖励！"
                                        }

                                        override fun onAdClosed() {}
                                    },
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFAF6EE)),
                    ) {
                        Text(text = "测试激励视频", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            adStatusText = "正在请求横幅广告..."
                            AdsManager.loadAd(
                                context = context,
                                request =
                                    AdsRequest(
                                        triggerId = AdsKeys.Trigger.HOME_BANNER,
                                        adUnitId = AdsKeys.AdMobTest.BANNER,
                                        adType = AdsType.BANNER,
                                        providerType = AdsProviderType.ADMOB,
                                    ),
                                adListener =
                                    object : AdsListener {
                                        override fun onAdLoaded(ad: AdsEntity) {
                                            adStatusText = "横幅广告加载成功"
                                        }

                                        override fun onAdFailedToLoad(error: String) {
                                            adStatusText = "横幅失败: $error"
                                        }

                                        override fun onAdShown() {}

                                        override fun onAdClicked() {}

                                        override fun onAdPaidEvent() {}

                                        override fun onAdUserEarnedReward() {}

                                        override fun onAdClosed() {}
                                    },
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFAF6EE)),
                    ) {
                        Text(text = "测试横幅广告", fontSize = 12.sp)
                    }
                }
            }

            // ==================== 2. 订阅/支付入口 (PayKit) ====================
            DebugSectionCard(
                title = "订阅与支付 (PayKit)",
                icon = Icons.Default.CardMembership,
                badge = "Play Billing",
            ) {
                Text(
                    text = "预设商品: ${PayProducts.SUBS_PLUS}, ${PayProducts.SUBS_PRO}",
                    fontSize = 12.sp,
                    color = Color(0xFFFAF6EE).copy(alpha = 0.65f),
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF141312))
                            .padding(10.dp),
                ) {
                    Text(
                        text = payStatusText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE5C384),
                        lineHeight = 15.sp,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    payStatusText = "正在获取买家信息与有效订阅..."
                                    val customerInfo = PayKit.shared.getCustomerInfo()
                                    val activeSubs = customerInfo?.activeSubscriptions.orEmpty()
                                    val nonConsumables = customerInfo?.nonConsumablePurchases.orEmpty()
                                    payStatusText =
                                        "有效订阅: ${if (activeSubs.isEmpty()) "无" else activeSubs.joinToString()}\n" +
                                        "非消耗商品: ${if (nonConsumables.isEmpty()) "无" else nonConsumables.joinToString()}\n" +
                                        "原始数据: $customerInfo"
                                } catch (e: Exception) {
                                    payStatusText = "查询失败: ${e.message}"
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22201E),
                                contentColor = Color(0xFFFAF6EE),
                            ),
                    ) {
                        Text(text = "查询订阅状态", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                payStatusText = "正在恢复购买..."
                                val result = PayKit.shared.restorePurchases()
                                payStatusText =
                                    if (result.isSuccess) {
                                        "恢复成功! 订单: ${result.getOrNull()}"
                                    } else {
                                        "恢复失败: ${result.exceptionOrNull()?.message}"
                                    }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22201E),
                                contentColor = Color(0xFFFAF6EE),
                            ),
                    ) {
                        Text(text = "恢复购买", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                payStatusText = "正在同步历史订单..."
                                val result = PayKit.shared.syncPurchases()
                                payStatusText =
                                    if (result.isSuccess) {
                                        "同步成功! 订单信息: ${result.getOrNull()}"
                                    } else {
                                        "同步失败: ${result.exceptionOrNull()?.message}"
                                    }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22201E),
                                contentColor = Color(0xFFFAF6EE),
                            ),
                    ) {
                        Text(text = "同步订单", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                payStatusText = "正在从 Google Play 查询商品详情..."
                                val result =
                                    PayKit.shared.getProducts(
                                        setOf(PayProducts.SUBS_PLUS, PayProducts.SUBS_PRO),
                                    )
                                val products = result.getOrNull()
                                if (!products.isNullOrEmpty() && activity != null) {
                                    val target = products.first()
                                    payStatusText = "唤起购买: ${target.productId}..."
                                    PayKit.shared.purchase(
                                        activity = activity,
                                        storeProduct = target,
                                        callback =
                                            object : PurchaseCallback {
                                                override fun onCompleted(
                                                    storeTransaction: StoreTransaction,
                                                    customerInfo: CustomerInfo,
                                                ) {
                                                    payStatusText = "🎉 购买成功! 订单号: ${storeTransaction.orderId}"
                                                }

                                                override fun onError(
                                                    error: PayKitError,
                                                    userCancelled: Boolean,
                                                ) {
                                                    payStatusText = if (userCancelled) "用户取消购买" else "购买失败: ${error.message}"
                                                }
                                            },
                                    )
                                } else {
                                    payStatusText = "未查询到 Google Play 上架商品（需真实 Play 环境及有效签名）"
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE5C384),
                                contentColor = Color(0xFF0E0D0C),
                            ),
                    ) {
                        Text(text = "测试发起订阅", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // ==================== 3. 日志入口 (LogKit) ====================
            DebugSectionCard(
                title = "日志调试 (LogKit)",
                icon = Icons.Default.Article,
                badge = "Disk & UI",
            ) {
                Text(
                    text = "支持 Logcat、沙盒磁盘持久化及全屏日志检视器。",
                    fontSize = 12.sp,
                    color = Color(0xFFFAF6EE).copy(alpha = 0.65f),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 打开应用内日志控制台
                Button(
                    onClick = {
                        if (activity != null) {
                            // 先自愈清洗脏数据，防止 NumberFormatException 崩溃
                            com.shortvideo.app.core.log.LogSanitizer
                                .sanitize(context)
                            try {
                                LogKit.showLogUi(activity)
                            } catch (e: Exception) {
                                Toast.makeText(context, "打开控制台失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Activity 实例不可用", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5C384),
                            contentColor = Color(0xFF0E0D0C),
                        ),
                ) {
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "唤起应用内日志悬浮控制台 (Log UI)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 打桩测试日志按钮组
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            val time = System.currentTimeMillis()
                            LogKit.d("DEBUG_TEST", "这是一条 Debug 级别测试日志 [$time] 🚀")
                            Toast.makeText(context, "已写入 Debug 日志", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFAF6EE)),
                    ) {
                        Text("写入 Debug", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val time = System.currentTimeMillis()
                            LogKit.i("DEBUG_TEST", "这是一条 Info 级别常规业务日志 [$time] ℹ️")
                            Toast.makeText(context, "已写入 Info 日志", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFAF6EE)),
                    ) {
                        Text("写入 Info", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val time = System.currentTimeMillis()
                            LogKit.w("DEBUG_TEST", "这是一条 Warning 告警测试日志 [$time] ⚠️")
                            Toast.makeText(context, "已写入 Warn 日志", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFAF6EE)),
                    ) {
                        Text("写入 Warn", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val time = System.currentTimeMillis()
                            LogKit.e("DEBUG_TEST", "这是一条 Error 异常测试日志 [$time] ❌")
                            Toast.makeText(context, "已写入 Error 日志", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                    ) {
                        Text("写入 Error", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 读取最近磁盘日志预览与管理
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "最近日志预览 (${recentLogs.size} 条)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFAF6EE).copy(alpha = 0.8f),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                com.shortvideo.app.core.log.LogSanitizer
                                    .clearAllLogs(context)
                                recentLogs = emptyList()
                                Toast.makeText(context, "已清空本地日志文件", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF22201E),
                                    contentColor = Color(0xFFFF5252),
                                ),
                        ) {
                            Text(text = "清空日志", fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = {
                                com.shortvideo.app.core.log.LogSanitizer
                                    .sanitize(context)
                                LogKit.readLog { lines ->
                                    recentLogs = lines.takeLast(10).reversed()
                                }
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新日志",
                                tint = Color(0xFFE5C384),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                if (recentLogs.isNotEmpty()) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF141312))
                                .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        recentLogs.forEach { logLine ->
                            Text(
                                text = logLine,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFAF6EE).copy(alpha = 0.75f),
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 调试模块卡片容器。
 */
@Composable
private fun DebugSectionCard(
    title: String,
    icon: ImageVector,
    badge: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF161514))
                .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFE5C384),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAF6EE),
                )
            }

            Text(
                text = badge,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE5C384),
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF22201E))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        content()
    }
}
