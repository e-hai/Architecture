import SwiftUI

/// 全局主行动按钮组件。
public struct PrimaryButton: View {
    private let title: String
    private let icon: String?
    private let isLoading: Bool
    private let action: () -> Void

    public init(
        title: String,
        icon: String? = nil,
        isLoading: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.icon = icon
        self.isLoading = isLoading
        self.action = action
    }

    public var body: some View {
        Button(action: {
            guard !isLoading else { return }
            action()
        }) {
            HStack(spacing: AppSpacing.sm) {
                if isLoading {
                    ProgressView()
                        .tint(.white)
                } else {
                    if let icon = icon {
                        Image(systemName: icon)
                    }
                    Text(title)
                }
            }
            .font(.body.weight(.semibold))
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(AppColors.primary)
            .foregroundColor(.white)
            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md))
        }
        .disabled(isLoading)
    }
}
