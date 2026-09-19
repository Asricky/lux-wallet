package com.luxwallet.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Null for a top-level (main) category; non-null for a subcategory. */
    val parentId: Long? = null,
    val isDefault: Boolean = false,
    val isIncomeCategory: Boolean = false,
    val icon: String? = null,
    val colorToken: String? = null
)
