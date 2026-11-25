package xxx.yyy.zzz.core.ui.nativehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * 局部生命周期原生对象句柄契约。
 *
 * 用于替代全局单例 `*Delegate` 无法表达的场景（如多实例播放器引擎、RTC/IM 会话、
 * 原生控制器等需要随页面或状态机独立创建与销毁的对象）。
 *
 * 实现类必须保证 [close] 的幂等性，在重复调用后不会重复释放底层原生资源，并将 [isClosed] 置为 `true`。
 */
interface ScopedNativeHandle : AutoCloseable {
    /** 当前句柄关联的底层原生实例是否已被释放。 */
    val isClosed: Boolean

    /**
     * 释放当前实例持有的原生资源（幂等操作）。
     */
    override fun close()
}

/**
 * 无状态局部原生对象工厂契约。
 *
 * 由双端宿主通过全局 `*Delegate` 装配，在每次业务需要独立会话/控制器时调用 [create] 产出隔离句柄，
 * 避免不同页面或同一页面多个组件之间产生状态串扰。
 */
fun interface ScopedNativeFactory<in Config, out T : ScopedNativeHandle> {
    /**
     * 根据初始化参数创建一个全新且生命周期独立的原生句柄实例。
     *
     * @param config 实例化配置参数
     * @return 新创建的局部原生句柄
     */
    fun create(config: Config): T
}

/**
 * 在 Compose 作用域内创建并绑定局部原生句柄的生命周期。
 *
 * 当组合离开界面树或 [key] 发生变化时，自动触发 [ScopedNativeHandle.close] 释放原生资源。
 *
 * @param key 用于控制句柄重建边界的稳定键（默认仅创建一次）
 * @param factory 用于构造局部原生句柄的工厂函数（通常委托给已注册的 [ScopedNativeFactory]）
 * @return 与当前组合生命周期绑定的句柄实例
 */
@Composable
fun <T : ScopedNativeHandle> rememberScopedNativeHandle(
    key: Any? = Unit,
    factory: () -> T,
): T {
    val handle = remember(key) { factory() }
    DisposableEffect(handle) {
        onDispose {
            handle.close()
        }
    }
    return handle
}
