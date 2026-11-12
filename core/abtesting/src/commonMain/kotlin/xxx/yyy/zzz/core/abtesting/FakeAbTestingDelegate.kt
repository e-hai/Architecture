package xxx.yyy.zzz.core.abtesting

/**
 * 内存可编程 A/B 测试模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakeAbTestingDelegate : AbTestingDelegate {
    var initCount = 0
    val values = mutableMapOf<String, Any>()

    override fun doInit() {
        initCount++
    }

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = (values[key] as? Boolean) ?: defaultValue

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = (values[key] as? String) ?: defaultValue

    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = (values[key] as? Long) ?: defaultValue

    override fun getDouble(
        key: String,
        defaultValue: Double,
    ): Double = (values[key] as? Double) ?: defaultValue

    fun clear() {
        initCount = 0
        values.clear()
    }
}
