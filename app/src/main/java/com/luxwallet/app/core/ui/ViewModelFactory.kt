package com.luxwallet.app.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.luxwallet.app.LuxWalletApp

/** Minimal factory bridging our hand-rolled [LuxWalletApp] container into Compose's `viewModel()`. */
class LuxViewModelFactory(private val create: (LuxWalletApp) -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = appHolder ?: error("LuxViewModelFactory used before app was set")
        return create(app) as T
    }

    companion object {
        var appHolder: LuxWalletApp? = null
    }
}

@Composable
inline fun <reified T : ViewModel> luxViewModel(noinline create: (LuxWalletApp) -> T): T {
    val context = LocalContext.current
    val app = context.applicationContext as LuxWalletApp
    LuxViewModelFactory.appHolder = app
    return viewModel(factory = LuxViewModelFactory { create(it) })
}
