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
    NavHost(navController = navController, startDestination = startDestination,
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None }) {
        composable(LuxDestinations.REPORTS) { com.luxwallet.app.feature.reports.ReportsScreen() }
        composable(LuxDestinations.ALERT_SETTINGS) { com.luxwallet.app.feature.settings.AlertSettingsScreen() }
        composable(LuxDestinations.HOME) { HomeScreen(onNavigate = { navController.openScreen(it) }) }
        composable(LuxDestinations.CALENDAR) { com.luxwallet.app.feature.calendar.CalendarScreen(onNavigate = { navController.openScreen(it) }) }
        composable(LuxDestinations.PLANNER) { com.luxwallet.app.feature.planner.PlannerScreen() }
        composable(LuxDestinations.COACH) { com.luxwallet.app.feature.coach.CoachScreen(onNavigate = { navController.openScreen(it) }) }
        composable(LuxDestinations.CASHFLOW) { CashflowScreen() }
        composable(LuxDestinations.ASSETS) { AssetsScreen(onAccounts = { navController.openScreen(LuxDestinations.ACCOUNTS) }) }
        composable(LuxDestinations.MORE) { MoreScreen(onNavigate = { navController.openScreen(it) }) }
        composable(LuxDestinations.QUICK_ADD) { QuickAddScreen(onDone = { navController.popBackStack() }, onAccounts = { navController.openScreen(LuxDestinations.ACCOUNTS) }) }
        composable(LuxDestinations.CALCULATOR) { com.luxwallet.app.feature.calculator.CalculatorScreen() }
        composable(LuxDestinations.NOTIFICATION_SETTINGS) {
            com.luxwallet.app.feature.settings.NotificationSettingsScreen(onDiagnostics = { navController.openScreen(LuxDestinations.NOTIFICATION_LAB) })
        }

        composable(LuxDestinations.TRANSACTIONS) {
            TransactionsScreen(onTransactionClick = { id -> navController.openScreen(LuxDestinations.transactionDetail(id)) })
        }
        composable(
            LuxDestinations.TRANSACTION_DETAIL,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            TransactionDetailScreen(transactionId = id, onDone = { navController.openScreen(LuxDestinations.HOME) })
        }

        composable(LuxDestinations.NEEDS_REVIEW) {
            NeedsReviewScreen(onTransactionClick = { id -> navController.openScreen(LuxDestinations.transactionDetail(id)) }, onManual = { navController.openScreen(LuxDestinations.QUICK_ADD) })
        }
        composable(LuxDestinations.BUDGETS_GOALS) { BudgetGoalsScreen() }
        composable(LuxDestinations.INSIGHTS) { InsightsScreen() }
        composable(LuxDestinations.CATEGORIES) { CategoriesScreen() }
        composable(LuxDestinations.RULES) { RulesScreen() }
        composable(LuxDestinations.ACCOUNTS) { AccountsScreen() }
        composable(LuxDestinations.NOTIFICATION_LAB) { NotificationLabScreen() }
        composable(LuxDestinations.SETTINGS) {
            SettingsScreen(
                onOpenNotifications = { navController.openScreen(LuxDestinations.ALERT_SETTINGS) },
                onOpenCoach = { navController.openScreen(LuxDestinations.COACH) },
                onOpenAccounts = { navController.openScreen(LuxDestinations.ACCOUNTS) },
                onOpenCategories = { navController.openScreen(LuxDestinations.CATEGORIES) },
                onOpenRules = { navController.openScreen(LuxDestinations.RULES) },
                onOpenNotificationLab = { navController.openScreen(LuxDestinations.NOTIFICATION_LAB) }
            )
        }
    }
}
