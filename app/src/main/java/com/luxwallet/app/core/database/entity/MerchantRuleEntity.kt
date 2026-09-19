package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * User-created or learned rule: "merchant name contains X" -> category. Created automatically
 * when a user corrects a transaction's category (PRD §23 "User correction learning"), and
 * editable by the user directly under Settings > Rules.
 */
@Serializable
@Entity(tableName = "merchant_rules")
data class MerchantRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantContains: String,
    val categoryId: Long,
    val subcategoryId: Long? = null,
    val createdAt: Long,
    val isUserCreated: Boolean = true
)
