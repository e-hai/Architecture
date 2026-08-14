package xxx.yyy.zzz.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xxx.yyy.zzz.core.database.model.VideoEntity

/**
 * 短视频缓存与本地数据访问接口 (DAO)。
 */
@Dao
interface VideoDao {
    /**
     * 响应式观察所有缓存的短视频列表。
     */
    @Query("SELECT * FROM videos ORDER BY createdAtEpochMillis DESC")
    fun getVideosFlow(): Flow<List<VideoEntity>>

    /**
     * 根据视频 ID 查询单个视频。
     *
     * @param id 视频 ID
     * @return 对应的视频实体，不存在时返回 null
     */
    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): VideoEntity?

    /**
     * 批量插入或替换视频。
     *
     * @param videos 视频实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    /**
     * 插入或更新单个视频。
     *
     * @param video 视频实体
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    /**
     * 更新指定视频的点赞状态与点赞数。
     *
     * @param id 视频 ID
     * @param isLiked 是否已点赞
     * @param likeCount 点赞总数
     */
    @Query("UPDATE videos SET isLiked = :isLiked, likeCount = :likeCount WHERE id = :id")
    suspend fun updateLikeState(
        id: String,
        isLiked: Boolean,
        likeCount: Long,
    )

    /**
     * 更新指定视频的收藏状态与收藏数。
     *
     * @param id 视频 ID
     * @param isBookmarked 是否已收藏
     * @param bookmarkCount 收藏总数
     */
    @Query("UPDATE videos SET isBookmarked = :isBookmarked, bookmarkCount = :bookmarkCount WHERE id = :id")
    suspend fun updateBookmarkState(
        id: String,
        isBookmarked: Boolean,
        bookmarkCount: Long,
    )

    /**
     * 清空所有短视频缓存。
     */
    @Query("DELETE FROM videos")
    suspend fun clearVideos()
}
