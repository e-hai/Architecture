import Foundation

/// 集中管理跨模块远程配置 Key 与默认值。
public enum AbTestingKeys {
    public static let enableNewFeedLayout = "enable_new_feed_layout"
    public static let bannerAutoScrollInterval = "banner_auto_scroll_interval"

    nonisolated(unsafe) public static let defaultValues: [String: Any] = [
        enableNewFeedLayout: true,
        bannerAutoScrollInterval: 5
    ]
}
