package com.safarismart.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// One DataStore file for the whole app (must be declared at top level)
private val Context.dataStore: DataStore<Preferences> by
    preferencesDataStore(name = "safarismart_prefs")

data class ReminderTime(val hour: Int, val minute: Int) {
    companion object {
        val DEFAULT = ReminderTime(hour = 7, minute = 0)
    }
}

/** Stores offline app preferences used by the UI and background worker. */
class BudgetStore(private val context: Context) {

    private val budgetKey = doublePreferencesKey("weekly_budget")
    private val reminderHourKey = intPreferencesKey("reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("reminder_minute")

    val weeklyBudget: Flow<Double> =
        context.dataStore.data.map { prefs -> prefs[budgetKey] ?: DEFAULT_BUDGET }

    val reminderTime: Flow<ReminderTime> = context.dataStore.data.map { prefs ->
        ReminderTime(
            hour = prefs[reminderHourKey] ?: ReminderTime.DEFAULT.hour,
            minute = prefs[reminderMinuteKey] ?: ReminderTime.DEFAULT.minute
        )
    }

    suspend fun setWeeklyBudget(amount: Double) {
        context.dataStore.edit { prefs -> prefs[budgetKey] = amount }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
        context.dataStore.edit { prefs ->
            prefs[reminderHourKey] = hour
            prefs[reminderMinuteKey] = minute
        }
    }

    /** One-off reads used by the application startup and background worker. */
    suspend fun currentBudget(): Double = weeklyBudget.first()
    suspend fun currentReminderTime(): ReminderTime = reminderTime.first()

    companion object {
        const val DEFAULT_BUDGET = 1500.0
    }
}
