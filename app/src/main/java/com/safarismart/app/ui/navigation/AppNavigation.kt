package com.safarismart.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safarismart.app.ui.screens.addedit.AddEditTripScreen
import com.safarismart.app.ui.screens.dashboard.DashboardScreen
import com.safarismart.app.ui.screens.insights.InsightsScreen
import com.safarismart.app.ui.screens.splash.SplashScreen
import com.safarismart.app.ui.screens.trips.TripsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val tabRoutes = bottomItems.map { it.route }

    Scaffold(
        // The bottom bar only shows on the three main tabs
        bottomBar = { if (currentRoute in tabRoutes) SafariBottomBar(navController, currentRoute) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = {
                    navController.navigate(Routes.DASHBOARD) {
                        // Remove splash from the back stack so Back exits the app
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }
            composable(Routes.DASHBOARD) {
                DashboardScreen(onAddTrip = { navController.navigate(Routes.addEdit()) })
            }
            composable(Routes.TRIPS) {
                TripsScreen(
                    onAddTrip = { navController.navigate(Routes.addEdit()) },
                    onEditTrip = { id -> navController.navigate(Routes.addEdit(id)) } // pass the trip id
                )
            }
            composable(Routes.INSIGHTS) { InsightsScreen() }
            composable(
                route = Routes.ADD_EDIT,
                arguments = listOf(navArgument("tripId") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) {
                AddEditTripScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}