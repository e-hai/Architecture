import Foundation

/// 用户领域模型。
/// 纯 Swift 结构体，零 UI/数据库依赖，支持并发 Sendable 与 Codable 序列化。
public struct User: Identifiable, Sendable, Codable, Hashable {
    /// 用户唯一标识
    public let id: String
    /// 用户昵称
    public var nickname: String
    /// 用户头像 URL
    public var avatarUrl: String?
    /// 个人简介
    public var bio: String?
    /// 注册/创建时间
    public let createdAt: Date

    public init(
        id: String,
        nickname: String,
        avatarUrl: String? = nil,
        bio: String? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.nickname = nickname
        self.avatarUrl = avatarUrl
        self.bio = bio
        self.createdAt = createdAt
    }
}
