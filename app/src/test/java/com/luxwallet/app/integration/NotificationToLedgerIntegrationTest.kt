package com.luxwallet.app.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.luxwallet.app.core.database.LuxDatabase
import com.luxwallet.app.core.database.entity.NotificationObservationEntity
import com.luxwallet.app.core.model.AccountKind
import com.luxwallet.app.core.model.AccountProvider
import com.luxwallet.app.core.model.ParseStatus
import com.luxwallet.app.core.model.ReviewStatus
import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionType
import com.luxwallet.app.data.AccountRepository
import com.luxwallet.app.data.CategoryRepository
import com.luxwallet.app.data.NotificationRepository
import com.luxwallet.app.data.TransactionRepository
import com.luxwallet.app.parser.core.NotificationInput
import com.luxwallet.app.parser.core.ParseResult
import com.luxwallet.app.parser.core.ParserRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * End-to-end regression test of the full pipeline (PRD §49 "Integration tests"):
 * Notification → Observation → Parser → Transaction → Ledger → account balances.
 * Uses an in-memory Room database via Robolectric so it runs on the JVM, no emulator required.
 */
@RunWith(RobolectricTestRunner::class)
class NotificationToLedgerIntegrationTest {

    private lateinit var db: LuxDatabase
    private lateinit var accountRepository: AccountRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var transactionRepository: TransactionRepository
    private val registry = ParserRegistry()

    private var bcaAccountId = 0L
    private var seabankAccountId = 0L
    private var shopeepayAccountId = 0L

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, LuxDatabase::class.java).allowMainThreadQueries().build()

        accountRepository = AccountRepository(db.accountDao())
        categoryRepository = CategoryRepository(db.categoryDao())
        notificationRepository = NotificationRepository(db.notificationObservationDao())
        transactionRepository = TransactionRepository(
            db.transactionDao(), db.ledgerEntryDao(), db.notificationObservationDao(),
            db.accountDao(), db.merchantRuleDao(), categoryRepository, db
        )
        categoryRepository.seedDefaultsIfEmpty()

        bcaAccountId = accountRepository.createAccount("BCA", AccountKind.BANK, AccountProvider.BCA, 15_000_000, 0L)
        seabankAccountId = accountRepository.createAccount("SeaBank", AccountKind.BANK, AccountProvider.SEABANK, 7_500_000, 0L)
        shopeepayAccountId = accountRepository.createAccount("ShopeePay", AccountKind.EWALLET, AccountProvider.SHOPEEPAY, 25_000, 0L)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun ingestNotification(sourceApp: SourceApp, title: String, text: String, postedAt: Long,
        notificationKey: String? = null, eventTime: Long? = null) {
        val input = NotificationInput(sourceApp = sourceApp, title = title, text = text, postedAt = postedAt)
        val hash = NotificationRepository.hashPayload(sourceApp.name, sourceApp.name, title, text, postedAt)
        val observation = NotificationObservationEntity(
            sourceApp = sourceApp, packageName = sourceApp.name, notificationKey = notificationKey,
            title = title, text = text, bigText = null, subText = null, textLines = null,
            postedAt = postedAt, receivedAt = postedAt, rawPayloadHash = hash,
            parserVersion = ParserRegistry.PARSER_VERSION, parseStatus = ParseStatus.PENDING, eventTime = eventTime
        )
        val id = notificationRepository.insertIfNew(observation) ?: return
        val stored = db.notificationObservationDao().getById(id)!!

        when (val result = registry.parse(input)) {
            is ParseResult.Parsed -> transactionRepository.ingest(stored, result.candidate)
            ParseResult.NotFinancial -> notificationRepository.update(stored.copy(parseStatus = ParseStatus.IGNORED))
            is ParseResult.Failed -> notificationRepository.update(stored.copy(parseStatus = ParseStatus.FAILED, parseFailureReason = result.reason))
        }
    }

    @Test
    fun sample2and3_endToEnd_becomesOneInternalTransfer() = runBlocking {
        // Sample 2: BCA receives Rp329.306 from SeaBank
        ingestNotification(
            SourceApp.MYBCA, "Catatan Finansial",
            "Pemasukan sebesar IDR 329,306.00 dari **KAS **CKY K di kategori Transfer Rekening.",
            postedAt = 1_000_000_000_000
        )
        // Sample 3: SeaBank realtime transfer out Rp329.306, 20s later
        ingestNotification(
            SourceApp.SEABANK, "Realtime Transfer",
            "Kamu baru melakukan transfer real-time senilai Rp329.306 ...",
            postedAt = 1_000_000_020_000
        )

        val all = db.transactionDao().getAllOnce()
        assertEquals("expected exactly one merged transaction, not two separate legs", 1, all.size)
        val tx = all.single()
        assertTrue(tx.isInternalTransfer)
        assertEquals(TransactionType.INTERNAL_TRANSFER, tx.type)
        assertEquals(329_306L, tx.amount)
        assertEquals(seabankAccountId, tx.sourceAccountId)
        assertEquals(bcaAccountId, tx.destinationAccountId)

        // Ledger + balances reflect both legs.
        val bca = accountRepository.getById(bcaAccountId)!!
        val seabank = accountRepository.getById(seabankAccountId)!!
        assertEquals(15_000_000 + 329_306, bca.currentEstimatedBalance)
        assertEquals(7_500_000 - 329_306, seabank.currentEstimatedBalance)

        // Internal transfers must not affect cashflow totals (PRD §11).
        val cashflowTx = db.transactionDao().getAllOnce().filter { !it.isInternalTransfer }
        assertTrue(cashflowTx.isEmpty())
    }

    @Test
    fun sample4and5_endToEnd_becomesOneInternalTransfer() = runBlocking {
        // Sample 4: ShopeePay receives Rp10.000 top-up
        ingestNotification(
            SourceApp.SHOPEEPAY, "Isi Saldo Berhasil",
            "Pengisian saldo sebesar Rp10.000 telah ditambahkan ke ShopeePay-mu. Saldo saat ini sebesar Rp25.191.",
            postedAt = 2_000_000_000_000
        )
        // Sample 5: SeaBank sends Rp10.000 to ShopeePay, 15s later
        ingestNotification(
            SourceApp.SEABANK, "Pembayaran Berhasil",
            "Kamu telah melakukan transfer virtual account sebesar Rp10.000 kepada ShopeePay ...",
            postedAt = 2_000_000_015_000
        )

        val all = db.transactionDao().getAllOnce()
        assertEquals(1, all.size)
        val tx = all.single()
        assertTrue(tx.isInternalTransfer)
        assertEquals(ReviewStatus.CONFIRMED, tx.reviewStatus) // explicit destination hint => auto-match
        assertEquals(seabankAccountId, tx.sourceAccountId)
        assertEquals(shopeepayAccountId, tx.destinationAccountId)

        // A reported snapshot must not silently overwrite the opening balance plus ledger delta.
        val shopeepay = accountRepository.getById(shopeepayAccountId)!!
        assertEquals(35_000L, shopeepay.currentEstimatedBalance)
    }

    @Test
    fun sample8_bcaExpense_neverInventsMerchant() = runBlocking {
        ingestNotification(
            SourceApp.MYBCA, "Catatan Finansial",
            "Pengeluaran sebesar IDR 1.00 di kategori Belanja Bulanan.",
            postedAt = 3_000_000_000_000
        )

        val tx = db.transactionDao().getAllOnce().single()
        assertEquals(1L, tx.amount)
        assertEquals(null, tx.merchantName)
        assertEquals(bcaAccountId, tx.sourceAccountId)
    }

    @Test
    fun duplicateNotification_doesNotCreateSecondTransaction() = runBlocking {
        val title = "Pembayaran QRIS berhasil"
        val text = "Pembayaran QRIS untuk PopCorn Technology sebesar 50.500 telah berhasil."
        ingestNotification(SourceApp.SEABANK, title, text, postedAt = 4_000_000_000_000)
        // The exact same notification delivered again (e.g. OS redelivery) has an identical raw hash.
        ingestNotification(SourceApp.SEABANK, title, text, postedAt = 4_000_000_000_000)

        assertEquals(1, db.transactionDao().getAllOnce().size)
    }

    @Test fun oldNotificationDoesNotCountOpeningBalanceTwice() = runBlocking {
        val account = accountRepository.getById(seabankAccountId)!!
        db.accountDao().update(account.copy(openingBalanceDate = 5000L))
        ingestNotification(SourceApp.SEABANK, "Pembayaran QRIS berhasil",
            "Pembayaran QRIS untuk PopCorn Technology sebesar 50.500 telah berhasil.", 4000L)
        assertTrue(db.transactionDao().getAllOnce().isEmpty())
        assertEquals(7500000L, accountRepository.getById(seabankAccountId)!!.currentEstimatedBalance)
    }

    @Test fun twoPurchasesAtSameMerchantRemainTwoTransactions() = runBlocking {
        val body = "Pembayaran QRIS untuk PopCorn Technology sebesar 50.500 telah berhasil."
        ingestNotification(SourceApp.SEABANK, "Pembayaran QRIS berhasil", body, 10000L)
        ingestNotification(SourceApp.SEABANK, "Pembayaran QRIS berhasil", body, 20000L)
        assertEquals(2, db.transactionDao().getAllOnce().size)
        assertEquals(7500000L - 101000L, accountRepository.getById(seabankAccountId)!!.currentEstimatedBalance)
    }

    @Test fun retryingSameObservationDoesNotApplyLedgerTwice() = runBlocking {
        val body = "Pembayaran QRIS untuk PopCorn Technology sebesar 50.500 telah berhasil."
        ingestNotification(SourceApp.SEABANK, "Pembayaran QRIS berhasil", body, 10000L)
        val observation = db.notificationObservationDao().getById(1L)!!
        val result = registry.parse(NotificationInput(SourceApp.SEABANK, observation.title, body, postedAt = 10000L)) as ParseResult.Parsed
        transactionRepository.ingest(observation.copy(parseStatus = ParseStatus.PENDING), result.candidate)
        assertEquals(1, db.transactionDao().getAllOnce().size)
        assertEquals(7500000L - 50500L, accountRepository.getById(seabankAccountId)!!.currentEstimatedBalance)
    }

    @Test fun ignoredTransactionReversesItsBalanceExactlyOnce() = runBlocking {
        ingestNotification(SourceApp.SEABANK, "Pembayaran QRIS berhasil",
            "Pembayaran QRIS untuk PopCorn Technology sebesar 50.500 telah berhasil.", 10000L)
        val id = db.transactionDao().getAllOnce().single().id
        transactionRepository.markIgnored(id)
        transactionRepository.markIgnored(id)
        assertEquals(7500000L, accountRepository.getById(seabankAccountId)!!.currentEstimatedBalance)
        assertTrue(db.ledgerEntryDao().getAllOnce().isEmpty())
    }

    @Test fun paymentIsNeverMergedWithIncomingTopup() = runBlocking {
        ingestNotification(SourceApp.SEABANK, "Pembayaran QRIS berhasil",
            "Pembayaran QRIS untuk PopCorn Technology sebesar 10.000 telah berhasil.", 10000L)
        ingestNotification(SourceApp.SHOPEEPAY, "Isi Saldo Berhasil",
            "Pengisian saldo sebesar Rp10.000 telah ditambahkan ke ShopeePay-mu.", 11000L)
        assertEquals(2, db.transactionDao().getAllOnce().size)
        assertTrue(db.transactionDao().getAllOnce().none { it.isInternalTransfer })
    }

    @Test fun failedLedgerWriteRollsBackTransactionAndKeepsObservationPending() = runBlocking {
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_ledger BEFORE INSERT ON ledger_entries BEGIN SELECT RAISE(ABORT, 'test failure'); END")
        var failed = false
        try {
            ingestNotification(SourceApp.SEABANK, "Pembayaran QRIS berhasil",
                "Pembayaran QRIS untuk PopCorn Technology sebesar 50.500 telah berhasil.", 10000L)
        } catch (_: Exception) { failed = true }
        assertTrue(failed)
        assertTrue(db.transactionDao().getAllOnce().isEmpty())
        assertEquals(1, notificationRepository.getPending().size)
        assertEquals(7500000L, accountRepository.getById(seabankAccountId)!!.currentEstimatedBalance)
    }

    @Test fun myBcaThreeRupiahRedeliveryUsesOriginalEventIdentity() = runBlocking {
        val body = "Pengeluaran sebesar IDR 3.00 di kategori Belanja Bulanan."
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 10000L, "bca:key", 9000L)
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 12000L, "bca:key", 9000L)
        assertEquals(1, db.transactionDao().getAllOnce().size)
        assertEquals(3L, com.luxwallet.app.core.common.CashflowMath.totalExpense(db.transactionDao().getAllOnce()))
        assertEquals(15000000L - 3L, accountRepository.getById(bcaAccountId)!!.currentEstimatedBalance)
    }

    @Test fun myBcaAmbiguousDuplicateIsHeldUntilConfirmedAsSeparatePayment() = runBlocking {
        val body = "Pengeluaran sebesar IDR 3.00 di kategori Belanja Bulanan."
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 10000L, "first", 9000L)
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 12000L, "second", 11000L)
        val held = db.transactionDao().getAllOnce().single { it.reviewReason == com.luxwallet.app.core.model.ReviewReason.POSSIBLE_DUPLICATE }
        assertEquals(3L, com.luxwallet.app.core.common.CashflowMath.totalExpense(db.transactionDao().getAllOnce()))
        assertEquals(15000000L - 3L, accountRepository.getById(bcaAccountId)!!.currentEstimatedBalance)
        transactionRepository.confirm(held.id)
        transactionRepository.confirm(held.id)
        assertEquals(6L, com.luxwallet.app.core.common.CashflowMath.totalExpense(db.transactionDao().getAllOnce()))
        assertEquals(15000000L - 6L, accountRepository.getById(bcaAccountId)!!.currentEstimatedBalance)
    }

    @Test fun myBcaSameAmountDifferentCategoryRemainsIndependent() = runBlocking {
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", "Pengeluaran sebesar IDR 3.00 di kategori Belanja Bulanan.", 10000L)
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", "Pengeluaran sebesar IDR 3.00 di kategori Makanan.", 12000L)
        assertEquals(6L, com.luxwallet.app.core.common.CashflowMath.totalExpense(db.transactionDao().getAllOnce()))
    }

    @Test fun myBcaReplayAfterRawRetentionDoesNotDoubleCount() = runBlocking {
        val body = "Pengeluaran sebesar IDR 3.00 di kategori Belanja Bulanan."
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 10000L, "bca:key", 9000L)
        notificationRepository.purgeAccordingToPolicy(com.luxwallet.app.core.model.RawRetentionPolicy.NEVER)
        assertEquals("", db.notificationObservationDao().getById(1L)!!.text)
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 12000L, "bca:key", 9000L)
        assertEquals(1, db.transactionDao().getAllOnce().size)
    }

    @Test fun changingAccountValueCreatesAdjustmentWithoutIncomeOrExpense() = runBlocking {
        transactionRepository.setAccountBalance(bcaAccountId, 10000000L)
        assertEquals(10000000L, accountRepository.getById(bcaAccountId)!!.currentEstimatedBalance)
        val all = db.transactionDao().getAllOnce()
        assertEquals(TransactionType.BALANCE_ADJUSTMENT, all.single().type)
        assertEquals(0L, com.luxwallet.app.core.common.CashflowMath.totalExpense(all))
        assertEquals(0L, com.luxwallet.app.core.common.CashflowMath.totalIncome(all))
    }

    @Test fun repeatedHeldNotificationDoesNotCreateMoreReviewRows() = runBlocking {
        val body = "Pengeluaran sebesar IDR 3.00 di kategori Belanja Bulanan."
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 10000L, "first", 9000L)
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 12000L, "second", 11000L)
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 13000L, "second", 11000L)
        assertEquals(2, db.transactionDao().getAllOnce().size)
        assertEquals(15000000L - 3L, accountRepository.getById(bcaAccountId)!!.currentEstimatedBalance)
    }

    @Test fun upgradingReviewsExistingDoubleCountAndRestoresBalanceOnce() = runBlocking {
        val body = "Pengeluaran sebesar IDR 3.00 di kategori Belanja Bulanan."
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 10000L)
        ingestNotification(SourceApp.MYBCA, "Catatan Finansial", body, 50000L)
        assertEquals(15000000L - 6L, accountRepository.getById(bcaAccountId)!!.currentEstimatedBalance)
        // Recreate the v1 state: both notifications were counted, although posted seconds apart.
        val second = db.notificationObservationDao().getById(2)!!
        db.notificationObservationDao().update(second.copy(postedAt = 12000L, contentHash = null))
        transactionRepository.reviewExistingDuplicates()
        transactionRepository.reviewExistingDuplicates()
        assertEquals(15000000L - 3L, accountRepository.getById(bcaAccountId)!!.currentEstimatedBalance)
        assertEquals(1, db.transactionDao().getAllOnce().count { it.reviewReason == com.luxwallet.app.core.model.ReviewReason.POSSIBLE_DUPLICATE })
    }
}
