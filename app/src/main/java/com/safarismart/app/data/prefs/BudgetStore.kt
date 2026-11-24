package com.safarismart.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// One DataStore file for the whole app (must be declared at top level)
private val Context.dataStore: DataStore<Preferences> by
preferencesDataStore(name = "safarismart_prefs")

/** Stores the user's weekly fare budget. Survives app restarts and works offline. */
class BudgetStore(private val context: Context) {

    private val budgetKey = doublePreferencesKey("weekly_budget")

    val weeklyBudget: Flow<Double> =
        context.dataStore.data.map { prefs -> prefs[budgetKey] ?: DEFAULT_BUDGET }

    suspend fun setWeeklyBudget(amount: Double) {
        context.dataStore.edit { prefs -> prefs[budgetKey] = amount }
    }

    /** One-off read, used by the background worker. */
    suspend fun currentBudget(): Double = weeklyBudget.first()

    companion object {
        const val DEFAULT_BUDGET = 1500.0
    }
}