package com.luxwallet.app.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class DonutSlice(val label: String, val value: Double, val color: Color)

/**
 * A lightweight custom Canvas donut chart (PRD §5) — no external chart library dependency.
 * Mirrors the myBCA cashflow reference's information hierarchy (ring + centered total) without
 * copying its branding (PRD §27/§42).
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerLabel: String,
    centerSubLabel: String,
    modifier: Modifier = Modifier,
    strokeWidthDp: Int = 28
) {
    val total = slices.sumOf { it.value }.coerceAtLeast(0.0001)

    Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val stroke = Stroke(width = strokeWidthDp.dp.toPx())
            var startAngle = -90f
            val diameter = size.minDimension - stroke.width
            val topLeft = androidx.compose.ui.geometry.Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            if (slices.isEmpty()) {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
            }

            for (slice in slices) {
                val sweep = (slice.value / total * 360.0).toFloat()
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
                startAngle += sweep
            }
        }
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(centerLabel, style = MaterialTheme.typography.titleLarge)
                Text(centerSubLabel, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
