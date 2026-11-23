package com.safarismart.app

import android.app.Application
import android.content.Context
import com.safarismart.app.data.local.AppDatabase
import com.safarismart.app.data.prefs.BudgetStore
import com.safarismart.app.data.repository.TripRepository

class SafariSmartApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Member 6 adds: ReminderScheduler.schedule(this)
    }
}

/** Creates the long-lived objects once (simple manual dependency injection). */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val tripRepository = TripRepository(database.tripDao())
    val budgetStore = BudgetStore(context)
}