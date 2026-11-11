package xxx.yyy.zzz.core.video

/**
 * 跨平台视频播放与缓存委托协议。
 * 约束：仅允许在该接口的各端宿主 Delegate 内部调用原生 VideoKit SDK。
 */
interface VideoDelegate {
    /**
     * 初始化视频引擎配置与全局磁盘缓存。
     */
    fun doInit()

    /**
     * 异步后台预加载指定视频前 N 字节（默认 2MB）。
     *
     * @param url 视频网络资源地址
     * @param bytes 预加载字节数阈值
     */
    fun preload(
        url: String,
        bytes: Long = 2L * 1024 * 1024,
    )

    /**
     * 工业级双向预加载队列（优先预加载下一个，紧接着预加载上一个）。
     *
     * @param prevUrl 上一个视频 URL
     * @param nextUrl 下一个视频 URL
     * @param bytes 预加载字节数阈值
     */
    fun preloadBidirectional(
        prevUrl: String?,
        nextUrl: String?,
        bytes: Long = 2L * 1024 * 1024,
    )

    /**
     * 获取指定视频 URL 在本地磁盘已缓存的字节数。
     *
     * @param url 视频网络资源地址
     * @return 已缓存的字节数
     */
    fun getCachedBytes(url: String): Long

    /**
     * 检查某个视频 URL 是否已缓存（至少达到指定字节阈值）。
     *
     * @param url 视频网络资源地址
     * @param minBytes 最小字节判定阈值
     */
    fun isCached(
        url: String,
        minBytes: Long = 1L,
    ): Boolean

    /**
     * 清空全部本地视频磁盘缓存。
     */
    fun clearCache()

    /**
     * 保存视频断点记忆播放进度。
     *
     * @param videoId 视频唯一业务标识/URL
     * @param positionMs 播放位置毫秒数
     * @param durationMs 视频总时长毫秒数（用于完播自动重置，默认 0 表示不计算完播重置）
     */
    fun saveProgress(
        videoId: String,
        positionMs: Long,
        durationMs: Long = 0L,
    )

    /**
     * 读取指定视频的历史断点记忆播放进度。
     *
     * @param videoId 视频唯一业务标识/URL
     * @return 历史播放位置毫秒数，若无记录则返回 0
     */
    fun getProgress(videoId: String): Long

    /**
     * 清除指定视频的历史断点记忆播放进度记录。
     *
     * @param videoId 视频唯一业务标识/URL
     */
    fun clearProgress(videoId: String)
}

/**
 * 默认控制台兜底实现。
 */
object DefaultVideoDelegate : VideoDelegate {
    override fun doInit() = Unit

    override fun preload(
        url: String,
        bytes: Long,
    ) {
        println("[Video] Default preload (NoOp): url=$url, bytes=$bytes")
    }

    override fun preloadBidirectional(
        prevUrl: String?,
        nextUrl: String?,
        bytes: Long,
    ) {
        println("[Video] Default preloadBidirectional (NoOp): prev=$prevUrl, next=$nextUrl")
    }

    override fun getCachedBytes(url: String): Long = 0L

    override fun isCached(
        url: String,
        minBytes: Long,
    ): Boolean = false

    override fun clearCache() {
        println("[Video] Default clearCache (NoOp)")
    }

    override fun saveProgress(
        videoId: String,
        positionMs: Long,
        durationMs: Long,
    ) {
        println("[Video] Default saveProgress (NoOp): videoId=$videoId, pos=$positionMs, duration=$durationMs")
    }

    override fun getProgress(videoId: String): Long = 0L

    override fun clearProgress(videoId: String) {
        println("[Video] Default clearProgress (NoOp): videoId=$videoId")
    }
}

/**
 * 跨平台静态视频门面（CoreVideo）。
 * 遵循 KMP 架构规范，统一通过 install 注入双端宿主原生实现。
 */
object CoreVideo : VideoDelegate {
    private var delegate: VideoDelegate = DefaultVideoDelegate

    /**
     * 挂载平台特定的视频委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的视频委托接口
     */
    fun install(delegate: VideoDelegate) {
        this.delegate = delegate
    }

    override fun doInit() = delegate.doInit()

    override fun preload(
        url: String,
        bytes: Long,
    ) = delegate.preload(url, bytes)

    override fun preloadBidirectional(
        prevUrl: String?,
        nextUrl: String?,
        bytes: Long,
    ) = delegate.preloadBidirectional(prevUrl, nextUrl, bytes)

    override fun getCachedBytes(url: String): Long = delegate.getCachedBytes(url)

    override fun isCached(
        url: String,
        minBytes: Long,
    ): Boolean = delegate.isCached(url, minBytes)

    override fun clearCache() = delegate.clearCache()

    override fun saveProgress(
        videoId: String,
        positionMs: Long,
        durationMs: Long,
    ) = delegate.saveProgress(videoId, positionMs, durationMs)

    override fun getProgress(videoId: String): Long = delegate.getProgress(videoId)

    override fun clearProgress(videoId: String) = delegate.clearProgress(videoId)
}
