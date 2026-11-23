package com.safarismart.app.domain.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Date helpers using Calendar so they work on API 24 without desugaring. */
object DateUtils {
    private const val DAY_MS = 24L * 60 * 60 * 1000

    fun startOfDay(ms: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** Weeks run Monday to Sunday. */
    fun startOfWeek(ms: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            timeInMillis = startOfDay(ms)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }.timeInMillis

    fun endOfWeek(ms: Long = System.currentTimeMillis()): Long =
        startOfWeek(ms) + 7 * DAY_MS - 1

    fun startOfMonth(ms: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = startOfDay(ms)
            set(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

    fun daysAgo(days: Int): Long = startOfDay() - days * DAY_MS

    fun format(ms: Long): String =
        SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(ms))
}