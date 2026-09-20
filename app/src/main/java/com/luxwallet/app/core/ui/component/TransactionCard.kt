package com.luxwallet.app.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.*
import com.luxwallet.app.core.ui.theme.LocalLuxSemanticColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun TransactionCard(transaction: TransactionEntity, sourceAccountName: String?, destinationAccountName: String?,
    categoryName: String?, onClick: () -> Unit, modifier: Modifier = Modifier, hideAmounts: Boolean = com.luxwallet.app.core.common.LocalAmountsHidden.current) {
    val tx = transaction
    val colors = LocalLuxSemanticColors.current
    val date = Instant.ofEpochMilli(tx.transactionTime).atZone(ZoneId.systemDefault())
    val held = tx.reviewReason == ReviewReason.POSSIBLE_DUPLICATE
    val color = if (held) colors.warning else if (tx.isInternalTransfer) colors.transfer else if (tx.direction == TransactionDirection.OUT) colors.expense else colors.income
    Card(onClick, modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(date.format(DateTimeFormatter.ofPattern("MMM", Locale("id", "ID"))), style = MaterialTheme.typography.labelSmall)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (tx.isInternalTransfer) "${sourceAccountName ?: "Rekening"} → ${destinationAccountName ?: "Rekening"}"
                    else tx.merchantName ?: tx.counterpartyName ?: categoryName ?: "Transaksi", style = MaterialTheme.typography.titleSmall)
                Text(if (hideAmounts) "********" else AmountFormat.rupiah(tx.amount), color = color, style = MaterialTheme.typography.titleMedium)
                Text(if (tx.isInternalTransfer) "Pindah saldo sendiri" else "${sourceAccountName ?: "Belum ada rekening"} · ${categoryName ?: "Belum dikategorikan"}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (held) Text("Calon duplikat · belum dihitung", color = colors.warning, style = MaterialTheme.typography.labelSmall)
                else if (tx.reviewStatus == ReviewStatus.NEEDS_REVIEW) Text("Perlu ditinjau", color = colors.warning, style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Outlined.ChevronRight, "Detail", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
