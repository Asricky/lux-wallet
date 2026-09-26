package com.luxwallet.app.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luxwallet.app.engine.InterimBudget
import com.luxwallet.app.core.common.AmountFormat

@Composable fun InterimBudgetCard(budget: InterimBudget, hidden: Boolean) {
    fun money(value: Long) = if (hidden) "********" else AmountFormat.rupiah(value)
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Saran harian sementara", style = MaterialTheme.typography.titleMedium)
            Text("Sisa hari ini ${money(budget.remaining)}", style = MaterialTheme.typography.titleLarge)
            Text("Batas hari ini ${money(budget.daily)} · terpakai ${money(budget.spent)}", style = MaterialTheme.typography.bodySmall)
            Text("Saldo tercatat ${money(budget.cash)} − cadangan ${money(budget.reserved)}. Dana bebas dan belanja hari ini dibagi ${budget.days} hari sampai ${budget.until}.", style = MaterialTheme.typography.bodySmall)
            Text("Transportasi terpisah. Cadangan memakai nilai terbesar dari profil atau rencana lama dan tetap ditahan penuh. Perkiraan gaji tidak dihitung. Konfirmasi saldo, tagihan yang sudah dibayar, dan tanggal pemasukan untuk angka yang lebih tepat.", style = MaterialTheme.typography.bodySmall)
            if (budget.remaining == 0L) Text("Tahan belanja tambahan; cek kebutuhan wajib dan perbarui alokasi.", color = MaterialTheme.colorScheme.error)
        }
    }
}
