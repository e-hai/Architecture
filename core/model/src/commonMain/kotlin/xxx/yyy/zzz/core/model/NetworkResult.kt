package xxx.yyy.zzz.core.model

/**
 * 跨平台通用业务结果封装，用于单向数据流与领域层异常安全。
 */
sealed interface NetworkResult<out T> {
    /**
     * 请求成功结果封装。
     *
     * @property data 承载的数据实体
     */
    data class Success<T>(
        val data: T,
    ) : NetworkResult<T>

    /**
     * 请求失败结果封装。
     *
     * @property exception 业务或网络异常
     * @property message 错误提示文案
     */
    data class Error(
        val exception: Throwable? = null,
        val message: String = "",
    ) : NetworkResult<Nothing>

    /**
     * 加载中状态。
     */
    data object Loading : NetworkResult<Nothing>
}
