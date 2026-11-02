import SwiftUI
import CoreUI
import CoreNavigation
import CoreAnalytics
import CoreLog

/// 顶层应用容器视图。
/// 托管全局统一导航栈与基础模态控制器。
public struct RootView: View {
    @StateObject private var router = AppRouter()

    public init() {}

    public var body: some View {
        NavigationStack(path: $router.path) {
            ScrollView {
                VStack(spacing: AppSpacing.lg) {
                    // 1. 骨架架构标头
                    VStack(spacing: AppSpacing.xs) {
                        Image(systemName: "square.stack.3d.up.fill")
                            .font(.system(size: 48))
                            .foregroundColor(AppColors.primary)
                            .padding(.bottom, AppSpacing.xs)

                        Text("iOS 现代模块化架构")
                            .font(.title2.bold())
                            .foregroundColor(AppColors.textPrimary)

                        Text("SwiftUI · Swift 6 · SPM 三级模块化 · 离线优先")
                            .font(.caption)
                            .foregroundColor(AppColors.textSecondary)
                    }
                    .padding(.top, AppSpacing.xl)

                    // 2. 核心架构特性卡片
                    VStack(alignment: .leading, spacing: AppSpacing.sm) {
                        Text("架构规范与就绪能力")
                            .font(.footnote.weight(.semibold))
                            .foregroundColor(AppColors.textSecondary)
                            .padding(.horizontal, AppSpacing.xs)

                        FeatureItemRow(
                            icon: "point.3.filled.connected.trianglepath.dotted",
                            title: "三级模块化规范",
                            subtitle: "App → Features (Interface + Impl) → Core"
                        )

                        FeatureItemRow(
                            icon: "arrow.triangle.swap",
                            title: "轻量依赖注入 (CoreDI)",
                            subtitle: "基于协议与 @Injected 原生属性包装器"
                        )

                        FeatureItemRow(
                            icon: "shippingbox.fill",
                            title: "统一商业化 Kit 门面",
                            subtitle: "Analytics, Ads, Pay (StoreKit 2), Push, Log"
                        )

                        FeatureItemRow(
                            icon: "network",
                            title: "现代并发网络与存储",
                            subtitle: "URLSession async/await + 离线优先策略"
                        )
                    }
                    .padding(.horizontal, AppSpacing.md)

                    // 3. 体验交互按钮
                    PrimaryButton(
                        title: "打点统计测试 (Analytics)",
                        icon: "waveform.path.ecg"
                    ) {
                        Analytics.logEvent(AnalyticsEvents.appOpen, parameters: [
                            AnalyticsParams.source: "root_view"
                        ])
                    }
                    .padding(.horizontal, AppSpacing.md)
                }
                .padding(.bottom, AppSpacing.xl)
            }
            .background(AppColors.background)
            .navigationTitle("架构骨架")
            #if canImport(UIKit)
            .navigationBarTitleDisplayMode(.inline)
            #endif
        }
        .environmentObject(router)
    }
}

/// 特性清单行视图组件
private struct FeatureItemRow: View {
    let icon: String
    let title: String
    let subtitle: String

    var body: some View {
        HStack(spacing: AppSpacing.md) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(AppColors.primary)
                .frame(width: 32, height: 32)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.body.weight(.medium))
                    .foregroundColor(AppColors.textPrimary)

                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(AppColors.textSecondary)
            }

            Spacer()
        }
        .padding(AppSpacing.md)
        .background(AppColors.secondaryBackground)
        .clipShape(RoundedRectangle(cornerRadius: AppRadius.md))
    }
}
