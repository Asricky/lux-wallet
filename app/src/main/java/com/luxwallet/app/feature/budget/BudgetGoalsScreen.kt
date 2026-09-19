package com.luxwallet.app.feature.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.database.entity.GoalEntity
import com.luxwallet.app.core.model.BudgetStatus
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.core.ui.theme.LocalLuxSemanticColors

@Composable
fun BudgetGoalsScreen() {
    val viewModel = luxViewModel { BudgetGoalsViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()

    var incomeText by remember(state.profile.expectedMonthlyIncome) { mutableStateOf(state.profile.expectedMonthlyIncome.toString()) }
    var obligationsText by remember(state.profile.fixedObligations) { mutableStateOf(state.profile.fixedObligations.toString()) }
    var savingsText by remember(state.profile.savingsTargetMonthly) { mutableStateOf(state.profile.savingsTargetMonthly.toString()) }
    var investmentText by remember(state.profile.investmentTargetMonthly) { mutableStateOf(state.profile.investmentTargetMonthly.toString()) }
    var newGoalName by remember { mutableStateOf("") }
    var newGoalTarget by remember { mutableStateOf("") }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("MONTHLY PLAN", style = MaterialTheme.typography.labelMedium) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(incomeText, { incomeText = it }, label = { Text("Expected Monthly Income") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(obligationsText, { obligationsText = it }, label = { Text("Fixed Obligations") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(savingsText, { savingsText = it }, label = { Text("Savings Target") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(investmentText, { investmentText = it }, label = { Text("Investment Target") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        viewModel.saveProfile(
                            state.profile.copy(
                                expectedMonthlyIncome = incomeText.toLongOrNull() ?: 0,
                                fixedObligations = obligationsText.toLongOrNull() ?: 0,
                                savingsTargetMonthly = savingsText.toLongOrNull() ?: 0,
                                investmentTargetMonthly = investmentText.toLongOrNull() ?: 0
                            )
                        )
                    }) { Text("Save Plan") }
                }
            }
        }

        item { Text("BUDGETS", style = MaterialTheme.typography.labelMedium) }
        state.overallBudgetLine?.let { line -> item { BudgetLineCard(line) } }
        items(state.categoryLines) { line -> BudgetLineCard(line) }
        if (state.overallBudgetLine == null && state.categoryLines.isEmpty()) {
            item { Text("No budgets set yet.", style = MaterialTheme.typography.bodyMedium) }
        }

        item { Text("GOALS", style = MaterialTheme.typography.labelMedium) }
        items(state.goals) { goal -> GoalCard(goal) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add a goal", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(newGoalName, { newGoalName = it }, label = { Text("Goal name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(newGoalTarget, { newGoalTarget = it }, label = { Text("Target amount") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        val target = newGoalTarget.toLongOrNull()
                        if (newGoalName.isNotBlank() && target != null) {
                            viewModel.addGoal(GoalEntity(name = newGoalName, targetAmount = target, createdAt = System.currentTimeMillis()))
                            newGoalName = ""
                            newGoalTarget = ""
                        }
                    }) { Text("Add Goal") }
                }
            }
        }
    }
}

@Composable
private fun BudgetLineCard(line: BudgetLine) {
    val semantic = LocalLuxSemanticColors.current
    val color = when (line.status) {
        BudgetStatus.ON_TRACK -> semantic.positive
        BudgetStatus.WATCH -> semantic.warning
        BudgetStatus.OVER_BUDGET -> semantic.expense
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(line.categoryName, fontWeight = FontWeight.Medium)
                Text(line.status.name.replace('_', ' '), color = color, style = MaterialTheme.typography.labelLarge)
            }
            LinearProgressIndicator(
                progress = { if (line.budgetAmount > 0) (line.spent.toFloat() / line.budgetAmount.toFloat()).coerceIn(0f, 1f) else 0f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            Text("${AmountFormat.rupiah(line.spent)} of ${AmountFormat.rupiah(line.budgetAmount)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun GoalCard(goal: GoalEntity) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(goal.name, fontWeight = FontWeight.Medium)
            LinearProgressIndicator(
                progress = { if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f) else 0f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            Text("${AmountFormat.rupiah(goal.currentAmount)} of ${AmountFormat.rupiah(goal.targetAmount)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
