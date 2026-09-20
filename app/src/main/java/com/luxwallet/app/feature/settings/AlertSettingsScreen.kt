package com.luxwallet.app.feature.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.NotificationOptions
import com.luxwallet.app.notification.TransactionNotifications
import kotlinx.coroutines.launch

@Composable fun AlertSettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as LuxWalletApp
    val options by app.preferences.notificationOptions.collectAsState(initial = emptyMap())
    val scope = rememberCoroutineScope()
    var allowed by remember { mutableStateOf(TransactionNotifications.allowed(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed = TransactionNotifications.allowed(context) }
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event -> if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) allowed = TransactionNotifications.allowed(context) }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Kabar transaksi, sesuai pilihanmu", style = MaterialTheme.typography.headlineSmall) }
        item { Text("Konfirmasi dikirim setelah pencatatan tersimpan. Transfer otomatis menunggu pasangan notifikasi, maksimal 30 menit sejak dicatat. Jadwal pengiriman mengikuti pembatasan baterai Android.") }
        item { Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (allowed) "Notifikasi perangkat aktif" else "Izinkan notifikasi perangkat")
            Text("Nominal mengikuti tombol sembunyikan saldo. Layar kunci hanya menampilkan pemberitahuan umum.", style = MaterialTheme.typography.bodySmall)
            TextButton({
                if (Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
            }) { Text("Atur izin Android") }
        } } }
        item { Card { Column(Modifier.padding(16.dp)) {
            NotificationOptions.entries.forEach { option ->
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(option.label, Modifier.weight(1f))
                    Switch(options[option] != false, { enabled -> scope.launch { app.preferences.setNotificationOption(option, enabled) } })
                }
            }
        } } }
        item { Text("Peringatan budget berdiri sendiri. Mematikan konfirmasi tidak menghentikan pencatatan transaksi. Notifikasi lama tidak dikirim ulang setelah izin diaktifkan.", style = MaterialTheme.typography.bodySmall) }
    }
}
