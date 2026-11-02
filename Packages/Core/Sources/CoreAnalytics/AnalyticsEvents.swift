import Foundation

/// 集中管理跨模块打点事件名。
public enum AnalyticsEvents {
    public static let appOpen = "app_open"
    public static let screenView = "screen_view"
    public static let userLogin = "user_login"
    public static let userLogout = "user_logout"
    public static let purchaseSuccess = "purchase_success"
    public static let adClick = "ad_click"
    public static let shareContent = "share_content"
}

/// 集中管理跨模块打点参数 Key。
public enum AnalyticsParams {
    public static let screenName = "screen_name"
    public static let screenClass = "screen_class"
    public static let itemId = "item_id"
    public static let itemName = "item_name"
    public static let contentType = "content_type"
    public static let source = "source"
    public static let success = "success"
}
