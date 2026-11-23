package com.safarismart.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safarismart.app.domain.util.toKes
import com.safarismart.app.ui.AppViewModelProvider
import com.safarismart.app.ui.components.EmptyState
import com.safarismart.app.ui.components.LoadingIndicator
import com.safarismart.app.ui.components.TripCard

@Composable
fun DashboardScreen(
    onAddTrip: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTrip,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Log trip") }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator(Modifier.padding(padding))
            state.recentTrips.isEmpty() -> EmptyState(
                icon = Icons.Filled.DirectionsBus,
                title = "No trips yet",
                message = "Tap Log trip to record your first matatu ride.",
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("Your week", style = MaterialTheme.typography.headlineSmall) }
                item { WeeklySummaryCard(state) }
                item { Text("Recent trips", style = MaterialTheme.typography.titleMedium) }
                items(state.recentTrips, key = { it.id }) { trip ->
                    TripCard(trip = trip, onClick = {})
                }
            }
        }
    }
}

@Composable
private fun WeeklySummaryCard(state: DashboardUiState) {
    val fraction = if (state.weeklyBudget > 0)
        (state.weeklyTotal / state.weeklyBudget).toFloat().coerceIn(0f, 1f)
    else 0f
    val overBudget = state.weeklyBudget > 0 && state.weeklyTotal >= state.weeklyBudget

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Spent this week", style = MaterialTheme.typography.labelLarge)
            Text(
                state.weeklyTotal.toKes(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth(),
                color = if (overBudget) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "of ${state.weeklyBudget.toKes()} budget • ${state.tripsThisWeek} trips",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}