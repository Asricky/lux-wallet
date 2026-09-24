package com.luxwallet.app.integration

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import com.luxwallet.app.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LumiCoverTest {
    @Test fun nineNativeLauncherCoversInflateAtDifferentSizes() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val ids = listOf(R.mipmap.ic_lumi_happy, R.mipmap.ic_lumi_proud, R.mipmap.ic_launcher,
            R.mipmap.ic_lumi_excited, R.mipmap.ic_lumi_curious, R.mipmap.ic_lumi_nervous,
            R.mipmap.ic_lumi_shocked, R.mipmap.ic_lumi_sad, R.mipmap.ic_lumi_angry)
        val sheet = Bitmap.createBitmap(576, 576, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(Color.WHITE)
        val pixels = mutableSetOf<Int>()
        ids.forEachIndexed { i, id ->
            val icon = app.getDrawable(id)!!
            val size = if (i % 2 == 0) 168 else 144
            val image = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            icon.setBounds(0, 0, size, size)
            icon.draw(Canvas(image))
            assertTrue(Color.alpha(image.getPixel(size / 2, size / 2)) > 0)
            pixels += image.getPixel(size / 2, size / 2)
            canvas.drawBitmap(image, (i % 3 * 192 + (192 - size) / 2).toFloat(), (i / 3 * 192).toFloat(), Paint())
        }
        assertTrue(pixels.size >= 7)
        java.io.File("build/reports/ui").mkdirs()
        java.io.File("build/reports/ui/lumi-launchers.png").outputStream().use { sheet.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
