package com.shortvideo.app.core.pay

import com.kit.pay.models.PayKitConfiguration

/**
 * Play Console 商品 ID 与权益配置。
 *
 * 占位 ID 须在真实项目中替换为 Console 中的商品；
 * 订阅 / 消耗 / 非消耗三分法必须与后台一致，否则补单会走错 ack/consume。
 */
object PayProducts {
    /** 订阅：Plus 档。 */
    const val SUBS_PLUS = "subs_plus"

    /** 订阅：Pro 档。 */
    const val SUBS_PRO = "subs_pro"

    /** 消耗型：限时皮肤等。 */
    const val CONSUMABLE_SKIN_3DAY = "consumable_product_01"

    /** 非消耗型：永久皮肤等。 */
    const val NON_CONSUMABLE_SKIN = "one_time_product_01"

    /**
     * 生成 PayKit 初始化配置。
     */
    fun toConfiguration(): PayKitConfiguration =
        PayKitConfiguration(
            subsProductIds = setOf(SUBS_PLUS, SUBS_PRO),
            consumableProductIds = setOf(CONSUMABLE_SKIN_3DAY),
            nonConsumableProductIds = setOf(NON_CONSUMABLE_SKIN),
        )
}
