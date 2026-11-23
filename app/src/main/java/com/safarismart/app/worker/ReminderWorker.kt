package com.safarismart.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.safarismart.app.SafariSmartApp
import com.safarismart.app.domain.util.BudgetLevel
import com.safarismart.app.domain.util.FareCalculator
import com.safarismart.app.domain.util.InsightsCalculator
import com.safarismart.app.domain.util.toKes

/** Runs in the background (even if the app is closed) and shows a budget-aware notification. */
class ReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val container = (applicationContext as SafariSmartApp).container
            val spent = container.tripRepository.weeklyTotalNow()
            val budget = container.budgetStore.currentBudget()
            val remaining = FareCalculator.remaining(budget, spent)

            val (title, text) = when (InsightsCalculator.budgetLevel(spent, budget)) {
                BudgetLevel.OVER ->
                    "Weekly budget exceeded" to "You've spent ${spent.toKes()} of ${budget.toKes()}. Plan cheaper trips."
                BudgetLevel.WARNING ->
                    "Budget almost used" to "Only ${remaining.toKes()} left this week."
                BudgetLevel.OK ->
                    "Good morning! Plan your commute" to "${remaining.toKes()} left in your weekly budget."
            }

            NotificationHelper.show(applicationContext, title, text)
            Result.success()
        } catch (e: Exception) {
            // Try again later (WorkManager backs off), give up after 3 attempts
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}