package xxx.yyy.zzz.core.ui.nativehost

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.Foundation.NSLock
import platform.UIKit.UIView

/**
 * iOS 端单个局部原生视图的绑定契约。
 *
 * 由 Swift 侧实现（或通过 [NativeViewRegistry.registerView] 便捷方法封装），
 * 承载单个 `UIView` 实例及其更新/销毁生命周期。
 */
interface IosNativeViewBinding {
    /** 绑定的 iOS 原生视图实例。 */
    val view: UIView

    /**
     * 当 Compose 传入更新后的 [NativeViewRequest] 时触发。
     *
     * @param request 最新的请求参数
     */
    fun onUpdate(request: NativeViewRequest) = Unit

    /**
     * 当视图离开组合树或重建时触发，用于停止播放器、释放播放器/控制器引用等清理动作。
     */
    fun onRelease() = Unit
}

/**
 * iOS 端局部原生视图创建工厂。
 */
fun interface IosNativeViewFactory {
    /**
     * 根据请求参数创建一个全新且独立的 iOS 原生视图绑定实例。
     *
     * @param request 原生视图请求参数
     */
    fun create(request: NativeViewRequest): IosNativeViewBinding
}

/**
 * iOS 平台局部原生视图注册表实现（基于 `NSLock` 保证线程安全）。
 */
actual object NativeViewRegistry {
    private val lock = NSLock()
    private val factories = mutableMapOf<String, IosNativeViewFactory>()

    /**
     * 注册指定类型的 iOS 原生视图工厂。
     *
     * @param viewType 原生视图类型标识
     * @param factory 视图创建工厂
     */
    fun register(
        viewType: String,
        factory: IosNativeViewFactory,
    ) {
        lock.lock()
        try {
            factories[viewType] = factory
        } finally {
            lock.unlock()
        }
    }

    /**
     * 面向 Swift 调用方提供的闭包便捷注册入口，避免手动实现 [IosNativeViewFactory] 与 [IosNativeViewBinding]。
     *
     * @param viewType 原生视图类型标识
     * @param create 创建原生 [UIView] 实例的闭包
     * @param onUpdate 视图参数变更时的更新闭包
     * @param onRelease 视图卸载释放时的清理闭包
     */
    fun registerView(
        viewType: String,
        create: (NativeViewRequest) -> UIView,
        onUpdate: (UIView, NativeViewRequest) -> Unit = { _, _ -> },
        onRelease: (UIView) -> Unit = {},
    ) {
        register(
            viewType = viewType,
            factory =
                IosNativeViewFactory { req ->
                    val createdView = create(req)
                    object : IosNativeViewBinding {
                        override val view: UIView = createdView

                        override fun onUpdate(request: NativeViewRequest) {
                            onUpdate(createdView, request)
                        }

                        override fun onRelease() {
                            onRelease(createdView)
                        }
                    }
                },
        )
    }

    actual fun isRegistered(viewType: String): Boolean {
        lock.lock()
        return try {
            factories.containsKey(viewType)
        } finally {
            lock.unlock()
        }
    }

    actual fun unregister(viewType: String) {
        lock.lock()
        try {
            factories.remove(viewType)
        } finally {
            lock.unlock()
        }
    }

    actual fun clear() {
        lock.lock()
        try {
            factories.clear()
        } finally {
            lock.unlock()
        }
    }

    internal fun resolve(viewType: String): IosNativeViewFactory? {
        lock.lock()
        return try {
            factories[viewType]
        } finally {
            lock.unlock()
        }
    }
}

private class IosViewBindingHolder {
    var binding: IosNativeViewBinding? = null
    private var released: Boolean = false

    fun attach(newBinding: IosNativeViewBinding): UIView {
        binding = newBinding
        released = false
        return newBinding.view
    }

    fun releaseIfNeeded() {
        if (!released) {
            released = true
            binding?.onRelease()
            binding = null
        }
    }
}

@Composable
internal actual fun PlatformNativeViewHost(
    request: NativeViewRequest,
    modifier: Modifier,
    fallback: @Composable (NativeViewRequest) -> Unit,
) {
    val factory = NativeViewRegistry.resolve(request.viewType)
    if (factory == null) {
        Box(modifier = modifier) {
            fallback(request)
        }
        return
    }

    key(request.viewType, request.instanceKey) {
        val holder = remember(request.viewType, request.instanceKey) { IosViewBindingHolder() }
        UIKitView(
            factory = {
                holder.attach(factory.create(request))
            },
            modifier = modifier,
            update = { _ ->
                holder.binding?.onUpdate(request)
            },
            onRelease = { _ ->
                holder.releaseIfNeeded()
            },
        )
    }
}
