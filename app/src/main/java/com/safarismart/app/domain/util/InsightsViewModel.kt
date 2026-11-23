package com.safarismart.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarismart.app.data.prefs.BudgetStore
import com.safarismart.app.data.repository.TripRepository
import com.safarismart.app.domain.util.BudgetLevel
import com.safarismart.app.domain.util.Insights
import com.safarismart.app.domain.util.InsightsCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InsightsUiState(
    val isLoading: Boolean = true,
    val hasTrips: Boolean = false,
    val insights: Insights? = null,
    val weeklySpent: Double = 0.0,
    val weeklyBudget: Double = BudgetStore.DEFAULT_BUDGET,
    val level: BudgetLevel = BudgetLevel.OK
)

class InsightsViewModel(
    repo: TripRepository,
    private val budgetStore: BudgetStore
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> =
        combine(repo.trips, repo.observeWeeklyTotal(), budgetStore.weeklyBudget) { trips, spent, budget ->
            InsightsUiState(
                isLoading = false,
                hasTrips = trips.isNotEmpty(),
                insights = InsightsCalculator.compute(trips),
                weeklySpent = spent,
                weeklyBudget = budget,
                level = InsightsCalculator.budgetLevel(spent, budget)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    /** Returns an error message, or null when the budget was saved. */
    fun saveBudget(text: String): String? {
        val amount = text.trim().toDoubleOrNull() ?: return "Enter a valid amount"
        if (amount <= 0.0) return "Budget must be more than 0"
        if (amount > 100_000.0) return "Budget looks too high"
        viewModelScope.launch { budgetStore.setWeeklyBudget(amount) }
        return null
    }
}