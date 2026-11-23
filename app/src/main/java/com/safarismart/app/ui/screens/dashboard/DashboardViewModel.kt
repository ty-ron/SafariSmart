package com.safarismart.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarismart.app.data.local.Trip
import com.safarismart.app.data.prefs.BudgetStore
import com.safarismart.app.data.repository.TripRepository
import com.safarismart.app.domain.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val isLoading: Boolean = true,
    val weeklyTotal: Double = 0.0,
    val weeklyBudget: Double = 0.0,
    val tripsThisWeek: Int = 0,
    val recentTrips: List<Trip> = emptyList()
)

class DashboardViewModel(
    repo: TripRepository,
    budgetStore: BudgetStore
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        combine(repo.trips, repo.observeWeeklyTotal(), budgetStore.weeklyBudget) { trips, total, budget ->
            val weekStart = DateUtils.startOfWeek()
            DashboardUiState(
                isLoading = false,
                weeklyTotal = total,
                weeklyBudget = budget,
                tripsThisWeek = trips.count { it.tripDate >= weekStart },
                recentTrips = trips.take(3)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}