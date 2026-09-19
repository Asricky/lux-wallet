package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luxwallet.app.core.model.BudgetKind
import kotlinx.serialization.Serializable

/** [yearMonth] is formatted "yyyy-MM"; one row per (kind, category, month). */
@Serializable
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: BudgetKind,
    val categoryId: Long? = null,
    val yearMonth: String,
    val amount: Long
)
