package com.shortvideo.app.core.pay

import android.content.Context
import com.kit.pay.PayKit
import com.kit.pay.models.PayKitConfiguration

/**
 * PayKit 初始化入口。
 * 配置商品三分法后连接 Play Billing 并自动 sync。
 */
object PayInitializer {
    /**
     * 配置并启动支付 SDK。重复调用会被 PayKit 忽略。
     *
     * @param context 任意 Context（内部取 applicationContext）
     * @param configuration 商品 ID 三分法配置，默认使用 [PayProducts.toConfiguration]
     */
    fun configure(
        context: Context,
        configuration: PayKitConfiguration = PayProducts.toConfiguration(),
    ) {
        PayKit.configure(context, configuration)
    }
}
