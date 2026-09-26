package com.luxwallet.app.integration

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.luxwallet.app.feature.reports.ReportPages
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.*
import com.luxwallet.app.engine.*
import com.luxwallet.app.feature.reports.MonthlyReportPdf
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File
import java.time.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MonthlyReportPdfTest {
    @Test fun a4LayoutHasReadablePagesPrivacyAndPaginatedTransactions() {
        val app=ApplicationProvider.getApplicationContext<Application>()
        val day=LocalDate.of(2026,9,26);val now=day.atTime(20,0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val records=(1..80).map { i -> TransactionEntity(id=i.toLong(),type=if(i==1)TransactionType.INCOME else TransactionType.EXPENSE,
            direction=if(i==1)TransactionDirection.IN else TransactionDirection.OUT,amount=if(i==1)6000000 else 12500L*i,
            sourceAccountId=1,categoryId=if(i%2==0)1 else 2,merchantName=if(i==1)"Pendapatan bulanan" else "Transaksi kebutuhan harian nomor $i",
            transactionTime=day.withDayOfMonth((i%26)+1).atTime(10,0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            createdAt=now,updatedAt=now,confidenceScore=1.0,reviewStatus=ReviewStatus.CONFIRMED) }
        val report=MonthlyReportEngine.create(YearMonth.from(day),records,mapOf(1L to "Food & Drink",2L to "Belanja & kebutuhan rumah"),emptyList(),emptySet(),emptySet(),now)
        File("build/reports/ui").mkdirs()
        for(hidden in listOf(false,true)) {
            val texts=mutableListOf<String>()
            var rendered=0
            var bitmap:Bitmap?=null
            val pages=object:ReportPages {
                override fun startPage(number:Int):Canvas {
                    bitmap=Bitmap.createBitmap(1190,1684,Bitmap.Config.ARGB_8888)
                    return object:Canvas(bitmap!!) {
                        override fun drawText(text:String,x:Float,y:Float,paint:Paint) {
                            assertTrue("Text outside A4: $text at $y",y in 8f..810f)
                            assertTrue("Text overflows right edge: $text",x+paint.measureText(text)<=558f)
                            texts.add(text)
                            super.drawText(text,x,y,paint)
                        }
                    }.apply { scale(2f,2f) }
                }
                override fun finishPage() {
                    rendered++
                    val variant=if(hidden) "private" else "full"
                    File("build/reports/ui/lumi-report-$variant-$rendered.png").outputStream().use { bitmap!!.compress(Bitmap.CompressFormat.PNG,100,it) }
                    bitmap!!.recycle()
                }
            }
            val count=MonthlyReportPdf.draw(app,report,hidden,pages)
            assertTrue(count>=8);assertEquals(count,rendered)
            assertTrue(texts.any { it.contains("nomor 80") })
            assertTrue(texts.any { it.contains("Evaluasi Lumi") })
            assertTrue(texts.any { it.contains("bulan berikutnya") })
            if(hidden) { assertFalse(texts.any { it.contains("Rp") });assertTrue(texts.any { it.contains("********") }) }
            else assertTrue(texts.any { it.contains("Rp6.000.000") })

        }
    }
}
