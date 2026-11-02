import Foundation

/// 基础数据项领域模型。
public struct Item: Identifiable, Sendable, Codable, Hashable {
    public let id: String
    public var title: String
    public var detail: String?
    public var updatedAt: Date

    public init(
        id: String,
        title: String,
        detail: String? = nil,
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.title = title
        self.detail = detail
        self.updatedAt = updatedAt
    }
}
