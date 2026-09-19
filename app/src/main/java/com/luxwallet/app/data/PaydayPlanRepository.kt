package com.luxwallet.app.data

import com.luxwallet.app.core.database.dao.PaydayPlanDao
import com.luxwallet.app.core.database.entity.PaydayPlanEntity
import com.luxwallet.app.engine.PaydayPlan
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PaydayPlanRepository(private val dao: PaydayPlanDao) {
    private val json = Json { ignoreUnknownKeys = true }
    val plans = dao.observeAll().map { rows -> rows.map { json.decodeFromString<PaydayPlan>(it.payload) } }
    suspend fun save(plan: PaydayPlan) {
        plan.validate()
        dao.upsert(PaydayPlanEntity(plan.startDay, plan.capturedAt, json.encodeToString(plan)))
    }
}
