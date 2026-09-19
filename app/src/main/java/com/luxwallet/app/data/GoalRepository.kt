package com.luxwallet.app.data

import com.luxwallet.app.core.database.dao.GoalDao
import com.luxwallet.app.core.database.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {
    fun observeAll(): Flow<List<GoalEntity>> = goalDao.observeAll()
    suspend fun upsert(goal: GoalEntity): Long =
        if (goal.id == 0L) goalDao.insert(goal) else { goalDao.update(goal); goal.id }
    suspend fun delete(goal: GoalEntity) = goalDao.delete(goal)
}
