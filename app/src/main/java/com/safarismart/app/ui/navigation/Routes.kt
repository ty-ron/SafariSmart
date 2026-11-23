package com.safarismart.app.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val TRIPS = "trips"
    const val INSIGHTS = "insights"
    const val ADD_EDIT = "add_edit/{tripId}"   // tripId = -1 means "add new"

fun addEdit(tripId: Int = -1) = "add_edit/$tripId"
}
