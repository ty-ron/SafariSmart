package com.safarismart.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.safarismart.app.data.local.AppDatabase
import com.safarismart.app.data.prefs.BudgetStore
import com.safarismart.app.data.prefs.ReminderTime
import com.safarismart.app.data.repository.TripRepository
import com.safarismart.app.worker.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SafariSmartApp : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch {
            val reminderTime = try {
                container.budgetStore.currentReminderTime()
            } catch (exception: Exception) {
                Log.e(TAG, "Could not load saved reminder time; using the default", exception)
                ReminderTime.DEFAULT
            }
            ReminderScheduler.schedule(this@SafariSmartApp, reminderTime.hour, reminderTime.minute)
        }
    }

    private companion object {
        const val TAG = "SafariSmartApp"
    }
}


/** Creates the long-lived objects once (simple manual dependency injection). */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val tripRepository = TripRepository(database.tripDao())
    val budgetStore = BudgetStore(context)
}
