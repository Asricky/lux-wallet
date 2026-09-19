package com.luxwallet.app.data

import com.luxwallet.app.core.database.LuxDatabase
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** PRD §38: MVP CSV transaction export. */
class CsvExportRepository(private val database: LuxDatabase) {

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun exportTransactions(destinationFile: File) {
        val transactions = database.transactionDao().getAllOnce().sortedBy { it.transactionTime }
        val accounts = database.accountDao().getAllAccountsOnce().associateBy { it.id }
        val categories = database.categoryDao().getAll().associateBy { it.id }

        val header = listOf(
            "id", "date", "type", "direction", "amount", "currency", "source_account",
            "destination_account", "merchant", "category", "review_status", "is_internal_transfer",
            "is_manual", "note"
        ).joinToString(",")

        val rows = transactions.map { tx ->
            listOf(
                tx.id.toString(),
                Instant.ofEpochMilli(tx.transactionTime).atZone(ZoneId.systemDefault()).format(dateFormat),
                tx.type.name,
                tx.direction.name,
                tx.amount.toString(),
                tx.currency,
                accounts[tx.sourceAccountId]?.name.orEmpty(),
                accounts[tx.destinationAccountId]?.name.orEmpty(),
                tx.merchantName.orEmpty(),
                categories[tx.categoryId]?.name.orEmpty(),
                tx.reviewStatus.name,
                tx.isInternalTransfer.toString(),
                tx.isManual.toString(),
                tx.note.orEmpty()
            ).joinToString(",") { csvEscape(it) }
        }

        destinationFile.writeText((listOf(header) + rows).joinToString("\n"))
    }

    private fun csvEscape(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
