package com.luxwallet.app.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Semantic state colors (PRD §42): Expense / Income / Transfer / Warning / Positive / Neutral. */
data class LuxSemanticColors(
    val expense: Color,
    val income: Color,
    val transfer: Color,
    val warning: Color,
    val positive: Color,
    val neutral: Color
)

val LocalLuxSemanticColors = staticCompositionLocalOf {
    LuxSemanticColors(
        expense = ExpenseRedLight,
        income = IncomeGreenLight,
        transfer = TransferBlueLight,
        warning = WarningAmberLight,
        positive = PositiveGreenLight,
        neutral = NeutralGrayLight
    )
}

val LightSemanticColors = LuxSemanticColors(
    expense = ExpenseRedLight,
    income = IncomeGreenLight,
    transfer = TransferBlueLight,
    warning = WarningAmberLight,
    positive = PositiveGreenLight,
    neutral = NeutralGrayLight
)

val DarkSemanticColors = LuxSemanticColors(
    expense = ExpenseRedDark,
    income = IncomeGreenDark,
    transfer = TransferBlueDark,
    warning = WarningAmberDark,
    positive = PositiveGreenDark,
    neutral = NeutralGrayDark
)
