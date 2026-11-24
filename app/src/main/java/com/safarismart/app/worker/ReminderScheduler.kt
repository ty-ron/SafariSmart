package com.safarismart.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME = "morning_commute_reminder"

    /** Schedules one reminder every 24 hours, first run at the next selected local time. */
    fun schedule(
        context: Context,
        hour: Int = 7,
        minute: Int = 0,
        replaceExisting: Boolean = false
    ) {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }

        val now = Calendar.getInstance()
        val firstRun = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!firstRun.after(now)) firstRun.add(Calendar.DAY_OF_YEAR, 1)

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(firstRun.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
            .build()

        val policy = if (replaceExisting) {
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }
}
