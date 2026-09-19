package com.luxwallet.app.core.database

/** Default category seed data, PRD §22. Main category name -> subcategory names. */
object DefaultCategories {
    val MAIN_CATEGORIES: List<String> = listOf(
        "Food & Drink", "Transportation", "Shopping", "Bills", "Entertainment",
        "Health", "Education", "Travel", "Financial", "Income", "Transfer", "Other"
    )

    val SUBCATEGORIES: Map<String, List<String>> = mapOf(
        "Food & Drink" to listOf("Restaurant", "Coffee", "Delivery", "Groceries"),
        "Transportation" to listOf("Fuel", "Ride Hailing", "Public Transport", "Parking"),
        "Bills" to listOf("Electricity", "Water", "Internet", "Mobile"),
        "Income" to listOf("Salary", "Bonus", "Interest", "Cashback", "Refund")
    )

    val INCOME_CATEGORY_NAMES: Set<String> = setOf("Income")
}
