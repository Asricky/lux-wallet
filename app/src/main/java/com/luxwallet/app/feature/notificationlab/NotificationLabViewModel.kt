package com.luxwallet.app.feature.notificationlab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.NotificationObservationEntity
import com.luxwallet.app.data.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** PRD §39: inspect raw notification fields, parser output/confidence, dedup/matching outcome. */
class NotificationLabViewModel(notificationRepository: NotificationRepository) : ViewModel() {
    val observations: StateFlow<List<NotificationObservationEntity>> = notificationRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun create(app: LuxWalletApp) = NotificationLabViewModel(app.notificationRepository)
    }
}
