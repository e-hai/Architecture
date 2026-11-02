import SwiftUI

/// 全局通用错误与重试交互视图组件。
public struct ErrorView: View {
    private let message: String
    private let onRetry: (() -> Void)?

    public init(
        message: String = "加载失败，请重试",
        onRetry: (() -> Void)? = nil
    ) {
        self.message = message
        self.onRetry = onRetry
    }

    public var body: some View {
        VStack(spacing: AppSpacing.md) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 40))
                .foregroundColor(AppColors.warning)

            Text(message)
                .font(.subheadline)
                .foregroundColor(AppColors.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, AppSpacing.lg)

            if let onRetry = onRetry {
                Button(action: onRetry) {
                    Text("重试")
                        .font(.callout.bold())
                        .padding(.horizontal, AppSpacing.lg)
                        .padding(.vertical, AppSpacing.sm)
                        .background(AppColors.primary)
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: AppRadius.md))
                }
                .padding(.top, AppSpacing.xs)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(AppSpacing.lg)
    }
}
