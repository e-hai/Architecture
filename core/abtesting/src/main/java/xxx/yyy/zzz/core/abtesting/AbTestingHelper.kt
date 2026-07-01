package xxx.yyy.zzz.core.abtesting

/**
 * A/B 测试助手接口。
 *
 * 屏蔽 Firebase Remote Config 的具体实现细节，提供统一的
 * 远程参数获取和 A/B 实验管理接口。
 */
interface AbTestingHelper {

    /**
     * 获取字符串类型的远程参数。
     * @param key 参数名
     * @param defaultValue 默认值（远程未设置或未激活时返回）
     */
    fun getString(
        key: String,
        defaultValue: String = "",
    ): String

    /**
     * 获取布尔类型的远程参数。
     * @param key 参数名
     * @param defaultValue 默认值
     */
    fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean

    /**
     * 获取长整型的远程参数。
     * @param key 参数名
     * @param defaultValue 默认值
     */
    fun getLong(
        key: String,
        defaultValue: Long = 0L,
    ): Long

    /**
     * 获取双精度浮点型的远程参数。
     * @param key 参数名
     * @param defaultValue 默认值
     */
    fun getDouble(
        key: String,
        defaultValue: Double = 0.0,
    ): Double

    /**
     * 拉取并激活远程配置（异步）。
     * 等价于先 [fetch] 再 [activate]。
     * @return 是否有新参数被激活
     */
    suspend fun fetchAndActivate(): Boolean

    /**
     * 仅拉取远程配置（不激活）。
     * 通常配合 [activate] 在合适的时机统一激活。
     */
    suspend fun fetch()

    /**
     * 激活已拉取的远程配置。
     * @return 是否有新参数被激活
     */
    suspend fun activate(): Boolean
}
