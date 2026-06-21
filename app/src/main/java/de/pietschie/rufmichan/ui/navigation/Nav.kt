package de.pietschie.rufmichan.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.ui.contacts.ContactEditScreen
import de.pietschie.rufmichan.ui.contacts.ContactListScreen
import de.pietschie.rufmichan.ui.schedule.ScheduleCallScreen
import de.pietschie.rufmichan.ui.schedule.ScheduledListScreen
import de.pietschie.rufmichan.ui.settings.SettingsScreen

private sealed class TopDest(val route: String, val labelRes: Int) {
    object Contacts : TopDest("contacts", R.string.contacts)
    object Scheduled : TopDest("scheduled", R.string.scheduled)
    object Settings : TopDest("settings", R.string.settings)
}

@Composable
fun RufMichAnNavHost() {
    val navController = rememberNavController()
    val topDests = listOf(TopDest.Contacts, TopDest.Scheduled, TopDest.Settings)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDest = navBackStackEntry?.destination
            val showBar = topDests.any { it.route == currentDest?.route }
            if (showBar) {
                NavigationBar {
                    topDests.forEach { dest ->
                        NavigationBarItem(
                            icon = {
                                when (dest) {
                                    is TopDest.Contacts -> Icon(Icons.Filled.Contacts, contentDescription = null)
                                    is TopDest.Scheduled -> Icon(Icons.Filled.AccessTime, contentDescription = null)
                                    is TopDest.Settings -> Icon(Icons.Filled.Settings, contentDescription = null)
                                }
                            },
                            label = { Text(stringResource(dest.labelRes)) },
                            selected = currentDest?.hierarchy?.any { it.route == dest.route } == true,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopDest.Contacts.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TopDest.Contacts.route) {
                ContactListScreen(
                    onAddContact = { navController.navigate("contactEdit/new") },
                    onEditContact = { id -> navController.navigate("contactEdit/$id") },
                    onScheduleCall = { id -> navController.navigate("schedule/$id") }
                )
            }
            composable(
                route = "contactEdit/{contactId}",
                arguments = listOf(navArgument("contactId") { type = NavType.StringType })
            ) { backStackEntry ->
                val rawId = backStackEntry.arguments?.getString("contactId")
                val contactId = rawId?.toLongOrNull()
                ContactEditScreen(
                    contactId = contactId,
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(TopDest.Scheduled.route) {
                ScheduledListScreen(
                    onScheduleNew = { navController.navigate("schedule/0") }
                )
            }
            composable(
                route = "schedule/{contactId}",
                arguments = listOf(navArgument("contactId") { type = NavType.LongType })
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getLong("contactId") ?: 0L
                ScheduleCallScreen(
                    preselectedContactId = contactId.takeIf { it != 0L },
                    onScheduled = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(TopDest.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
