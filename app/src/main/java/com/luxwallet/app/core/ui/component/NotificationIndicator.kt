package com.luxwallet.app.core.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.luxwallet.app.notification.NotificationAccess

@Composable fun NotificationIndicator(onClick: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(NotificationAccess.isGranted(context)) }
    val connected by NotificationAccess.connected.collectAsState()
    val error by NotificationAccess.lastCaptureError.collectAsState()
    val active = granted && connected && error == null
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = NotificationAccess.isGranted(context)
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val transition = rememberInfiniteTransition(label = "Status notifikasi")
    val pulse by transition.animateFloat(0.45f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "Indikator")
    IconButton(onClick, modifier = Modifier.semantics {
        contentDescription = "Pengaturan notifikasi"
        stateDescription = if (active) "Pemantauan aktif" else "Pemantauan tidak aktif"
    }) {
        Box(Modifier.size(26.dp)) {
            Icon(if (active) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff, null)
            Box(Modifier.align(Alignment.TopEnd).size(8.dp).alpha(pulse)
                .background(if (active) Color(0xFF40B77B) else MaterialTheme.colorScheme.error, CircleShape))
        }
    }
}
