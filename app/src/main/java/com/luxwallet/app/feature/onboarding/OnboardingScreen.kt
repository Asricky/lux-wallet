package com.luxwallet.app.feature.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.model.AccountKind
import com.luxwallet.app.core.model.AccountProvider
import com.luxwallet.app.core.model.AssetClass
import com.luxwallet.app.core.model.LiabilityType
import com.luxwallet.app.core.model.RiskProfile
import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.ui.luxViewModel

private enum class OnboardingStep {
    WELCOME, PRIVACY, NOTIFICATION_ACCESS, SOURCES, ACCOUNTS, ASSETS_LIABILITIES, INCOME_OBLIGATIONS, GOALS, RISK_PROFILE, FINISH
}

@Composable
fun OnboardingScreen() {
    val viewModel = luxViewModel { OnboardingViewModel.create(it) }
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }
    val context = LocalContext.current

    val enabledSources = remember { mutableStateOf(SourceApp.entries.toMutableSet()) }
    var bcaBalance by remember { mutableStateOf("") }
    var seabankBalance by remember { mutableStateOf("") }
    var gopayBalance by remember { mutableStateOf("") }
    var shopeepayBalance by remember { mutableStateOf("") }
    var cashBalance by remember { mutableStateOf("") }

    var assetName by remember { mutableStateOf("") }
    var assetValue by remember { mutableStateOf("") }
    var liabilityName by remember { mutableStateOf("") }
    var liabilityValue by remember { mutableStateOf("") }

    var incomeText by remember { mutableStateOf("") }
    var obligationsText by remember { mutableStateOf("") }
    var savingsText by remember { mutableStateOf("") }
    var investmentText by remember { mutableStateOf("") }
    var bufferText by remember { mutableStateOf("") }

    var goalName by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("") }

    var riskProfile by remember { mutableStateOf(RiskProfile.MODERATE) }

    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when (step) {
            OnboardingStep.WELCOME -> {
                Text("Selamat datang di Lux Wallet", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Catat transaksi dari notifikasi bank dan e-wallet. Kenali arus kasmu, langsung dari perangkat ini.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = { step = OnboardingStep.PRIVACY }) { Text("Mulai sekarang") }
            }

            OnboardingStep.PRIVACY -> {
                Text("Privasi milikmu", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Lux Wallet never connects to your bank, never makes payments, and never uses Gmail as a data source. " +
                        "Semua data keuangan disimpan di perangkat ini.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = { step = OnboardingStep.NOTIFICATION_ACCESS }) { Text("Lanjut") }
            }

            OnboardingStep.NOTIFICATION_ACCESS -> {
                Text("Akses notifikasi", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Lux Wallet needs Notification Access to detect transactions automatically from myBCA, SeaBank, ShopeePay, and GoPay.",
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }) { Text("Buka pengaturan notifikasi") }
                Button(onClick = { step = OnboardingStep.SOURCES }) { Text("Lanjut") }
            }

            OnboardingStep.SOURCES -> {
                Text("Pilih sumber transaksi", style = MaterialTheme.typography.headlineMedium)
                SourceApp.entries.forEach { source ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = enabledSources.value.contains(source),
                            onCheckedChange = { checked ->
                                val next = enabledSources.value.toMutableSet()
                                if (checked) next.add(source) else next.remove(source)
                                enabledSources.value = next
                            }
                        )
                        Text(source.name)
                    }
                }
                Button(onClick = {
                    SourceApp.entries.forEach { viewModel.setSourceEnabled(it, enabledSources.value.contains(it)) }
                    step = OnboardingStep.ACCOUNTS
                }) { Text("Lanjut") }
            }

            OnboardingStep.ACCOUNTS -> {
                Text("Saldo awal rekening", style = MaterialTheme.typography.headlineMedium)
                Text("Isi saldo saat ini, misalnya 1.500.000. Kosongkan rekening yang tidak digunakan.", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(bcaBalance, { bcaBalance = it }, label = { Text("BCA") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(seabankBalance, { seabankBalance = it }, label = { Text("SeaBank") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(gopayBalance, { gopayBalance = it }, label = { Text("GoPay") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(shopeepayBalance, { shopeepayBalance = it }, label = { Text("ShopeePay") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(cashBalance, { cashBalance = it }, label = { Text("Tunai") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(bcaBalance)?.let { viewModel.createAccountIfNamed("BCA", AccountProvider.BCA, AccountKind.BANK, it) }
                    com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(seabankBalance)?.let { viewModel.createAccountIfNamed("SeaBank", AccountProvider.SEABANK, AccountKind.BANK, it) }
                    com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(gopayBalance)?.let { viewModel.createAccountIfNamed("GoPay", AccountProvider.GOPAY, AccountKind.EWALLET, it) }
                    com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(shopeepayBalance)?.let { viewModel.createAccountIfNamed("ShopeePay", AccountProvider.SHOPEEPAY, AccountKind.EWALLET, it) }
                    com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(cashBalance)?.let { viewModel.createAccountIfNamed("Tunai", AccountProvider.CASH, AccountKind.MANUAL, it) }
                    step = OnboardingStep.ASSETS_LIABILITIES
                }) { Text("Lanjut") }
            }

            OnboardingStep.ASSETS_LIABILITIES -> {
                Text("Other assets & liabilities", style = MaterialTheme.typography.headlineMedium)
                Text("Optional — you can add more later in Assets.", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(assetName, { assetName = it }, label = { Text("Asset name (e.g. Deposito)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(assetValue, { assetValue = it }, label = { Text("Value") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(liabilityName, { liabilityName = it }, label = { Text("Liability name (e.g. Credit Card)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(liabilityValue, { liabilityValue = it }, label = { Text("Outstanding amount") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    viewModel.addAssetIfNamed(assetName, AssetClass.DEPOSITO, com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(assetValue) ?: 0)
                    viewModel.addLiabilityIfNamed(liabilityName, LiabilityType.CREDIT_CARD, com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(liabilityValue) ?: 0)
                    step = OnboardingStep.INCOME_OBLIGATIONS
                }) { Text("Lanjut") }
            }

            OnboardingStep.INCOME_OBLIGATIONS -> {
                Text("Income & monthly obligations", style = MaterialTheme.typography.headlineMedium)
                OutlinedTextField(incomeText, { incomeText = it }, label = { Text("Expected monthly income") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(obligationsText, { obligationsText = it }, label = { Text("Fixed obligations") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(savingsText, { savingsText = it }, label = { Text("Savings target") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(investmentText, { investmentText = it }, label = { Text("Investment target") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(bufferText, { bufferText = it }, label = { Text("Safety buffer") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { step = OnboardingStep.GOALS }) { Text("Lanjut") }
            }

            OnboardingStep.GOALS -> {
                Text("Financial goals", style = MaterialTheme.typography.headlineMedium)
                Text("Optional — you can add more later.", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(goalName, { goalName = it }, label = { Text("Goal (e.g. Emergency Fund)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(goalTarget, { goalTarget = it }, label = { Text("Target amount") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    viewModel.addGoalIfNamed(goalName, com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(goalTarget) ?: 0)
                    step = OnboardingStep.RISK_PROFILE
                }) { Text("Lanjut") }
            }

            OnboardingStep.RISK_PROFILE -> {
                Text("Risk profile", style = MaterialTheme.typography.headlineMedium)
                RiskProfile.entries.forEach { profile ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = riskProfile == profile, onClick = { riskProfile = profile })
                        Text(profile.name)
                    }
                }
                Button(onClick = { step = OnboardingStep.FINISH }) { Text("Lanjut") }
            }

            OnboardingStep.FINISH -> {
                Text("All set", style = MaterialTheme.typography.headlineMedium)
                Text("Lux Wallet is ready. You can change any of this later in Settings.", style = MaterialTheme.typography.bodyLarge)
                Button(onClick = {
                    viewModel.finish(
                        expectedMonthlyIncome = com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(incomeText) ?: 0,
                        fixedObligations = com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(obligationsText) ?: 0,
                        savingsTarget = com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(savingsText) ?: 0,
                        investmentTarget = com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(investmentText) ?: 0,
                        safetyBuffer = com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(bufferText) ?: 0,
                        riskProfile = riskProfile
                    )
                }) { Text("Selesai") }
            }
        }
    }
}
