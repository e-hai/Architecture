@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.shared.performance

// [KIT:analytics:START]
import xxx.yyy.zzz.core.analytics.AnalyticsEvents
import xxx.yyy.zzz.core.analytics.CoreAnalytics
// [KIT:analytics:END]
// [KIT:log:START]
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
// [KIT:log:END]
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * 跨平台冷启动耗时基线追踪器（StartupPerformanceTracker）。
 *
 * 追踪双端冷启动两个关键阶段并幂等上报至 [CoreAnalytics] 与 [CoreLog]：
 * 1. `stage1_ms`：进程启动入口（`MyApplication.onCreate` / `iOSApp.init`）至宿主 Stage 1 + Koin 装配完成耗时；
 * 2. `ttid_ms`（Time To Initial Display）：进程启动至 CMP 根视图 [App] 完成首帧挂载耗时。
 */
object StartupPerformanceTracker {
    private var timeSource: TimeSource = TimeSource.Monotonic
    private var startMark: TimeMark? = null
    private var stage1ElapsedMs: Long? = null
    private var platformName: String = "unknown"
    private var reportedFirstFrame: Boolean = false

    /**
     * 标记进程冷启动入口开始计时（仅首次调用生效，幂等安全）。
     *
     * @param platform 平台标识（如 `android` / `ios`）
     */
    fun markProcessStart(platform: String) {
        if (startMark == null) {
            platformName = platform
            startMark = timeSource.markNow()
        }
    }

    /**
     * 标记宿主 Stage 1 必需 Kit 与纯 KMP Koin 容器初始化完成节点。
     */
    fun markStage1EssentialReady() {
        val mark = startMark ?: return
        if (stage1ElapsedMs == null) {
            stage1ElapsedMs = mark.elapsedNow().inWholeMilliseconds
        }
    }

    /**
     * 标记 Compose 首帧渲染完成并上报冷启动耗时埋点（仅首次调用触发上报）。
     *
     * @return 若本次实际完成首帧上报则返回 `true`，已上报或尚未开始计时则返回 `false`
     */
    fun markFirstFrameRendered(): Boolean {
        val mark = startMark
        if (mark == null || reportedFirstFrame) return false
        reportedFirstFrame = true

        val ttidMs = mark.elapsedNow().inWholeMilliseconds
        val stage1Ms = stage1ElapsedMs ?: ttidMs
        // [KIT:analytics:START]
        CoreAnalytics.logEvent(
            AnalyticsEvents.App.COLD_START,
            mapOf(
                "platform" to platformName,
                "ttid_ms" to ttidMs,
                "stage1_ms" to stage1Ms,
            ),
        )
        // [KIT:analytics:END]
        // [KIT:log:START]
        CoreLog.i(
            LogTags.APP,
            "[Perf] Cold start completed ($platformName): ttid=${ttidMs}ms, stage1=${stage1Ms}ms",
        )
        // [KIT:log:END]
        return true
    }

    internal fun resetForTesting(customTimeSource: TimeSource = TimeSource.Monotonic) {
        timeSource = customTimeSource
        startMark = null
        stage1ElapsedMs = null
        platformName = "unknown"
        reportedFirstFrame = false
    }
}
