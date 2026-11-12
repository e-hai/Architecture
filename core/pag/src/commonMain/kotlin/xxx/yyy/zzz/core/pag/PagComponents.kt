package xxx.yyy.zzz.core.pag

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.pagkit.compose.PagImageView as SdkPagImageView
import io.github.pagkit.compose.PagView as SdkPagView
import io.github.pagkit.compose.rememberPagState as sdkRememberPagState
import io.github.pagkit.model.PagSource as SdkPagSource

/**
 * 统一对上层暴露的 PAG 数据源类型（支持 Asset / File / Url / Bytes）。
 */
typealias PagSource = io.github.pagkit.model.PagSource

/**
 * 统一对上层暴露的 PAG 组合模型。
 */
typealias PagComposition = io.github.pagkit.composition.PagComposition

/**
 * 跨平台 PAG 数据源工厂门面，避免上层模块直接依赖 `io.github.pagkit.model.PagSource` 子类。
 */
object PagSources {
    /** 从平台内置 Assets（Android assets / iOS Main Bundle 资源）加载 */
    fun asset(assetName: String): PagSource = SdkPagSource.Asset(assetName)

    /** 从本地文件绝对路径加载 */
    fun filePath(path: String): PagSource = SdkPagSource.FilePath(path)

    /** 从网络 URL 异步下载并自动缓存加载 */
    fun url(url: String): PagSource = SdkPagSource.Url(url)

    /** 从内存二进制数组加载 */
    fun bytes(
        bytes: ByteArray,
        cacheKey: String = bytes.contentHashCode().toString(),
    ): PagSource = SdkPagSource.Bytes(bytes, cacheKey)

    /** 直接挂载已构建好的 PagComposition */
    fun composition(composition: PagComposition): PagSource = SdkPagSource.Composition(composition)
}

/**
 * 统一对上层暴露的 PAG 缩放模式枚举。
 */
typealias PagScaleMode = io.github.pagkit.model.PagScaleMode

/**
 * 统一对上层暴露的 PAG 播放状态控制器。
 */
typealias PagState = io.github.pagkit.compose.PagState

/**
 * 统一对上层暴露的 PAG 文件模型（支持替换文本/图片图层）。
 */
typealias PagFile = io.github.pagkit.composition.PagFile

/**
 * 在 Composable 作用域内创建并记住 [PagState]。
 * 上层业务模块统一通过 `:core:pag` 包调用，避免直接耦合底层 `io.github.pagkit` SDK。
 */
@Composable
fun rememberPagState(
    source: PagSource? = null,
    autoPlay: Boolean = true,
    repeatCount: Int = 0,
    scaleMode: PagScaleMode = PagScaleMode.LetterBox,
    maxFrameRate: Float = 60f,
): PagState =
    sdkRememberPagState(
        source = source,
        autoPlay = autoPlay,
        repeatCount = repeatCount,
        scaleMode = scaleMode,
        maxFrameRate = maxFrameRate,
    )

/**
 * 跨平台高性能 GPU 实时渲染 PAG 动效视图门面包装。
 */
@Composable
fun PagView(
    state: PagState,
    modifier: Modifier = Modifier,
    sync: Boolean = false,
    videoEnabled: Boolean = true,
    cacheEnabled: Boolean = true,
    cacheScale: Float = 1.0f,
    onCompositionLoaded: ((PagFile) -> Unit)? = null,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null,
    onAnimationCancel: (() -> Unit)? = null,
    onAnimationRepeat: (() -> Unit)? = null,
    onAnimationUpdate: ((progress: Double) -> Unit)? = null,
) {
    SdkPagView(
        state = state,
        modifier = modifier,
        sync = sync,
        videoEnabled = videoEnabled,
        cacheEnabled = cacheEnabled,
        cacheScale = cacheScale,
        onCompositionLoaded = onCompositionLoaded,
        onAnimationStart = onAnimationStart,
        onAnimationEnd = onAnimationEnd,
        onAnimationCancel = onAnimationCancel,
        onAnimationRepeat = onAnimationRepeat,
        onAnimationUpdate = onAnimationUpdate,
    )
}

/**
 * 跨平台轻量序列帧位图缓存动效组件包装（适用于列表中高频复用的小型动效）。
 */
@Composable
fun PagImageView(
    state: PagState,
    modifier: Modifier = Modifier,
    cacheAllFramesInMemory: Boolean = false,
    renderScale: Float = 1.0f,
    onCompositionLoaded: ((PagFile) -> Unit)? = null,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null,
    onAnimationCancel: (() -> Unit)? = null,
    onAnimationRepeat: (() -> Unit)? = null,
    onAnimationUpdate: ((progress: Double) -> Unit)? = null,
) {
    SdkPagImageView(
        state = state,
        modifier = modifier,
        cacheAllFramesInMemory = cacheAllFramesInMemory,
        renderScale = renderScale,
        onCompositionLoaded = onCompositionLoaded,
        onAnimationStart = onAnimationStart,
        onAnimationEnd = onAnimationEnd,
        onAnimationCancel = onAnimationCancel,
        onAnimationRepeat = onAnimationRepeat,
        onAnimationUpdate = onAnimationUpdate,
    )
}
