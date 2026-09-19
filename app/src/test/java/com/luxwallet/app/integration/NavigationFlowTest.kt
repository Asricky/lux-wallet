package com.luxwallet.app.integration

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.ui.component.MoneyField
import com.luxwallet.app.core.ui.theme.LuxWalletTheme
import com.luxwallet.app.navigation.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class NavigationFlowTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var nav: NavHostController
    private lateinit var back: OnBackPressedDispatcher
    private fun launchShell() {
        compose.setContent {
            nav = rememberNavController()
            back = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
            LuxWalletTheme {
                LuxAppScaffold(nav) { controller ->
                    NavHost(controller, LuxDestinations.HOME) {
                        mainRoutes.forEach { route -> composable(route) {
                            Column {
                                Text("Layar $route")
                                Button({ controller.openScreen(LuxDestinations.CALCULATOR) }) { Text("Buka hitungan") }
                            }
                        } }
                        composable(LuxDestinations.CALCULATOR) { Text("Isi kalkulator") }
                        composable(LuxDestinations.QUICK_ADD) { Text("Isi catatan") }
                    }
                }
            }
        }
    }
    @Test fun allTabsRemainSelectableAndSystemBackReturnsHome() {
        launchShell()
        listOf("Kalender" to "calendar", "Aset" to "assets", "Lainnya" to "more", "Beranda" to "home").forEach { (label, route) ->
            compose.onNodeWithText(label).performClick()
            compose.onNodeWithText("Layar $route").assertIsDisplayed()
        }
        compose.onNodeWithText("Aset").performClick()
        compose.runOnIdle { back.onBackPressed() }
        compose.onNodeWithText("Layar home").assertIsDisplayed()
    }
    @Test fun detailBackReturnsToOriginAndTabsNeverRestoreAStaleForm() {
        launchShell()
        compose.onNodeWithText("Lainnya").performClick()
        compose.onNodeWithText("Buka hitungan").performClick()
        compose.onNodeWithText("Isi kalkulator").assertIsDisplayed()
        compose.onNodeWithText("Aset").assertDoesNotExist()
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Layar more").assertIsDisplayed()
        compose.onNodeWithText("Buka hitungan").performClick()
        compose.runOnIdle { nav.openScreen(LuxDestinations.CALENDAR) }
        compose.onNodeWithText("Lainnya").performClick()
        compose.onNodeWithText("Layar more").assertIsDisplayed()
        compose.onNodeWithText("Isi kalkulator").assertDoesNotExist()
    }
    @Test fun recordIsAnActionAndBackReturnsToTheSelectedTab() {
        launchShell()
        compose.onNodeWithText("Aset").performClick()
        compose.onNodeWithContentDescription("Catat transaksi").performClick()
        compose.onNodeWithText("Isi catatan").assertIsDisplayed()
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Layar assets").assertIsDisplayed()
    }
    @Test fun amountCanBeTypedReplacedAndClearedWithGrouping() {
        var raw = ""
        compose.setContent {
            var value by remember { mutableStateOf("") }
            LuxWalletTheme { MoneyField("Nominal", value, { value = it; raw = it }) }
        }
        compose.onNodeWithText("Nominal").performTextInput("3200000")
        compose.onNodeWithText("3.200.000").assertIsDisplayed()
        assertEquals("3200000", raw)
        compose.onNodeWithText("Nominal").performTextReplacement("45000")
        compose.onNodeWithText("45.000").assertIsDisplayed()
        compose.onNodeWithText("Nominal").performTextClearance()
        assertEquals("", raw)
    }
}
