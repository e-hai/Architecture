package com.shortvideo.app.core.model

import kotlinx.serialization.Serializable

/**
 * 创作者/用户信息领域模型。
 *
 * @property id 用户唯一标识 ID
 * @property nickname 用户昵称
 * @property avatarUrl 用户头像 URL
 * @property bio 个人简介
 * @property isFollowing 当前登录用户是否已关注该作者
 * @property followerCount 粉丝总数
 * @property followingCount 关注总数
 * @property likeCount 获赞总数
 */
@Serializable
data class Author(
    val id: String,
    val nickname: String,
    val avatarUrl: String,
    val bio: String = "",
    val isFollowing: Boolean = false,
    val followerCount: Long = 0L,
    val followingCount: Long = 0L,
    val likeCount: Long = 0L,
)
