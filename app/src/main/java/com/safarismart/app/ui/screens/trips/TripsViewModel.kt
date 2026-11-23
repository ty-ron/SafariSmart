package com.safarismart.app.ui.screens.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safarismart.app.data.local.Trip
import com.safarismart.app.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A one-off message for a Snackbar. undoable = show an "Undo" button. */
data class UiMessage(val text: String, val undoable: Boolean = false)

data class TripsUiState(
    val isLoading: Boolean = true,
    val trips: List<Trip> = emptyList(),
    val weeklyTotal: Double = 0.0
)

class TripsViewModel(private val repo: TripRepository) : ViewModel() {

    /** The single object the Trips screen observes. */
    val uiState: StateFlow<TripsUiState> =
        combine(repo.trips, repo.observeWeeklyTotal()) { trips, total ->
            TripsUiState(isLoading = false, trips = trips, weeklyTotal = total)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripsUiState()
        )

    private val _messages = MutableSharedFlow<UiMessage>()
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()

    private var lastDeleted: Trip? = null

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                repo.deleteTrip(trip)
                lastDeleted = trip
                _messages.emit(UiMessage("Trip deleted", undoable = true))
            } catch (e: Exception) {
                _messages.emit(UiMessage("Could not delete trip"))
            }
        }
    }

    fun undoDelete() {
        val trip = lastDeleted ?: return
        viewModelScope.launch {
            repo.addTrip(trip) // same id, so the trip returns exactly as it was
            lastDeleted = null
            _messages.emit(UiMessage("Trip restored"))
        }
    }
}