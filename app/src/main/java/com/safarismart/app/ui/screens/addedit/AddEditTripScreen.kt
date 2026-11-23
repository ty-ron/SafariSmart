package com.safarismart.app.ui.screens.addedit

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import com.safarismart.app.domain.util.DateUtils
import com.safarismart.app.ui.AppViewModelProvider
import com.safarismart.app.ui.components.LoadingIndicator
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTripScreen(
    onDone: () -> Unit,
    viewModel: AddEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    // After a successful save: show a toast and go back
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is AddEditEvent.Saved) {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                onDone()
            }
        }
    }

    // Duplicate or save errors appear as a Snackbar
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit trip" else "Add trip") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField("Origin stage", state.origin, state.errors.origin, viewModel::onOriginChange)
                FormField("Destination stage", state.destination, state.errors.destination, viewModel::onDestinationChange)
                FormField("Matatu SACCO", state.sacco, state.errors.sacco, viewModel::onSaccoChange)
                FormField("Fare paid (KES)", state.fareText, state.errors.fare, viewModel::onFareChange, KeyboardType.Decimal)
                FormField("Minutes late (optional)", state.delayText, state.errors.delay, viewModel::onDelayChange, KeyboardType.Number)

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (state.isPeak) "Peak hours" else "Off-peak")
                    Switch(checked = state.isPeak, onCheckedChange = viewModel::onPeakChange)
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Date: ${DateUtils.format(state.tripDate)}")
                }

                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isEditing) "Update trip" else "Save trip")
                }
            }
        }
    }

    if (showDatePicker) {
        // The picker works in UTC, our dates are local midnight, so shift by the time-zone offset
        val offset = TimeZone.getDefault().getOffset(state.tripDate)
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.tripDate + offset)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(viewModel::onDateChange)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    error: String?,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { error?.let { Text(it) } }, // inline error message
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}