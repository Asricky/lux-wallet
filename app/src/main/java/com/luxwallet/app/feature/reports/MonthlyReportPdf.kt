package com.luxwallet.app.feature.reports

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.engine.MonthlyReport
import com.luxwallet.app.notification.LumiNotificationBrand
import java.io.OutputStream
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

/** One A4 document powers both the preview and the exported file. No network or storage permission. */
internal interface ReportPages {
    fun startPage(number: Int): Canvas
    fun finishPage()
}
object MonthlyReportPdf {
    fun write(context: Context, report: MonthlyReport, hidden: Boolean, output: OutputStream): Int {
        val pdf = PdfDocument()
        var active: PdfDocument.Page? = null
        val pages = object : ReportPages {
            override fun startPage(number: Int): Canvas {
                val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, number).create())
                active = page
                return page.canvas
            }
            override fun finishPage() { active?.let { pdf.finishPage(it) }; active = null }
        }
        try {
            val count = draw(context, report, hidden, pages)
            pdf.writeTo(output)
            return count
        } finally { pages.finishPage(); pdf.close() }
    }
    internal fun draw(context: Context, report: MonthlyReport, hidden: Boolean, pages: ReportPages): Int {
        val teal = Color.rgb(8,122,120); val ink = Color.rgb(22,50,79); val gray = Color.rgb(92,108,120)
        val mint = Color.rgb(227,247,242); val red = Color.rgb(164,55,66)
        val month = report.month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id","ID")))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var canvas: Canvas? = null
        var count = 0; var y = 0f
        fun money(value: Long) = if (hidden) "********" else AmountFormat.rupiah(value)
        fun text(value: String, x: Float, baseline: Float, size: Float = 11f, color: Int = ink, bold: Boolean = false) {
            paint.color=color;paint.textSize=size;paint.typeface=Typeface.create("sans-serif",if(bold) Typeface.BOLD else Typeface.NORMAL)
            canvas!!.drawText(value,x,baseline,paint)
        }
        fun finish() { canvas?.let { text("Lumi  /  $month",40f,809f,9f,gray);text("${count}",535f,809f,9f,gray);pages.finishPage() }; canvas=null }
        fun start(title: String) {
            finish();count++;canvas=pages.startPage(count)
            canvas!!.drawColor(Color.WHITE)
            paint.color=teal;canvas!!.drawRect(0f,0f,595f,8f,paint)
            canvas!!.drawBitmap(LumiNotificationBrand.logo(context),null,RectF(40f,26f,88f,74f),paint)
            text("LUMI / LAPORAN BULANAN",100f,45f,10f,teal,true)
            text(title,100f,66f,20f,ink,true);y=104f
        }
        fun space(height: Float) { if(y+height>776) start("$month · lanjutan") }
        fun paragraph(value: String, size: Float=11f, color: Int=ink, bold: Boolean=false) {
            val tp=TextPaint(Paint.ANTI_ALIAS_FLAG).apply { this.color=color;textSize=size;typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL) }
            // User labels are bounded; paragraphs wrap and page-break at line boundaries.
            val layout=StaticLayout.Builder.obtain(value,0,value.length,tp,515).setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(4f,1f).build()
            for(i in 0 until layout.lineCount) {
                val height=(layout.getLineBottom(i)-layout.getLineTop(i)).toFloat()
                space(height+2)
                text(value.substring(layout.getLineStart(i),layout.getLineEnd(i)).trimEnd(),40f,y+size,size,color,bold)
                y+=height
            };y+=8
        }
        fun heading(value: String) { space(42f);y+=8;paragraph(value,14f,teal,true) }
        fun categoryRows(title: String, rows: List<Pair<String,Long>>, total: Long) {
            heading(title)
            if(rows.isEmpty()) paragraph("Belum ada catatan.")
            rows.forEach { (name,value) ->
                space(65f);paragraph(name.take(160),11f,ink,true)
                text(money(value),40f,y+10,11f)
                if(!hidden && total>0) {
                    text(String.format(Locale("id","ID"),"%.1f%%",value*100.0/total),485f,y+10,10f,gray)
                    paint.color=mint;canvas!!.drawRoundRect(40f,y+18,555f,y+24,3f,3f,paint)
                    paint.color=teal;canvas!!.drawRoundRect(40f,y+18,40f+515f*(value.toDouble()/total).toFloat(),y+24,3f,3f,paint)
                };y+=38
            }
        }
        try {
            start(month)
            paragraph(if(report.partial) "Laporan sementara · 1–${report.through.dayOfMonth} $month" else "Periode lengkap · 1–${report.through.dayOfMonth} $month",11f,gray)
            val labels=listOf("PEMASUKAN","PENGELUARAN","SELISIH ARUS KAS")
            val values=listOf(report.income,report.expense,report.net)
            labels.forEachIndexed { i,label ->
                val x=40f+i*175f;paint.color=mint;canvas!!.drawRoundRect(x,y,x+165f,y+84f,12f,12f,paint)
                text(label,x+12,y+24,9f,gray,true)
                val number=money(values[i]); val size=if(number.length>17) 11f else 15f
                text(number,x+12,y+56,size,if(i==1 || values[i]<0) red else teal,true)
            };y+=108
            paragraph("${report.transactions.size} transaksi arus kas · ${report.pending} catatan perlu ditinjau · ${report.excluded} catatan di luar arus kas.",11f,gray)
            heading("Evaluasi Lumi")
            paragraph(report.verdict,12f,if(report.overDays>0 && report.pending==0) red else ink,true)
            paragraph(if(hidden) "Rincian pemakaian budget disembunyikan." else "${report.overDays} hari melampaui budget dari ${report.coveredDays} hari dengan rencana. Total kelebihan pada hari tersebut: ${money(report.budgetExcess)}.")
            heading("Langkah untuk bulan berikutnya")
            report.recommendations.forEachIndexed { i,advice -> paragraph("${i+1}. $advice") }
            paragraph("Dibuat " + DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm",Locale("id","ID")).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(report.generatedAt)),9f,gray)
            heading("Cara membaca laporan")
            paragraph("Selisih adalah pemasukan dikurangi pengeluaran tercatat, bukan saldo rekening atau sisa budget. Transfer sendiri, top-up e-wallet, koreksi saldo dan catatan diabaikan tidak dihitung. Budget hanya dinilai pada hari dengan rencana tersimpan; transportasi/tagihan menggunakan cadangan rencana. Catatan belum ditinjau dapat mengubah hasil. Notifikasi yang tidak tertangkap perlu dicatat manual.",9f,gray)
            if(hidden) paragraph("MODE PRIVASI · Nominal, persentase dan status budget per hari disamarkan. Nama kategori dan merchant tetap tercantum.",9f,gray)
            start("Rincian kategori")
            categoryRows("Uang masuk",report.incomes,report.income)
            categoryRows("Uang keluar",report.expenses,report.expense)
            start("Kalender arus kas")
            paragraph("Masuk / keluar: seluruh arus kas. Status budget: hanya belanja yang dihitung rencana, bukan seluruh pengeluaran.",10f,gray)
            report.days.forEach { day ->
                space(52f)
                text("${day.date.dayOfMonth}",40f,y+12,12f,teal,true)
                text("Masuk ${money(day.income)}",76f,y+12,10f)
                text("Keluar ${money(day.expense)}",305f,y+12,10f)
                val status=when { hidden -> "Budget disamarkan";day.budget==null -> "Tanpa rencana";else -> "Budget ${money(day.budget)} · ${if(day.budgetSpent!!>day.budget) "lebih ${money(day.budgetSpent-day.budget)}" else "sisa ${money(day.budget-day.budgetSpent)}"}" }
                text(status,76f,y+30,9f,gray);y+=48
            }
            start("Daftar transaksi")
            paragraph("Diurutkan menurut tanggal. Tanda + berarti masuk; tanda − berarti keluar. Data rekening dan isi notifikasi tidak disertakan.",10f,gray)
            if(report.transactions.isEmpty()) paragraph("Belum ada transaksi arus kas untuk periode ini.")
            val fmt=DateTimeFormatter.ofPattern("dd MMM HH:mm",Locale("id","ID")).withZone(ZoneId.systemDefault())
            report.transactions.forEach { tx ->
                space(100f)
                paragraph((tx.merchantName ?: tx.counterpartyName ?: report.categories[tx.categoryId] ?: "Transaksi").replace('\n',' ').take(160),11f,ink,true)
                paragraph("${fmt.format(Instant.ofEpochMilli(tx.transactionTime))} · ${report.categories[tx.categoryId] ?: "Tanpa kategori"}".take(200),9f,gray)
                paragraph("${if(tx.direction==TransactionDirection.IN) "+" else "−"}${money(tx.amount)}${if(tx.reviewStatus==com.luxwallet.app.core.model.ReviewStatus.NEEDS_REVIEW) " · perlu ditinjau" else ""}",11f,if(tx.direction==TransactionDirection.IN) teal else red)
            }
            finish();return count
        } finally { if(canvas!=null) pages.finishPage() }
    }
}
