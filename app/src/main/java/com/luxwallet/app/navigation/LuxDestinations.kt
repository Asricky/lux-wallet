package com.luxwallet.app.navigation

object LuxDestinations {
    const val ALERT_SETTINGS = "alert_settings"
    const val CALENDAR = "calendar"
    const val PLANNER = "planner"
    const val COACH = "coach"
    const val HOME = "home"
    const val CALCULATOR = "calculator"
    const val NOTIFICATION_SETTINGS = "notification_settings"
    const val CASHFLOW = "cashflow"
    const val ASSETS = "assets"
    const val MORE = "more"
    const val QUICK_ADD = "quick_add"
    const val TRANSACTIONS = "transactions"
    const val TRANSACTION_DETAIL = "transaction_detail/{transactionId}"
    const val NEEDS_REVIEW = "needs_review"
    const val BUDGETS_GOALS = "budgets_goals"
    const val INSIGHTS = "insights"
    const val CATEGORIES = "categories"
    const val RULES = "rules"
    const val SETTINGS = "settings"
    const val ACCOUNTS = "accounts"
    const val NOTIFICATION_LAB = "notification_lab"

    fun transactionDetail(id: Long) = "transaction_detail/$id"
}
