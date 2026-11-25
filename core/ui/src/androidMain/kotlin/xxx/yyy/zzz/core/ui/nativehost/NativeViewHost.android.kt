package xxx.yyy.zzz.core.ui.nativehost

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.ConcurrentHashMap

/**
 * Android 端单个局部原生视图的绑定句柄。
 *
 * @param view 已初始化的 Android 原生 [View] 实例
 * @param onUpdate 当 Compose 重组传入新的 [NativeViewRequest] 时触发的增量更新回调
 * @param onRelease 当视图离开组合树或重建时触发的原生资源回收回调
 */
data class AndroidNativeViewBinding(
    val view: View,
    val onUpdate: (NativeViewRequest) -> Unit = {},
    val onRelease: () -> Unit = {},
)

/**
 * Android 端局部原生视图创建工厂。
 */
fun interface AndroidNativeViewFactory {
    /**
     * 基于宿主 [Context] 与请求参数创建独立的原生视图绑定对象。
     *
     * @param context Android 视图上下文
     * @param request 原生视图请求参数
     */
    fun create(
        context: Context,
        request: NativeViewRequest,
    ): AndroidNativeViewBinding
}

/**
 * Android 平台局部原生视图注册表实现（线程安全）。
 */
actual object NativeViewRegistry {
    private val factories = ConcurrentHashMap<String, AndroidNativeViewFactory>()

    /**
     * 注册指定类型的 Android 原生视图工厂。
     *
     * @param viewType 原生视图类型标识
     * @param factory 视图创建工厂
     */
    fun register(
        viewType: String,
        factory: AndroidNativeViewFactory,
    ) {
        factories[viewType] = factory
    }

    actual fun isRegistered(viewType: String): Boolean = factories.containsKey(viewType)

    actual fun unregister(viewType: String) {
        factories.remove(viewType)
    }

    actual fun clear() {
        factories.clear()
    }

    internal fun resolve(viewType: String): AndroidNativeViewFactory? = factories[viewType]
}

private class AndroidViewBindingHolder {
    var binding: AndroidNativeViewBinding? = null
    private var released: Boolean = false

    fun attach(newBinding: AndroidNativeViewBinding): View {
        binding = newBinding
        released = false
        return newBinding.view
    }

    fun releaseIfNeeded() {
        if (!released) {
            released = true
            binding?.onRelease?.invoke()
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
        val holder = remember(request.viewType, request.instanceKey) { AndroidViewBindingHolder() }
        AndroidView(
            factory = { context ->
                holder.attach(factory.create(context, request))
            },
            modifier = modifier,
            update = { _ ->
                holder.binding?.onUpdate?.invoke(request)
            },
            onRelease = { _ ->
                holder.releaseIfNeeded()
            },
        )
    }
}
