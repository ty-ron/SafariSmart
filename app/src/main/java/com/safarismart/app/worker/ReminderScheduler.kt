package com.safarismart.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME = "morning_commute_reminder"

    /** Schedules one reminder every 24 hours, first run at the next 7:00. */
    fun schedule(context: Context, hour: Int = 7) {
        val now = Calendar.getInstance()
        val firstRun = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (firstRun.before(now)) firstRun.add(Calendar.DAY_OF_YEAR, 1)

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(firstRun.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
            .build()

        // KEEP = do not create a duplicate if it is already scheduled
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}