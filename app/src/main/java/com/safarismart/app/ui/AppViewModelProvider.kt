package com.safarismart.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.safarismart.app.SafariSmartApp
import com.safarismart.app.ui.screens.dashboard.DashboardViewModel
import com.safarismart.app.ui.screens.trips.TripsViewModel
import androidx.lifecycle.createSavedStateHandle
import com.safarismart.app.ui.screens.addedit.AddEditViewModel
import com.safarismart.app.ui.screens.insights.InsightsViewModel

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
        initializer {
            AddEditViewModel(
                this.createSavedStateHandle(),
                safariApp().container.tripRepository
            )
        }
        initializer {
            InsightsViewModel(
                safariApp().container.tripRepository,
                safariApp().container.budgetStore
            )
        }
    }
}

fun CreationExtras.safariApp(): SafariSmartApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SafariSmartApp