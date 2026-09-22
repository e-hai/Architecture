package com.shortvideo.app.core.log

import android.content.Context
import java.io.File

/**
 * 日志文件自愈与清洗工具。
 *
 * 专门解决因进程异常中断或文件写入空字节导致的日志损坏问题：
 * 当 CSV 首字段包含 NUL (\u0000) 或非法字符时，会导致 LogKit 内部 Long.parseLong 抛出 NumberFormatException 并崩溃。
 */
object LogSanitizer {
    private const val LOG_DIR_NAME = "logger"

    /**
     * 校验并清洗损坏的日志文件。
     * 剔除包含空字节或时间戳无法被解析为 Long 的异常行，保证文件格式严格符合 CSV 规范。
     *
     * @param context 应用程序上下文
     * @return 修复/清洗的文件数量
     */
    fun sanitize(context: Context): Int {
        val logDir = File(context.filesDir, LOG_DIR_NAME)
        if (!logDir.exists() || !logDir.isDirectory) return 0

        var fixedCount = 0
        val files = logDir.listFiles { _, name -> name.startsWith("logs") && name.endsWith(".csv") } ?: return 0

        for (file in files) {
            try {
                if (!file.exists() || file.length() == 0L) continue

                val lines = file.readLines()
                var needsRewrite = false
                val sanitizedLines = mutableListOf<String>()

                for (line in lines) {
                    // 清理可能混入的首尾空字符、NUL 字节以及 Unicode 替换符
                    val trimmed = line.trim { it <= ' ' || it == '\u0000' || it == '\uFFFD' }
                    if (trimmed.isEmpty()) {
                        needsRewrite = true
                        continue
                    }

                    val firstPart = trimmed.substringBefore(",")
                    val timestamp = firstPart.toLongOrNull()

                    if (timestamp != null) {
                        // 时间戳有效，若原本有乱码前缀则标记需要重写
                        if (trimmed != line) {
                            needsRewrite = true
                        }
                        sanitizedLines.add(trimmed)
                    } else {
                        // 时间戳无法被解析，说明该行已被破坏，直接丢弃
                        needsRewrite = true
                    }
                }

                if (needsRewrite) {
                    if (sanitizedLines.isEmpty()) {
                        file.delete()
                    } else {
                        file.writeText(sanitizedLines.joinToString("\n") + "\n")
                    }
                    fixedCount++
                }
            } catch (e: Exception) {
                // 若单个文件读取完全损坏，直接删除防止继续崩溃
                try {
                    file.delete()
                    fixedCount++
                } catch (_: Exception) {
                }
            }
        }
        return fixedCount
    }

    /**
     * 清空所有本地日志文件。
     *
     * @param context 应用程序上下文
     * @return 是否清空成功
     */
    fun clearAllLogs(context: Context): Boolean {
        val logDir = File(context.filesDir, LOG_DIR_NAME)
        if (!logDir.exists() || !logDir.isDirectory) return true
        return try {
            logDir.listFiles()?.forEach { it.delete() }
            true
        } catch (e: Exception) {
            false
        }
    }
}
