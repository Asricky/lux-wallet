package com.luxwallet.app.feature.reports

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.ui.component.ChoiceField
import com.luxwallet.app.core.ui.luxViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun ReportsScreen() {
    val vm=luxViewModel { ReportsViewModel(it) }
    val document by vm.document.collectAsState()
    val busy by vm.busy.collectAsState()
    val message by vm.message.collectAsState()
    val app=LocalContext.current.applicationContext as LuxWalletApp
    val hidden by app.preferences.amountsHidden.collectAsState(initial=true)
    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var showAmounts by rememberSaveable { mutableStateOf(false) }
    var page by rememberSaveable { mutableIntStateOf(0) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var choosingLocation by rememberSaveable { mutableStateOf(false) }
    val save=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        choosingLocation=false
        if(uri!=null)vm.save(uri)
    }
    // Never keep a revealed PDF on screen when global privacy is enabled.
    LaunchedEffect(hidden) { if(hidden && document?.hidden==false && !busy && !choosingLocation) { vm.clear();showAmounts=false } }
    val months=remember { (0L..120L).map { YearMonth.now().minusMonths(it) } }
    val format=remember { DateTimeFormatter.ofPattern("MMMM yyyy",Locale("id","ID")) }
    val locked=busy || choosingLocation
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Cerita uangmu, satu bulan penuh",style=MaterialTheme.typography.titleLarge)
        Text("Ringkasan, kategori, kalender, evaluasi Lumi, dan daftar transaksi. Bulan berjalan diberi label sementara.",style=MaterialTheme.typography.bodyMedium)
        if(!locked) ChoiceField("Bulan laporan",YearMonth.parse(monthText).format(format),months.map { it.format(format) },{
            monthText=months[it].toString();page=0;vm.clear()
        }) else Text(YearMonth.parse(monthText).format(format))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Checkbox(showAmounts,onCheckedChange={ showAmounts=it;vm.clear() },enabled=!locked)
            Column(Modifier.weight(1f)) {
                Text("Sertakan nominal dalam PDF",style=MaterialTheme.typography.titleSmall)
                Text("Jika dimatikan, angka uang disamarkan. Nama kategori dan merchant tetap terlihat.",style=MaterialTheme.typography.bodySmall)
            }
        }
        Button(onClick={ page=0;vm.generate(YearMonth.parse(monthText),!showAmounts) },enabled=!locked,modifier=Modifier.fillMaxWidth()) {
            Text(if(busy) "Memproses laporan…" else if(document==null) "Buat preview PDF" else "Perbarui laporan")
        }
        message?.let { Text(it,style=MaterialTheme.typography.bodyMedium) }
        if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        document?.let { doc ->
            Text("Preview PDF · ${doc.pages} halaman",style=MaterialTheme.typography.titleMedium)
            Text("Snapshot saat dibuat. Unduhan berisi file yang sama dengan preview ini.",style=MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick={ choosingLocation=true;save.launch("Lumi-laporan-${doc.report.month}.pdf") },enabled=!locked,modifier=Modifier.fillMaxWidth()) { Text("Simpan / download PDF") }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                TextButton({page--},enabled=page>0 && !locked) { Text("Sebelumnya") }
                Text("${page+1} / ${doc.pages}",Modifier.padding(top=12.dp))
                TextButton({page++},enabled=page<doc.pages-1 && !locked) { Text("Berikutnya") }
            }
            Text("Perbesar preview ${(zoom*100).toInt()}%",style=MaterialTheme.typography.bodySmall)
            Slider(value=zoom,onValueChange={zoom=it},valueRange=1f..2.5f,steps=2)
            var bitmap by remember(doc.file,page) { mutableStateOf<android.graphics.Bitmap?>(null) }
            var renderError by remember(doc.file,page) { mutableStateOf(false) }
            LaunchedEffect(doc.file,page) {
                try { bitmap=renderReportPage(doc.file,page.coerceIn(0,doc.pages-1)) }
                catch(e:kotlinx.coroutines.CancellationException){throw e}
                catch(_:Exception){renderError=true}
            }
            if(renderError) Text("Preview tidak dapat dibuka. Coba perbarui laporan.")
            else if(bitmap==null) LinearProgressIndicator(Modifier.fillMaxWidth())
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val width=maxWidth*zoom
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    bitmap?.let { Image(it.asImageBitmap(),"Halaman ${page+1} laporan PDF",Modifier.width(width).aspectRatio(595f/842f)) }
                }
            }
        }
    }
}
