package com.luxwallet.app.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.ui.theme.LocalLuxSemanticColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CARD_TIME_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

/** PRD §28: a normal card ("Merchant / Type • Source / -Amount / Category / Time") and a distinct, non-red internal-transfer card. */
@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    sourceAccountName: String?,
    destinationAccountName: String?,
    categoryName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalLuxSemanticColors.current
    val time = Instant.ofEpochMilli(transaction.transactionTime).atZone(ZoneId.systemDefault()).format(CARD_TIME_FORMAT)

    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp)) {
            if (transaction.isInternalTransfer) {
                Text(
                    "${sourceAccountName ?: "?"} → ${destinationAccountName ?: "?"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text("Internal Transfer", color = semantic.transfer, style = MaterialTheme.typography.bodyMedium)
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                    Text(AmountFormat.rupiah(transaction.amount), color = semantic.transfer, fontWeight = FontWeight.Medium)
                    Text(time, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                val title = transaction.merchantName ?: transaction.counterpartyName ?: (categoryName ?: "Transaction")
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "${transaction.type.name.replace('_', ' ')} • ${sourceAccountName ?: "?"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                val amountColor = if (transaction.direction == TransactionDirection.OUT) semantic.expense else semantic.income
                val sign = if (transaction.direction == TransactionDirection.OUT) "-" else "+"
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                    Text("$sign${AmountFormat.rupiah(transaction.amount)}", color = amountColor, fontWeight = FontWeight.Medium)
                    Text(categoryName ?: "Uncategorized", style = MaterialTheme.typography.bodyMedium)
                }
                Text(time, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
