package com.safarismart.app.ui.screens.settings

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safarismart.app.SafariSmartApp
import com.safarismart.app.worker.ReminderScheduler
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as SafariSmartApp
    val preferences = app.container.budgetStore
    val reminderTime by preferences.reminderTime.collectAsStateWithLifecycle(
        initialValue = com.safarismart.app.data.prefs.ReminderTime.DEFAULT
    )
    val coroutineScope = rememberCoroutineScope()
    val timeText = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, reminderTime.hour)
        set(Calendar.MINUTE, reminderTime.minute)
    }.time.let { DateFormat.getTimeFormat(context).format(it) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Settings", style = MaterialTheme.typography.headlineSmall) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Daily commute reminder", style = MaterialTheme.typography.titleMedium)
                    Text("Choose when SafariSmart checks your weekly budget and sends its reminder.")
                    Text("Current time: $timeText", style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                coroutineScope.launch {
                                    preferences.setReminderTime(hour, minute)
                                    ReminderScheduler.schedule(context, hour, minute, replaceExisting = true)
                                }
                            },
                            reminderTime.hour,
                            reminderTime.minute,
                            DateFormat.is24HourFormat(context)
                        ).show()
                    }) {
                        Text("Choose reminder time")
                    }
                }
            }
        }
    }
}

