import SwiftUI

#if canImport(UIKit)
import UIKit
#elseif canImport(AppKit)
import AppKit
#endif

/// 设计系统全局颜色 Token。
public enum AppColors {
    public static let primary = Color.accentColor

    #if canImport(UIKit)
    public static let background = Color(uiColor: .systemBackground)
    public static let secondaryBackground = Color(uiColor: .secondarySystemBackground)
    public static let tertiaryBackground = Color(uiColor: .tertiarySystemBackground)
    
    public static let textPrimary = Color(uiColor: .label)
    public static let textSecondary = Color(uiColor: .secondaryLabel)
    public static let textMuted = Color(uiColor: .tertiaryLabel)
    #else
    public static let background = Color(nsColor: .windowBackgroundColor)
    public static let secondaryBackground = Color(nsColor: .controlBackgroundColor)
    public static let tertiaryBackground = Color(nsColor: .underPageBackgroundColor)
    
    public static let textPrimary = Color(nsColor: .labelColor)
    public static let textSecondary = Color(nsColor: .secondaryLabelColor)
    public static let textMuted = Color(nsColor: .tertiaryLabelColor)
    #endif

    public static let error = Color.red
    public static let success = Color.green
    public static let warning = Color.orange
}

/// 全局圆角规范
public enum AppRadius {
    public static let sm: CGFloat = 6
    public static let md: CGFloat = 12
    public static let lg: CGFloat = 18
    public static let full: CGFloat = 999
}

/// 全局间距规范
public enum AppSpacing {
    public static let xs: CGFloat = 4
    public static let sm: CGFloat = 8
    public static let md: CGFloat = 16
    public static let lg: CGFloat = 24
    public static let xl: CGFloat = 32
}
