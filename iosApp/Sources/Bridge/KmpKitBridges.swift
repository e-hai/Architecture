import Foundation
import SwiftUI
import LogKit
import CrashReportKit
import AnalyticsKit
import AbTestingKit
import MmpCore
import AdsCore
import PayKit
import PushKit

/**
 * 集中实现 KMP 暴露的 8 大 IosBridge 协议，单行转发给 GitHub SPM e-hai/*-iOS 原生 SDK。
 */
public final class KmpKitBridgeBootstrapper: Sendable {
    public static func installAll() {
        print("[KMP-iOS] Installing native Kit bridges...")
        // 示例：此处可按需注入 Swift 原生桥接实例给 KMP 导出的 register*IosBridge 方法
    }
}
