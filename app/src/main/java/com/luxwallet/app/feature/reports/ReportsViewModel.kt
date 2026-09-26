package com.luxwallet.app.feature.reports

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.engine.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.time.YearMonth

data class ReportDocument(val file: File, val report: MonthlyReport, val pages: Int, val hidden: Boolean)
class ReportsViewModel(private val app: LuxWalletApp): ViewModel() {
    val document = MutableStateFlow<ReportDocument?>(null)
    val busy = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)
    fun clear() { if(!busy.value) { document.value?.file?.delete();document.value=null;message.value=null } }
    fun generate(month: YearMonth, hidden: Boolean) {
        if(busy.value)return
        busy.value=true;message.value=null
        viewModelScope.launch {
            var file: File?=null
            try {
                val result=withContext(Dispatchers.IO) {
                    val report=app.database.withTransaction {
                        val categories=app.categoryRepository.observeAll().first()
                        MonthlyReportEngine.create(month,app.transactionRepository.observeAll().first(),categories.associate { it.id to it.name },
                            app.paydayPlanRepository.plans.first(),categories.filter { it.name==TransportPlan.CATEGORY }.map { it.id }.toSet(),
                            categories.filter { it.name==PaydayPlan.BILLS_CATEGORY }.map { it.id }.toSet())
                    }
                    val cache=File(app.cacheDir,"monthly-reports").apply { mkdirs() }
                    // Private, disposable snapshots. Clean stale files left by process death.
                    cache.listFiles()?.filter { it.name.startsWith("lumi-") && it != document.value?.file }?.forEach { it.delete() }
                    val target=File.createTempFile("lumi-$month-",".pdf",cache);file=target
                    val pages=target.outputStream().use { MonthlyReportPdf.write(app,report,hidden,it) }
                    ReportDocument(target,report,pages,hidden)
                }
                document.value?.file?.delete();document.value=result;file=null
            } catch(e: CancellationException) { throw e }
            catch(_:Exception) { message.value="Laporan belum berhasil dibuat. Coba kembali." }
            finally { file?.delete();busy.value=false }
        }
    }
    fun save(uri: Uri) {
        val snapshot=document.value?:return
        if(busy.value)return
        busy.value=true;message.value=null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    app.contentResolver.openOutputStream(uri,"wt")?.use { out -> snapshot.file.inputStream().use { it.copyTo(out) } }
                        ?: error("Tujuan tidak tersedia")
                };message.value="PDF tersimpan di lokasi yang kamu pilih."
            } catch(e: CancellationException) { throw e }
            catch(_:Exception) { message.value="PDF belum tersimpan. Cek ruang penyimpanan atau pilih lokasi lain." }
            finally { busy.value=false }
        }
    }
    override fun onCleared() { document.value?.file?.delete() }
}

suspend fun renderReportPage(file: File, index: Int): Bitmap = withContext(Dispatchers.IO) {
    ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
        PdfRenderer(fd).use { renderer ->
            renderer.openPage(index).use { page ->
                Bitmap.createBitmap(1190,(1190f*page.height/page.width).toInt(),Bitmap.Config.ARGB_8888).also {
                    it.eraseColor(Color.WHITE);page.render(it,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
            }
        }
    }
}
