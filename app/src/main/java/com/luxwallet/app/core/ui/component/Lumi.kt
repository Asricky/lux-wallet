package com.luxwallet.app.core.ui.component

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.luxwallet.app.R
import com.luxwallet.app.engine.PaydayStatus

enum class LumiMood { CALM, HAPPY, FOCUS }
fun lumiMood(status: PaydayStatus?): LumiMood = when {
    status == null -> LumiMood.CALM
    status.expired || status.freeRemaining < 0 || status.remainingToday < 0 -> LumiMood.FOCUS
    else -> LumiMood.HAPPY
}
@Composable fun Lumi(mood: LumiMood = LumiMood.CALM, modifier: Modifier = Modifier) {
    Image(painterResource(when (mood) {
        LumiMood.CALM -> R.drawable.ic_launcher_foreground
        LumiMood.HAPPY -> R.drawable.lumi_happy
        LumiMood.FOCUS -> R.drawable.lumi_focus
    }), "Lumi, penguin pendamping keuangan", modifier)
}
object LumiLauncher {
    private val names = listOf("LumiCalm", "LumiHappy", "LumiFocus")
    fun apply(context: Context, mood: LumiMood) {
        val manager = context.packageManager
        val components = names.map { ComponentName(context.packageName, "com.luxwallet.app.$it") }
        val target = components[mood.ordinal]
        if (manager.getComponentEnabledSetting(target) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) return
        // Enable the new icon first so older launchers never see zero launchable components.
        manager.setComponentEnabledSetting(target, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        components.filter { it != target }.forEach {
            manager.setComponentEnabledSetting(it, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
    }
}
