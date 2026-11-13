import Foundation
import PushKit
import Shared
import UIKit

/**
 * iOS 推送通知委托实现（1:1 透传 PushKit-iOS 原生 SDK，并在宿主生命周期同步系统权限与 APNs Token）。
 */
internal final class IosPushDelegate: NSObject, PushDelegate, @unchecked Sendable {
    private let lock = NSLock()
    private var cachedToken: String?
    private var cachedPermissionGranted = false
    private var activeObserver: NSObjectProtocol?

    deinit {
        if let activeObserver {
            NotificationCenter.default.removeObserver(activeObserver)
        }
    }

    func doInit(registerFcm: Bool) {
        guard Bundle.main.bundleIdentifier != nil else { return }

        PushKitManager.shared.setup()
        PushKitManager.shared.delegate.willPresentNotificationHandler = { _ in
            return [.banner, .list, .sound, .badge]
        }
        PushKitManager.shared.delegate.didReceiveNotificationResponseHandler = { response in
            let actionId = response.actionIdentifier
            let title = response.notification.request.content.title
            CoreLog.shared.i(
                tag: LogTags.shared.PUSH,
                message: "[Push-iOS] 用户点击通知 action=\(actionId), title=\(title)"
            )
        }
        PushKitManager.shared.apnsProvider.setMessageListener { message, provider in
            CoreLog.shared.i(
                tag: LogTags.shared.PUSH,
                message: "[Push-iOS] 收到推送消息 (\(provider)): id=\(message.id)"
            )
        }

        refreshPermissionStatusAsync()

        lock.lock()
        let shouldRegisterObserver = activeObserver == nil
        lock.unlock()
        if shouldRegisterObserver {
            let observer = NotificationCenter.default.addObserver(
                forName: UIApplication.didBecomeActiveNotification,
                object: nil,
                queue: .main
            ) { [weak self] _ in
                self?.refreshPermissionStatusAsync()
            }
            lock.lock()
            activeObserver = observer
            lock.unlock()
        }
    }

    func registerPush() {
        guard Bundle.main.bundleIdentifier != nil else {
            CoreLog.shared.w(tag: LogTags.shared.PUSH, message: "[Push-iOS] 当前运行环境无 Bundle ID，跳过通知权限请求")
            return
        }

        Task {
            do {
                let granted = try await PushKitManager.shared.requestPermission()
                lock.lock()
                cachedPermissionGranted = granted
                lock.unlock()
                CoreLog.shared.i(tag: LogTags.shared.PUSH, message: "[Push-iOS] 通知权限请求结果 granted=\(granted)")

                if granted {
                    PushKitManager.shared.apnsProvider.register { [weak self] token, provider in
                        self?.storeToken(token)
                        CoreLog.shared.i(
                            tag: LogTags.shared.PUSH,
                            message: "[Push-iOS] 收到 \(provider) Token (len=\(token.count))"
                        )
                    }
                }
            } catch {
                CoreLog.shared.w(
                    tag: LogTags.shared.PUSH,
                    message: "[Push-iOS] 请求通知权限失败: \(error.localizedDescription)"
                )
            }
        }
    }

    func setPushToken(token: String) {
        storeToken(token)
        CoreLog.shared.d(tag: LogTags.shared.PUSH, message: "[Push-iOS] Push Token updated (len=\(token.count))")
    }

    func getPushToken() -> String? {
        if let providerToken = PushKitManager.shared.apnsProvider.getToken(), !providerToken.isEmpty {
            return providerToken
        }
        lock.lock()
        defer { lock.unlock() }
        return cachedToken
    }

    func hasPushPermission() -> Bool {
        refreshPermissionStatusAsync()
        lock.lock()
        defer { lock.unlock() }
        return cachedPermissionGranted
    }

    private func storeToken(_ token: String) {
        lock.lock()
        cachedToken = token
        lock.unlock()
    }

    private func refreshPermissionStatusAsync() {
        guard Bundle.main.bundleIdentifier != nil else { return }
        Task {
            let granted = await PushKitManager.shared.hasNotificationPermission()
            lock.lock()
            cachedPermissionGranted = granted
            lock.unlock()
        }
    }
}
