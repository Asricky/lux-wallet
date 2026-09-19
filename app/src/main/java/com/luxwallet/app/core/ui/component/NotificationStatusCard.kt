package com.luxwallet.app.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.luxwallet.app.notification.NotificationAccess

@Composable fun NotificationStatusCard() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(NotificationAccess.isGranted(context)) }
    val connected by NotificationAccess.connected.collectAsState()
    val error by NotificationAccess.lastCaptureError.collectAsState()
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = NotificationAccess.isGranted(context)
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (!granted) "Aktifkan pencatatan otomatis" else if (connected) "Pemantauan notifikasi aktif" else "Menunggu koneksi notifikasi",
                style = MaterialTheme.typography.titleSmall)
            Text(error ?: if (connected) "Notifikasi baru dari sumber pilihanmu dicatat di perangkat ini."
                else "Izinkan akses notifikasi untuk mencatat transaksi bank dan e-wallet.", style = MaterialTheme.typography.bodySmall)
            if (!granted || !connected || error != null) {
                TextButton(onClick = {
                    if (granted) NotificationAccess.requestRebind(context)
                    context.startActivity(NotificationAccess.settingsIntent())
                }) { Text(if (granted) "Sambungkan ulang" else "Buka akses notifikasi") }
            }
        }
    }
}
