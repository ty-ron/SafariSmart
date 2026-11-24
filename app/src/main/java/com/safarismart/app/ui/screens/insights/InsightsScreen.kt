package com.safarismart.app.ui.screens.insights

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.safarismart.app.domain.util.BudgetLevel
import com.safarismart.app.domain.util.FareCalculator
import com.safarismart.app.domain.util.toKes
import com.safarismart.app.ui.AppViewModelProvider
import com.safarismart.app.ui.components.EmptyState
import com.safarismart.app.ui.components.LoadingIndicator
import com.safarismart.app.worker.ReminderWorker
import kotlin.math.roundToInt

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Android 13+ needs the user's permission before the reminder can show
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* if denied, the worker simply skips the notification */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33)
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    if (state.isLoading) {
        LoadingIndicator()
        return
    }
    if (!state.hasTrips) {
        EmptyState(
            Icons.Filled.BarChart,
            "No insights yet",
            "Log a few trips and your budget and delay insights will show up here."
        )
        return
    }
    val insights = state.insights ?: return

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Insights", style = MaterialTheme.typography.headlineSmall) }

        // 1. Weekly budget
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Weekly budget", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${state.weeklySpent.toKes()} of ${state.weeklyBudget.toKes()} " +
                                "(${FareCalculator.budgetUsedPercent(state.weeklySpent, state.weeklyBudget)}%)"
                    )
                    LinearProgressIndicator(
                        progress = { (state.weeklySpent / state.weeklyBudget).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (state.level == BudgetLevel.OK)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                    Text(
                        when (state.level) {
                            BudgetLevel.OK -> "You're on track."
                            BudgetLevel.WARNING -> "Careful: you've used over 80% of your budget."
                            BudgetLevel.OVER -> "You're over budget this week."
                        }
                    )
                    BudgetEditor(current = state.weeklyBudget, onSave = viewModel::saveBudget)
                }
            }
        }

        // 2. Peak vs off-peak
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Peak vs off-peak", style = MaterialTheme.typography.titleMedium)
                    Text("Average peak fare: ${insights.averagePeakFare.toKes()}")
                    Text("Average off-peak fare: ${insights.averageOffPeakFare.toKes()}")
                    Text(
                        insights.peakExtraPercent?.let { "Peak trips cost about $it% more." }
                            ?: "Log both peak and off-peak trips to compare."
                    )
                }
            }
        }

        // 3. Delays per route
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Delays by route", style = MaterialTheme.typography.titleMedium)
                    if (insights.routeDelays.isEmpty()) {
                        Text("No delays logged. Nice!")
                    } else {
                        insights.routeDelays.forEach { d ->
                            Text("${d.route}: ${d.averageDelay.roundToInt()} min on average (${d.trips} trips)")
                        }
                    }
                }
            }
        }

        // 4. Month summary
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("This month", style = MaterialTheme.typography.titleMedium)
                    Text("Total spent: ${insights.monthTotal.toKes()}")
                    Text("Most used route: ${insights.mostUsedRoute ?: "-"}")
                }
            }
        }

        // 5. Reminder (also the demo button for WorkManager)
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Morning reminder", style = MaterialTheme.typography.titleMedium)
                    Text("Choose your daily reminder time in Settings.")
                    OutlinedButton(onClick = {
                        WorkManager.getInstance(context)
                            .enqueue(OneTimeWorkRequestBuilder<ReminderWorker>().build())
                    }) { Text("Send test reminder now") }
                }
            }
        }
    }
}

@Composable
private fun BudgetEditor(current: Double, onSave: (String) -> String?) {
    var text by remember(current) { mutableStateOf(current.toInt().toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; error = null },
            label = { Text("New weekly budget (KES)") },
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = { error = onSave(text) },
            modifier = Modifier.padding(top = 8.dp)
        ) { Text("Save") }
    }
}
