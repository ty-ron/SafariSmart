package com.safarismart.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

data class BottomItem(val route: String, val label: String, val icon: ImageVector)

val bottomItems = listOf(
    BottomItem(Routes.DASHBOARD, "Home", Icons.Filled.Home),
    BottomItem(Routes.TRIPS, "Trips", Icons.AutoMirrored.Filled.List),
    BottomItem(Routes.INSIGHTS, "Insights", Icons.Filled.BarChart)
)

@Composable
fun SafariBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        bottomItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Keep ONE copy of each tab and restore its state
                        popUpTo(Routes.DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}