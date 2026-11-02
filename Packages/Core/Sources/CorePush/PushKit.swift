import Foundation
import UserNotifications
import CoreLog

/// 本地与远程通知管理门面。
public final class PushKitManager: @unchecked Sendable {
    public static let shared = PushKitManager()

    private init() {}

    /// 请求通知权限
    public func requestAuthorization() async throws -> Bool {
        let center = UNUserNotificationCenter.current()
        let options: UNAuthorizationOptions = [.alert, .badge, .sound]
        let granted = try await center.requestAuthorization(options: options)
        LogKit.i("通知权限授权结果: \(granted)", tag: .push)
        return granted
    }

    /// 发送本地测试通知
    public func scheduleLocalNotification(
        identifier: String = UUID().uuidString,
        title: String,
        body: String,
        timeInterval: TimeInterval = 1
    ) async throws {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: timeInterval, repeats: false)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        try await UNUserNotificationCenter.current().add(request)
        LogKit.d("已调度本地通知: \(title)", tag: .push)
    }
}
