package xxx.yyy.zzz.feature.debug.api

import androidx.compose.runtime.Composable

/**
 * 调试控制台可插拔分区归类。
 */
enum class DebugCategory(
    val title: String,
) {
    MEDIA_ANIMATION("2. 动效与媒体渲染 (CorePag)"),
    KITS("3. 商业化与基础设施 Kit 联调实验室"),
    SANDBOX("4. 弱网模拟与本地存储沙盒"),
}

/**
 * 调试控制台可插拔区块契约。
 * 各基础设施或 Kit 的调试面板均通过实现本接口并注册到 Koin，
 * 保证增删任意 Kit 时无需修改 [DebugScreen] 主容器。
 */
interface DebugSection {
    /** 区块唯一标识（如 pag / video / ads / infra） */
    val id: String

    /** 所属调试分类 */
    val category: DebugCategory

    /** 同分类下的展示顺序，数值越小越靠前 */
    val order: Int

    /**
     * 渲染该区块的调试交互 UI。
     *
     * @param emitLog 统一日志回调（写入屏幕控制台回显并透传至 CoreLog）
     */
    @Composable
    fun Content(emitLog: (tag: String, message: String, level: String) -> Unit)
}
