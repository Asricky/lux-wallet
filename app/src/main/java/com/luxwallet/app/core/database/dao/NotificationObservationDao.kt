package com.luxwallet.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.luxwallet.app.core.database.entity.NotificationObservationEntity
import com.luxwallet.app.core.model.ParseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationObservationDao {
    @Insert
    suspend fun insert(observation: NotificationObservationEntity): Long

    @Update
    suspend fun update(observation: NotificationObservationEntity)

    @Query("SELECT * FROM notification_observations WHERE id = :id")
    suspend fun getById(id: Long): NotificationObservationEntity?

    @Query("SELECT * FROM notification_observations WHERE rawPayloadHash = :hash LIMIT 1")
    suspend fun findByHash(hash: String): NotificationObservationEntity?

    @Query("SELECT * FROM notification_observations WHERE parseStatus = :status ORDER BY postedAt ASC")
    suspend fun getByStatus(status: ParseStatus): List<NotificationObservationEntity>

    @Query("SELECT * FROM notification_observations ORDER BY postedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<NotificationObservationEntity>>

    /** For the Notification Lab (PRD §39): every observation, regardless of parse outcome. */
    @Query("SELECT * FROM notification_observations ORDER BY postedAt DESC")
    fun observeAll(): Flow<List<NotificationObservationEntity>>

    @Query("DELETE FROM notification_observations WHERE postedAt < :beforeTimestamp AND linkedTransactionId IS NOT NULL")
    suspend fun purgeProcessedOlderThan(beforeTimestamp: Long): Int
}
