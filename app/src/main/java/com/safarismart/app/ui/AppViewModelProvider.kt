package com.safarismart.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.safarismart.app.SafariSmartApp
import com.safarismart.app.ui.screens.dashboard.DashboardViewModel
import com.safarismart.app.ui.screens.trips.TripsViewModel

/** One factory for every ViewModel, so screens just write viewModel(factory = AppViewModelProvider.Factory). */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { TripsViewModel(safariApp().container.tripRepository) }
        initializer {
            DashboardViewModel(
                safariApp().container.tripRepository,
                safariApp().container.budgetStore
            )
        }
        // Member 5 adds the AddEditViewModel initializer here
        // Member 6 adds the InsightsViewModel initializer here
    }
}

fun CreationExtras.safariApp(): SafariSmartApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SafariSmartApp