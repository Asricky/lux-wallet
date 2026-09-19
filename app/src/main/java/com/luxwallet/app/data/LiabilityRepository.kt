package com.luxwallet.app.data

import com.luxwallet.app.core.database.dao.LiabilityDao
import com.luxwallet.app.core.database.entity.LiabilityEntity
import kotlinx.coroutines.flow.Flow

class LiabilityRepository(private val liabilityDao: LiabilityDao) {
    fun observeAll(): Flow<List<LiabilityEntity>> = liabilityDao.observeAll()
    fun observeTotalOutstanding(): Flow<Long> = liabilityDao.observeTotalOutstanding()
    suspend fun upsert(liability: LiabilityEntity): Long =
        if (liability.id == 0L) liabilityDao.insert(liability) else { liabilityDao.update(liability); liability.id }
    suspend fun delete(liability: LiabilityEntity) = liabilityDao.delete(liability)
}
