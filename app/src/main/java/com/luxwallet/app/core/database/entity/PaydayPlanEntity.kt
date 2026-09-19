package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "payday_plans")
data class PaydayPlanEntity(@PrimaryKey val startDay: Long, val capturedAt: Long, val payload: String)
