package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luxwallet.app.core.model.AssetClass
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val assetClass: AssetClass,
    val currentValue: Long,
    val acquisitionValue: Long? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val notes: String? = null,
    val updatedAt: Long,
    val includeInNetWorth: Boolean = true,
    /** Optional link to the account this asset mirrors (e.g. a Deposito held "at" a bank account). */
    val linkedAccountId: Long? = null,
    @androidx.room.ColumnInfo(defaultValue = "0") val isArchived: Boolean = false
)
