package com.luxwallet.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.model.ParseStatus
import com.luxwallet.app.parser.core.NotificationInput
import com.luxwallet.app.parser.core.ParseResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Drains PENDING [com.luxwallet.app.core.database.entity.NotificationObservationEntity] rows:
 * parses each with the source-specific parser, then either ingests it as a transaction
 * (PRD §7), marks it as not-financial (promo notifications), or marks it FAILED while keeping
 * the raw observation for Needs Review / the Notification Lab — nothing from a supported,
 * enabled source is ever silently dropped.
 */
class NotificationProcessingWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as LuxWalletApp
        val registry = app.parserRegistry
        val pending = try { app.notificationRepository.getPending() } catch (error: Exception) {
            if (error is CancellationException) throw error
            return Result.retry()
        }
        var retryNeeded = false

        for (observation in pending) {
          try {
            val input = NotificationInput(
                sourceApp = observation.sourceApp,
                title = observation.title,
                text = observation.text,
                bigText = observation.bigText,
                subText = observation.subText,
                textLines = observation.textLines?.split("\n").orEmpty(),
                postedAt = observation.postedAt
            )

            when (val result = registry.parse(input)) {
                is ParseResult.Parsed -> {
                    app.transactionRepository.ingest(observation, result.candidate)
                }
                ParseResult.NotFinancial -> {
                    app.notificationRepository.update(observation.copy(parseStatus = ParseStatus.IGNORED))
                }
                is ParseResult.Failed -> {
                    app.notificationRepository.update(
                        observation.copy(parseStatus = ParseStatus.FAILED, parseFailureReason = result.reason)
                    )
                }
            }
          } catch (cancelled: CancellationException) {
              throw cancelled
          } catch (error: Exception) {
              retryNeeded = true
              android.util.Log.e("LuxProcessing", "Processing failed: ${error.javaClass.simpleName}")
          }
        }
        if (retryNeeded) return Result.retry()
        app.notificationRepository.purgeAccordingToPolicy(app.preferences.rawRetentionPolicy.first())
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "notification_processing"
    }
}
