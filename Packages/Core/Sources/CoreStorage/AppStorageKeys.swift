import Foundation

/// 集中管理跨模块持久化 Key。
public enum AppStorageKeys {
    public static let userToken = "storage.key.user_token"
    public static let userId = "storage.key.user_id"
    public static let isFirstLaunch = "storage.key.is_first_launch"
    public static let selectedTheme = "storage.key.selected_theme"
}
