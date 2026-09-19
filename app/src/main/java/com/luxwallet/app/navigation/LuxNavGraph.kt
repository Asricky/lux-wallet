package com.luxwallet.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.luxwallet.app.feature.accounts.AccountsScreen
import com.luxwallet.app.feature.assets.AssetsScreen
import com.luxwallet.app.feature.budget.BudgetGoalsScreen
import com.luxwallet.app.feature.cashflow.CashflowScreen
import com.luxwallet.app.feature.categories.CategoriesScreen
import com.luxwallet.app.feature.home.HomeScreen
import com.luxwallet.app.feature.insights.InsightsScreen
import com.luxwallet.app.feature.notificationlab.NotificationLabScreen
import com.luxwallet.app.feature.quickadd.QuickAddScreen
import com.luxwallet.app.feature.review.NeedsReviewScreen
import com.luxwallet.app.feature.rules.RulesScreen
import com.luxwallet.app.feature.settings.SettingsScreen
import com.luxwallet.app.feature.transactions.TransactionDetailScreen
import com.luxwallet.app.feature.transactions.TransactionsScreen

@Composable
fun LuxNavGraph(navController: NavHostController, startDestination: String = LuxDestinations.HOME) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(LuxDestinations.HOME) { HomeScreen(onNavigate = { navController.navigate(it) }) }
        composable(LuxDestinations.CASHFLOW) { CashflowScreen() }
        composable(LuxDestinations.ASSETS) { AssetsScreen() }
        composable(LuxDestinations.MORE) { MoreScreen(onNavigate = { navController.navigate(it) }) }
        composable(LuxDestinations.QUICK_ADD) { QuickAddScreen(onDone = { navController.popBackStack() }) }

        composable(LuxDestinations.TRANSACTIONS) {
            TransactionsScreen(onTransactionClick = { id -> navController.navigate(LuxDestinations.transactionDetail(id)) })
        }
        composable(
            LuxDestinations.TRANSACTION_DETAIL,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            TransactionDetailScreen(transactionId = id)
        }

        composable(LuxDestinations.NEEDS_REVIEW) {
            NeedsReviewScreen(onTransactionClick = { id -> navController.navigate(LuxDestinations.transactionDetail(id)) })
        }
        composable(LuxDestinations.BUDGETS_GOALS) { BudgetGoalsScreen() }
        composable(LuxDestinations.INSIGHTS) { InsightsScreen() }
        composable(LuxDestinations.CATEGORIES) { CategoriesScreen() }
        composable(LuxDestinations.RULES) { RulesScreen() }
        composable(LuxDestinations.ACCOUNTS) { AccountsScreen() }
        composable(LuxDestinations.NOTIFICATION_LAB) { NotificationLabScreen() }
        composable(LuxDestinations.SETTINGS) {
            SettingsScreen(
                onOpenAccounts = { navController.navigate(LuxDestinations.ACCOUNTS) },
                onOpenCategories = { navController.navigate(LuxDestinations.CATEGORIES) },
                onOpenRules = { navController.navigate(LuxDestinations.RULES) },
                onOpenNotificationLab = { navController.navigate(LuxDestinations.NOTIFICATION_LAB) }
            )
        }
    }
}
