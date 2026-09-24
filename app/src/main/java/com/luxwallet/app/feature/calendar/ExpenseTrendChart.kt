package com.luxwallet.app.feature.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.engine.DailyExpense
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable fun ExpenseTrendChart(points: List<DailyExpense>, selected: LocalDate, hidden: Boolean, onSelect: (LocalDate) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val max = (points.maxOfOrNull { it.amount } ?: 0L).coerceAtLeast(1000)
    val current = points.find { it.date == selected }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tren Pengeluaran", style = MaterialTheme.typography.titleMedium)
            when {
                hidden -> Text("Tampilkan nominal untuk melihat grafik.", style = MaterialTheme.typography.bodySmall)
                points.isEmpty() -> Text("Belum ada hari aktual pada periode ini.", style = MaterialTheme.typography.bodySmall)
                else -> {
                    Text("${current?.date?.dayOfMonth ?: "—"} · ${current?.let { AmountFormat.rupiah(it.amount) } ?: "Pilih tanggal"}", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.height(140.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Text(com.luxwallet.app.core.common.compactCashflow(max).removePrefix("+"), style = MaterialTheme.typography.labelSmall)
                            Text("Rp0", style = MaterialTheme.typography.labelSmall)
                        }
                        Canvas(Modifier.weight(1f).height(140.dp).semantics { contentDescription = "Grafik pengeluaran harian. Geser pemilih tanggal di bawah untuk rincian." }
                            .pointerInput(points) { detectTapGestures { position ->
                                val index = ((position.x / size.width) * (points.size - 1)).roundToInt().coerceIn(points.indices)
                                onSelect(points[index].date)
                            } }) {
                            val height = size.height - 12.dp.toPx()
                            val top = 6.dp.toPx()
                            fun point(i: Int) = Offset(if (points.size == 1) size.width / 2 else size.width * i / (points.size - 1), top + height * (1 - points[i].amount.toFloat() / max))
                            (0..2).forEach { i -> val y = top + height * i / 2; drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx()) }
                            val path = Path()
                            points.indices.forEach { i -> val p = point(i); if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
                            drawPath(path, primary, style = Stroke(2.dp.toPx()))
                            points.indices.forEach { i -> drawCircle(primary, if (points[i].date == selected) 5.dp.toPx() else 2.dp.toPx(), point(i)) }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("1", style = MaterialTheme.typography.labelSmall)
                        Text("Tanggal", style = MaterialTheme.typography.labelSmall)
                        Text(points.last().date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                    if (points.size > 1) Slider(value = (selected.dayOfMonth - 1).coerceIn(points.indices).toFloat(),
                        onValueChange = { onSelect(points[it.roundToInt()].date) }, valueRange = 0f..points.lastIndex.toFloat(),
                        steps = (points.size - 2).coerceAtLeast(0), modifier = Modifier.semantics { contentDescription = "Pilih tanggal tren pengeluaran" })
                }
            }
        }
    }
}
