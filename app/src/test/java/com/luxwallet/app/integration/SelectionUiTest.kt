package com.luxwallet.app.integration

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.luxwallet.app.core.ui.component.ChoiceField
import com.luxwallet.app.core.ui.theme.LuxThemePreference
import com.luxwallet.app.core.ui.theme.LuxWalletTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SelectionUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun bottomSheetSelectsAnAccountAndCloses() {
        var selected = -1
        compose.setContent {
            var value by remember { mutableStateOf("Pilih rekening") }
            LuxWalletTheme(LuxThemePreference.LIGHT) {
                ChoiceField("Rekening", value, listOf("BCA", "SeaBank"), { selected = it; value = listOf("BCA", "SeaBank")[it] })
            }
        }
        compose.onNodeWithText("Pilih rekening").performClick()
        compose.onNodeWithText("SeaBank").performClick()
        compose.onNodeWithText("SeaBank").assertIsDisplayed()
        compose.onNodeWithText("BCA").assertDoesNotExist()
        assertEquals(1, selected)
    }
    @Test fun selectionWorksInDarkModeToo() {
        var selected = -1
        compose.setContent {
            LuxWalletTheme(LuxThemePreference.DARK) {
                ChoiceField("Periode", "September 2026", listOf("September 2026", "Agustus 2026"), { selected = it })
            }
        }
        compose.onNodeWithText("September 2026").performClick()
        compose.onNodeWithText("Agustus 2026").performClick()
        assertEquals(1, selected)
    }
}
