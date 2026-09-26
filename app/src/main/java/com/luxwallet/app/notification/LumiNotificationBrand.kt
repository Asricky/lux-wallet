package com.luxwallet.app.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.luxwallet.app.R

/** Android uses an alpha-only status icon; the full mascot belongs in the large icon. */
object LumiNotificationBrand {
    @Volatile private var cached: Bitmap? = null
    fun logo(context: Context): Bitmap = cached ?: synchronized(this) {
        cached ?: Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888).also { bitmap ->
            context.getDrawable(R.mipmap.ic_launcher)!!.apply { setBounds(0, 0, 192, 192); draw(Canvas(bitmap)) }
            cached = bitmap
        }
    }
    fun builder(context: Context, channel: String) = NotificationCompat.Builder(context, channel)
        .setSmallIcon(R.drawable.ic_notification_lumi)
        .setLargeIcon(logo(context))
        .setColor(Color.rgb(8, 122, 120))
}
