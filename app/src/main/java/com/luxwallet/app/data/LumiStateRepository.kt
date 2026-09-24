package com.luxwallet.app.data

import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.ui.component.LumiMood
import com.luxwallet.app.engine.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class LumiStateRepository(app: LuxWalletApp) {
    // In-process clock expires recent-event moods. It creates no alarm or background polling work.
    private val clock = flow { while (true) { emit(System.currentTimeMillis()); kotlinx.coroutines.delay(60_000) } }
    private val inputs = combine(app.paydayPlanRepository.plans, app.transactionRepository.observeAll(),
        app.categoryRepository.observeAll(), app.goalRepository.observeAll()) { plans, txs, categories, goals ->
        { now: Long -> LumiCondition.evaluate(plans, txs,
            categories.filter { it.name == com.luxwallet.app.core.common.TransportPlan.CATEGORY }.map { it.id }.toSet(),
            categories.filter { it.name == PaydayPlan.BILLS_CATEGORY }.map { it.id }.toSet(), goals, now, LocalDate.now()) }
    }
    val mood = combine(inputs, clock, app.notificationRepository.observeAll()) { evaluate, now, observations ->
        val result = evaluate(now)
        if (observations.any { it.parseStatus == com.luxwallet.app.core.model.ParseStatus.FAILED } && result !in setOf(LumiMood.NERVOUS, LumiMood.SAD, LumiMood.ANGRY)) LumiMood.CURIOUS else result
    }
        .distinctUntilChanged().stateIn(app.applicationScope, SharingStarted.Eagerly, LumiMood.CALM)
}
