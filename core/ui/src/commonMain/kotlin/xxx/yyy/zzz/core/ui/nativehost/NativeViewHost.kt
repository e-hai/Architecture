package xxx.yyy.zzz.core.ui.nativehost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * 跨平台局部原生视图渲染请求描述符。
 *
 * @param viewType 原生视图类型标识（需与双端宿主在 [NativeViewRegistry] 中注册的 Key 一致）
 * @param instanceKey 多实例隔离键（同一列表中渲染多个同类型原生视图时，需传入唯一业务 ID）
 * @param params 透传给双端原生视图的不可变配置参数表
 */
@Immutable
data class NativeViewRequest(
    val viewType: String,
    val instanceKey: String = "",
    val params: ImmutableMap<String, String> = persistentMapOf(),
)

/**
 * 跨平台局部原生视图注册中心。
 *
 * 各双端宿主在冷启动阶段将原生 UI 工厂注册到该注册表；业务层和各纯 `commonMain` Kit
 * 通过 [NativeViewHost] 按 [NativeViewRequest.viewType] 安全挂载与释放，无需感知 `AndroidView`/`UIKitView`。
 */
expect object NativeViewRegistry {
    /**
     * 查询指定 [viewType] 是否已在当前平台注册了原生视图工厂。
     *
     * @param viewType 原生视图类型标识
     */
    fun isRegistered(viewType: String): Boolean

    /**
     * 注销指定 [viewType] 的原生视图工厂。
     *
     * @param viewType 原生视图类型标识
     */
    fun unregister(viewType: String)

    /**
     * 清空所有已注册的原生视图工厂（主要用于单测隔离与环境重置）。
     */
    fun clear()
}

/**
 * 跨平台通用局部原生视图宿主容器。
 *
 * 根据 [request] 查找平台侧注册的工厂，并在组合进入时按 `(viewType, instanceKey)` 独立创建实例，
 * 参数更新时触发 `onUpdate`，离开界面树时自动触发 `onRelease`。
 *
 * @param request 原生视图请求参数
 * @param modifier Compose 布局修饰符
 * @param fallback 当当前平台未注册对应 [NativeViewRequest.viewType] 时的兜底占位视图
 */
@Composable
fun NativeViewHost(
    request: NativeViewRequest,
    modifier: Modifier = Modifier,
    fallback: @Composable (NativeViewRequest) -> Unit = { req ->
        DefaultNativeViewFallback(request = req)
    },
) {
    PlatformNativeViewHost(
        request = request,
        modifier = modifier,
        fallback = fallback,
    )
}

@Composable
internal expect fun PlatformNativeViewHost(
    request: NativeViewRequest,
    modifier: Modifier = Modifier,
    fallback: @Composable (NativeViewRequest) -> Unit,
)

@Composable
private fun DefaultNativeViewFallback(
    request: NativeViewRequest,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .testTag("native_view_fallback")
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Unregistered NativeView: ${request.viewType}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
