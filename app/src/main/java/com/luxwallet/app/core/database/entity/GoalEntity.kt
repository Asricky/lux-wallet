package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long = 0,
    val targetDate: Long? = null,
    val priority: Int = 0,
    val linkedAccountId: Long? = null,
    val createdAt: Long,
    val isAchieved: Boolean = false
)
