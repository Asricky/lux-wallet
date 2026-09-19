package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luxwallet.app.core.model.LiabilityType
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "liabilities")
data class LiabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: LiabilityType,
    val currentOutstanding: Long,
    val monthlyPayment: Long? = null,
    val dueDate: Long? = null,
    val interestRate: Double? = null,
    val notes: String? = null,
    val updatedAt: Long
)
