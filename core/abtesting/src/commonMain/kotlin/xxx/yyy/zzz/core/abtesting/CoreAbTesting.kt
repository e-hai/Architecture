package xxx.yyy.zzz.core.abtesting

/**
 * 跨平台 A/B 测试委托协议。
 * 约束：仅允许在该接口的各端实现类内部调用原生 AbTestingKit SDK。
 */
interface AbTestingDelegate {
    /**
     * 初始化 A/B 测试与 Remote Config SDK。
     */
    fun doInit()

    fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean

    fun getString(
        key: String,
        defaultValue: String = "",
    ): String

    fun getLong(
        key: String,
        defaultValue: Long = 0L,
    ): Long

    fun getDouble(
        key: String,
        defaultValue: Double = 0.0,
    ): Double
}

/**
 * 默认本地配置兜底实现。
 */
object DefaultAbTestingDelegate : AbTestingDelegate {
    private val defaults = AbTestingKeys.defaults()

    override fun doInit() = Unit

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = (defaults[key] as? Boolean) ?: defaultValue

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = (defaults[key] as? String) ?: defaultValue

    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = (defaults[key] as? Long) ?: defaultValue

    override fun getDouble(
        key: String,
        defaultValue: Double,
    ): Double = (defaults[key] as? Double) ?: defaultValue
}

/**
 * 跨平台静态 A/B 测试与 Remote Config 门面（CoreAbTesting）。
 */
object CoreAbTesting : AbTestingDelegate {
    private var delegate: AbTestingDelegate = DefaultAbTestingDelegate

    /**
     * 挂载平台特定的 A/B 测试委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的 A/B 测试委托接口
     */
    fun install(delegate: AbTestingDelegate) {
        this.delegate = delegate
    }

    override fun doInit() = delegate.doInit()

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ) = delegate.getBoolean(key, defaultValue)

    override fun getString(
        key: String,
        defaultValue: String,
    ) = delegate.getString(key, defaultValue)

    override fun getLong(
        key: String,
        defaultValue: Long,
    ) = delegate.getLong(key, defaultValue)

    override fun getDouble(
        key: String,
        defaultValue: Double,
    ) = delegate.getDouble(key, defaultValue)
}
