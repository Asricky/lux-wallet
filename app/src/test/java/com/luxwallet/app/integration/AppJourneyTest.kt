package com.luxwallet.app.integration

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.model.*
import com.luxwallet.app.core.ui.theme.LuxWalletTheme
import com.luxwallet.app.navigation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = LuxWalletApp::class)
class AppJourneyTest {
    @get:Rule val compose = createComposeRule()
    private val app get() = ApplicationProvider.getApplicationContext<LuxWalletApp>()
    private lateinit var nav: NavHostController
    private lateinit var back: OnBackPressedDispatcher
    @Before fun prepare() = runBlocking {
        app.preferences.planV2Ready.first { it }
        app.accountRepository.createAccount("Rekening uji", AccountKind.BANK, AccountProvider.BCA, 100_000, 0)
        app.categoryRepository.resolveOrCreateTopLevel("Makan uji")
        Unit
    }
    @After fun cleanUp() = runBlocking {
        app.applicationScope.coroutineContext[Job]?.cancelAndJoin()
        app.database.close()
    }
    private fun launch() {
        compose.setContent {
            nav = rememberNavController()
            back = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
            LuxWalletTheme { LuxAppScaffold(nav) }
        }
    }
    @Test fun actualManualEntryCanReturnEditSaveAndNavigateAgain() {
        launch()
        compose.onNodeWithContentDescription("Catat transaksi").performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText("Pilih rekening").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Nominal").performTextInput("3000")
        compose.onNodeWithText("Pilih rekening").performScrollTo().performClick()
        compose.onNodeWithText("Rekening uji").performClick()
        compose.onNodeWithText("Pilih kategori").performScrollTo().performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Makan uji"))
        compose.onNodeWithText("Makan uji").performClick()
        compose.onNodeWithText("Lanjut ke ringkasan").performScrollTo().performClick()
        compose.onNodeWithText("Periksa catatan").assertIsDisplayed()
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("3.000").assertIsDisplayed()
        compose.onNodeWithText("Lanjut ke ringkasan").performScrollTo().performClick()
        compose.onNodeWithText("Simpan transaksi").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText("LUX WALLET").fetchSemanticsNodes().isNotEmpty() }
        val transactions = runBlocking { app.transactionRepository.observeAll().first() }
        Assert.assertEquals(1, transactions.count { it.isManual && it.amount == 3000L })
        compose.onNodeWithText("Kalender").performClick()
        compose.onNodeWithText("Kalender keuangan").assertIsDisplayed()
        compose.onNodeWithText("Aset").performClick()
        compose.onNodeWithText("Aset saya").assertIsDisplayed()
        compose.runOnIdle { back.onBackPressed() }
        compose.onNodeWithText("LUX WALLET").assertIsDisplayed()
    }
    @Test fun abandoningDraftRequiresAnExplicitChoiceThenReturns() {
        launch()
        compose.onNodeWithContentDescription("Catat transaksi").performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText("Nominal").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Nominal").performTextInput("3200000")
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Lanjut mengisi").performClick()
        compose.onNodeWithText("3.200.000").assertIsDisplayed()
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Buang catatan").performClick()
        compose.onNodeWithText("LUX WALLET").assertIsDisplayed()
        Assert.assertTrue(runBlocking { app.transactionRepository.observeAll().first() }.none { it.isManual })
    }
    @Test
    @Config(sdk = [34], application = LuxWalletApp::class, qualifiers = "w393dp-h851dp-mdpi")
    @org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
    fun renderHomeAndCalendarInBothThemes() {
        runBlocking {
            app.preferences.setAmountsHidden(false)
            val today = java.time.LocalDate.now()
            app.paydayPlanRepository.save(com.luxwallet.app.engine.PaydayPlan(System.currentTimeMillis(), today.toEpochDay(),
                com.luxwallet.app.engine.PaydayPlan.nextPayday(today).toEpochDay(), 100_000))
        }
        var theme by mutableStateOf(com.luxwallet.app.core.ui.theme.LuxThemePreference.LIGHT)
        lateinit var androidView: android.view.View
        compose.setContent {
            androidView = androidx.compose.ui.platform.LocalView.current
            nav = rememberNavController()
            LuxWalletTheme(theme) { LuxAppScaffold(nav) }
        }
        compose.waitUntil(5000) { compose.onAllNodesWithText("LUX WALLET").fetchSemanticsNodes().isNotEmpty() && compose.onAllNodesWithText("Memuat…").fetchSemanticsNodes().isEmpty() }
        val dir = java.io.File("build/reports/ui").apply { mkdirs() }
        fun capture(name: String) {
            compose.waitForIdle()
            val bitmap = android.graphics.Bitmap.createBitmap(androidView.width, androidView.height, android.graphics.Bitmap.Config.ARGB_8888)
            compose.runOnIdle { androidView.draw(android.graphics.Canvas(bitmap)) }
            java.io.File(dir, name).outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
        capture("home-light.png")
        compose.onNodeWithText("Kalender").performClick()
        compose.onNodeWithText("Kalender keuangan").assertIsDisplayed()
        capture("calendar-light.png")
        compose.runOnIdle { theme = com.luxwallet.app.core.ui.theme.LuxThemePreference.DARK }
        capture("calendar-dark.png")
        compose.onNodeWithText("Beranda").performClick()
        capture("home-dark.png")
    }
}
