package xxx.yyy.zzz.core.pag

import io.github.pagkit.cache.PagDiskCache
import io.github.pagkit.model.PagFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 跨平台静态 PAG 动效门面（CorePag）。
 * 集中管理全局缓存池、字体注册与异步预加载。
 */
object CorePag {
    private var isInitialized = false

    /**
     * 初始化 PAG 动效引擎与全局缓存（使用默认 256MB 磁盘缓存）。
     */
    fun doInit() {
        doInit(256L * 1024 * 1024)
    }

    /**
     * 初始化 PAG 动效引擎与全局缓存。
     *
     * @param maxDiskCacheBytes 最大磁盘缓存字节数
     */
    fun doInit(maxDiskCacheBytes: Long) {
        runCatching { PagDiskCache.setMaxDiskSize(maxDiskCacheBytes) }
        isInitialized = true
    }

    /**
     * 异步后台预热并下载网络 PAG 动效至本地磁盘缓存。
     *
     * @param url 网络动效地址
     * @param onComplete 完成回调（主线程或协程上下文调度）
     */
    fun preload(
        url: String,
        onComplete: ((Boolean) -> Unit)? = null,
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            val file = PagDiskCache.loadFromUrl(url)
            onComplete?.invoke(file != null)
        }
    }

    /**
     * 注册自定义字体（支持 TTF / OTF 格式）。
     *
     * @param fontPath 本地文件路径或 Asset 路径
     * @param fontFamily 目标字体族名称
     * @param fontStyle 字体样式
     * @return 注册是否成功
     */
    fun registerFont(
        fontPath: String,
        fontFamily: String = "",
        fontStyle: String = "",
    ): Boolean =
        if (fontFamily.isNotEmpty() || fontStyle.isNotEmpty()) {
            PagFont.registerFont(fontPath, fontFamily, fontStyle) != null
        } else {
            PagFont.registerFont(fontPath) != null
        }

    /**
     * 清空全部 PAG 本地磁盘缓存。
     */
    fun clearCache() {
        runCatching { PagDiskCache.removeAll() }
    }

    /**
     * 检查 CorePag 引擎是否已完成初始化。
     */
    fun isReady(): Boolean = isInitialized
}
