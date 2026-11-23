package com.safarismart.app.ui.screens.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarismart.app.data.local.Trip
import com.safarismart.app.data.repository.TripRepository
import com.safarismart.app.domain.util.DateUtils
import com.safarismart.app.domain.util.TripFormErrors
import com.safarismart.app.domain.util.Validators
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val origin: String = "",
    val destination: String = "",
    val sacco: String = "",
    val fareText: String = "",
    val delayText: String = "",
    val isPeak: Boolean = false,
    val tripDate: Long = DateUtils.startOfDay(),
    val errors: TripFormErrors = TripFormErrors(),
    val snackbarMessage: String? = null
)

sealed interface AddEditEvent {
    data class Saved(val message: String) : AddEditEvent
}

class AddEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo: TripRepository
) : ViewModel() {

    // The trip id arrives as a navigation argument. -1 means "add a new trip".
    private val tripId: Int = savedStateHandle.get<Int>("tripId") ?: -1

    private val _state = MutableStateFlow(
        AddEditUiState(isEditing = tripId != -1, isLoading = tripId != -1)
    )
    val state: StateFlow<AddEditUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddEditEvent>()
    val events: SharedFlow<AddEditEvent> = _events.asSharedFlow()

    init {
        if (tripId != -1) loadTrip()
    }

    /** UPDATE flow: load the existing trip into the form. */
    private fun loadTrip() {
        viewModelScope.launch {
            val trip = repo.getTrip(tripId)
            _state.update {
                if (trip == null) it.copy(isLoading = false, snackbarMessage = "Trip not found")
                else it.copy(
                    isLoading = false,
                    origin = trip.origin,
                    destination = trip.destination,
                    sacco = trip.sacco,
                    fareText = trip.fare.toString().removeSuffix(".0"),
                    delayText = if (trip.delayMinutes > 0) trip.delayMinutes.toString() else "",
                    isPeak = trip.isPeak,
                    tripDate = trip.tripDate
                )
            }
        }
    }

    // Each change also clears that field's error as soon as the user starts fixing it
    fun onOriginChange(v: String) = _state.update { it.copy(origin = v, errors = it.errors.copy(origin = null)) }
    fun onDestinationChange(v: String) = _state.update { it.copy(destination = v, errors = it.errors.copy(destination = null)) }
    fun onSaccoChange(v: String) = _state.update { it.copy(sacco = v, errors = it.errors.copy(sacco = null)) }
    fun onFareChange(v: String) = _state.update { it.copy(fareText = v, errors = it.errors.copy(fare = null)) }
    fun onDelayChange(v: String) = _state.update { it.copy(delayText = v, errors = it.errors.copy(delay = null)) }
    fun onPeakChange(v: Boolean) = _state.update { it.copy(isPeak = v) }
    fun onDateChange(ms: Long) = _state.update { it.copy(tripDate = DateUtils.startOfDay(ms)) }
    fun clearSnackbar() = _state.update { it.copy(snackbarMessage = null) }

    /** CREATE or UPDATE, after validation and a duplicate check. */
    fun save() {
        val s = state.value
        if (s.isSaving) return

        val errors = TripFormErrors(
            origin = Validators.validateStage(s.origin, "Origin stage"),
            destination = Validators.validateStage(s.destination, "Destination stage")
                ?: Validators.validateDifferent(s.origin, s.destination),
            sacco = Validators.validateStage(s.sacco, "SACCO"),
            fare = Validators.validateFare(s.fareText),
            delay = Validators.validateDelay(s.delayText)
        )
        if (errors.hasErrors) {
            _state.update { it.copy(errors = errors) }
            return
        }

        val trip = Trip(
            id = if (tripId == -1) 0 else tripId, // id = 0 -> Room generates a new one
            origin = s.origin.trim(),
            destination = s.destination.trim(),
            sacco = s.sacco.trim(),
            fare = s.fareText.trim().toDouble(),
            isPeak = s.isPeak,
            delayMinutes = s.delayText.trim().toIntOrNull() ?: 0,
            tripDate = s.tripDate
        )

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                if (repo.isDuplicate(trip)) {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            snackbarMessage = "You already logged this trip for that day"
                        )
                    }
                    return@launch
                }
                if (tripId == -1) repo.addTrip(trip) else repo.updateTrip(trip)
                _state.update { it.copy(isSaving = false) }
                _events.emit(AddEditEvent.Saved(if (tripId == -1) "Trip saved" else "Trip updated"))
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, snackbarMessage = "Could not save trip. Try again.") }
            }
        }
    }
}