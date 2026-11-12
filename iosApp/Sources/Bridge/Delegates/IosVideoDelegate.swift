import Foundation
import VideoKit

#if canImport(Shared)
import Shared

/**
 * iOS 视频播放与缓存委托实现（1:1 透传 VideoKit-iOS 原生 SDK）。
 */
internal final class IosVideoDelegate: NSObject, VideoDelegate {
    func doInit() {
        _ = VideoCacheManager.shared
        print("[IosVideoDelegate] VideoCacheManager initialized.")
    }

    func preload(url: String, bytes: Int64) {
        VideoCacheManager.shared.preload(urlString: url, bytes: bytes)
    }

    func preloadBidirectional(prevUrl: String?, nextUrl: String?, bytes: Int64) {
        VideoCacheManager.shared.preloadBidirectional(prevUrl: prevUrl, nextUrl: nextUrl, bytes: bytes)
    }

    func getCachedBytes(url: String) -> Int64 {
        return VideoCacheManager.shared.getCachedBytes(for: url)
    }

    func isCached(url: String, minBytes: Int64) -> Bool {
        return VideoCacheManager.shared.isCached(for: url, minBytes: minBytes)
    }

    func clearCache() {
        VideoCacheManager.shared.clearAllCache()
        print("[IosVideoDelegate] VideoCacheManager cache cleared.")
    }

    func saveProgress(videoId: String, positionMs: Int64, durationMs: Int64) {
        VodProgressStore.shared.saveProgress(for: videoId, positionSec: Double(positionMs) / 1000.0)
    }

    func getProgress(videoId: String) -> Int64 {
        let seconds = VodProgressStore.shared.getProgress(for: videoId)
        return Int64(seconds * 1000.0)
    }

    func clearProgress(videoId: String) {
        VodProgressStore.shared.clearProgress(for: videoId)
        print("[IosVideoDelegate] VodProgressStore cleared for: \(videoId)")
    }
}
#endif
