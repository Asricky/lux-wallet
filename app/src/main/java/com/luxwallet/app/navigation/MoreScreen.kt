package com.luxwallet.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable fun MoreScreen(onNavigate: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Lainnya", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp)) }
        item { Text("Rencana & aktivitas", style = MaterialTheme.typography.labelLarge) }
        item { MenuRow("Rencana sampai gajian", "Alokasi saldo sampai tanggal 25 atau 1", Icons.Outlined.EventAvailable) { onNavigate(LuxDestinations.PLANNER) } }
        item { MenuRow("Riwayat transaksi", "Semua pemasukan dan pengeluaran", Icons.Outlined.ReceiptLong) { onNavigate(LuxDestinations.TRANSACTIONS) } }
        item { MenuRow("Laporan bulanan", "Preview dan download PDF bersama evaluasi Lumi", Icons.Outlined.PictureAsPdf) { onNavigate(LuxDestinations.REPORTS) } }
        item { MenuRow("Arus kas", "Rincian per bulan dan kategori", Icons.Outlined.PieChart) { onNavigate(LuxDestinations.CASHFLOW) } }
        item { MenuRow("Anggaran & target", "Transportasi, bisnis, dan investasi", Icons.Outlined.Savings) { onNavigate(LuxDestinations.BUDGETS_GOALS) } }
        item { MenuRow("Kalkulator", "Hitung uang dan simulasi investasi", Icons.Outlined.Calculate) { onNavigate(LuxDestinations.CALCULATOR) } }
        item { HorizontalDivider(Modifier.padding(vertical = 12.dp)); Text("Pendamping & pengaturan", style = MaterialTheme.typography.labelLarge) }
        item { MenuRow("Saran Lumi", "Pengingat harian dan pilihan maskot", Icons.Outlined.Pets) { onNavigate(LuxDestinations.COACH) } }
        item { MenuRow("Perlu ditinjau", "Periksa catatan yang belum pasti", Icons.Outlined.FactCheck) { onNavigate(LuxDestinations.NEEDS_REVIEW) } }
        item { MenuRow("Pemantauan notifikasi", "Koneksi dengan aplikasi keuangan", Icons.Outlined.Notifications) { onNavigate(LuxDestinations.NOTIFICATION_SETTINGS) } }
        item { MenuRow("Pengaturan", "Tampilan, keamanan, dan data", Icons.Outlined.Settings) { onNavigate(LuxDestinations.SETTINGS) } }
    }
}
@Composable private fun MenuRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.background) {
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
            Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Outlined.ChevronRight, null)
        }
    }
}
