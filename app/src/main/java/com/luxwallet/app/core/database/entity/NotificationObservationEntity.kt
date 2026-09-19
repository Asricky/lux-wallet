package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.luxwallet.app.core.model.ParseStatus
import com.luxwallet.app.core.model.SourceApp

@Entity(
    tableName = "notification_observations",
    indices = [
        Index(value = ["notificationKey"]),
        Index(value = ["rawPayloadHash"]),
        Index(value = ["parseStatus"]),
        Index(value = ["postedAt"])
    ]
)
data class NotificationObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceApp: SourceApp,
    val packageName: String,
    val notificationKey: String?,
    val title: String,
    val text: String,
    val bigText: String?,
    val subText: String?,
    /** Multi-line notification body lines (e.g. InboxStyle), stored newline-joined. */
    val textLines: String?,
    val postedAt: Long,
    val receivedAt: Long,
    val rawPayloadHash: String,
    val parserVersion: Int,
    val parseStatus: ParseStatus,
    val parseFailureReason: String? = null,
    val linkedTransactionId: Long? = null,
    val eventTime: Long? = null,
    val contentHash: String? = null
)
