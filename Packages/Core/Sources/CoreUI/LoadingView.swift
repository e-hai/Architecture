import SwiftUI

/// 全局通用 Loading 视图组件。
public struct LoadingView: View {
    private let text: String

    public init(text: String = "加载中...") {
        self.text = text
    }

    public var body: some View {
        VStack(spacing: AppSpacing.md) {
            ProgressView()
                .scaleEffect(1.2)
            if !text.isEmpty {
                Text(text)
                    .font(.footnote)
                    .foregroundColor(AppColors.textSecondary)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
