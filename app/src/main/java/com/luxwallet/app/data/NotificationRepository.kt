package com.luxwallet.app.data

import com.luxwallet.app.core.database.dao.NotificationObservationDao
import com.luxwallet.app.core.database.entity.NotificationObservationEntity
import com.luxwallet.app.core.model.ParseStatus
import com.luxwallet.app.core.model.RawRetentionPolicy
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class NotificationRepository(private val observationDao: NotificationObservationDao) {

    fun observeRecent(limit: Int = 200): Flow<List<NotificationObservationEntity>> = observationDao.observeRecent(limit)
    fun observeAll(): Flow<List<NotificationObservationEntity>> = observationDao.observeAll()

    /** Returns null if an observation with the same raw payload hash already exists (exact re-delivery/update). */
    suspend fun insertIfNew(observation: NotificationObservationEntity): Long? {
        observationDao.findByHash(observation.rawPayloadHash)?.let { return null }
        return observationDao.insert(observation.copy(contentHash = com.luxwallet.app.engine.NotificationIdentity.hash(observation)))
    }

    suspend fun dismissFailed(id: Long) {
        val item = observationDao.getById(id) ?: return
        if (item.parseStatus == ParseStatus.FAILED) observationDao.update(item.copy(parseStatus = ParseStatus.IGNORED))
    }
    suspend fun getPending(): List<NotificationObservationEntity> = observationDao.getByStatus(ParseStatus.PENDING)

    suspend fun update(observation: NotificationObservationEntity) = observationDao.update(observation)

    suspend fun purgeAccordingToPolicy(policy: RawRetentionPolicy) {
        if (policy == RawRetentionPolicy.INDEFINITE) return
        val cutoff = when (policy) {
            RawRetentionPolicy.NEVER -> System.currentTimeMillis()
            RawRetentionPolicy.DAYS_7 -> System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            RawRetentionPolicy.DAYS_30 -> System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
            RawRetentionPolicy.INDEFINITE -> return
        }
        observationDao.purgeProcessedOlderThan(cutoff)
    }

    companion object {
        fun hashPayload(sourceApp: String, packageName: String, title: String, text: String, postedAt: Long,
                        notificationKey: String? = null, bigText: String? = null,
                        subText: String? = null, textLines: List<String> = emptyList()): String {
            val raw = (listOf(sourceApp, packageName, title, text, postedAt.toString(), notificationKey.orEmpty(),
                bigText.orEmpty(), subText.orEmpty()) + textLines).joinToString("") { "${it.length}:$it" }
            val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
