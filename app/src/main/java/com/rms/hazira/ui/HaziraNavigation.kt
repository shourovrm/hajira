package com.rms.hazira.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rms.hazira.ui.dues.DuesScreen
import com.rms.hazira.ui.dues.PersonStatementScreen
import com.rms.hazira.ui.month.MonthScreen
import com.rms.hazira.ui.people.PeopleScreen
import com.rms.hazira.ui.people.PersonEditScreen
import com.rms.hazira.ui.today.TodayScreen
import java.time.YearMonth

private const val ROUTE_TODAY = "today"
private const val ROUTE_PEOPLE = "people"
private const val ROUTE_MONTH = "month"
private const val ROUTE_DUES = "dues"
private const val ROUTE_PERSON_EDIT = "person-edit"
private const val ROUTE_STATEMENT = "statement"

private const val ARGUMENT_PERSON_ID = "personId"
private const val ARGUMENT_MONTH = "month"

/** personId value that means "add a new person" on the edit route. */
private const val NEW_PERSON_ID = 0L

/**
 * One tab of the bottom bar. The taka sign is drawn as text because the core icon set has no
 * currency glyph and the extended set is not available on this build machine.
 */
private data class Tab(val route: String, val label: String, val icon: ImageVector?, val glyph: String = "")

private val tabs = listOf(
    Tab(ROUTE_TODAY, "Today", Icons.Filled.CheckCircle),
    Tab(ROUTE_PEOPLE, "People", Icons.Filled.Person),
    Tab(ROUTE_MONTH, "Month", Icons.Filled.DateRange),
    Tab(ROUTE_DUES, "Dues", icon = null, glyph = "৳"),
)

@Composable
fun HaziraNavigation() {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Detail screens (edit form, statement) hide the bar so Back is the only way out.
            if (tabs.any { tab -> tab.route == currentRoute }) {
                BottomBar(currentRoute = currentRoute, onSelect = { route -> navController.openTab(route) })
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_TODAY,
            // Consuming the insets stops each screen's own Scaffold from padding for them again.
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            composable(ROUTE_TODAY) {
                TodayScreen(onAddPerson = { navController.openPersonEdit(NEW_PERSON_ID) })
            }
            composable(ROUTE_PEOPLE) {
                PeopleScreen(
                    onAddPerson = { navController.openPersonEdit(NEW_PERSON_ID) },
                    onEditPerson = { personId -> navController.openPersonEdit(personId) },
                )
            }
            composable(ROUTE_MONTH) {
                MonthScreen()
            }
            composable(ROUTE_DUES) {
                DuesScreen(
                    onOpenStatement = { personId, month ->
                        navController.navigate("$ROUTE_STATEMENT/$personId/$month")
                    },
                )
            }
            composable(
                route = "$ROUTE_PERSON_EDIT/{$ARGUMENT_PERSON_ID}",
                arguments = listOf(navArgument(ARGUMENT_PERSON_ID) { type = NavType.LongType }),
            ) { entry ->
                val personId = entry.arguments?.getLong(ARGUMENT_PERSON_ID) ?: NEW_PERSON_ID
                PersonEditScreen(
                    personId = if (personId == NEW_PERSON_ID) null else personId,
                    onDone = { navController.popBackStack() },
                )
            }
            composable(
                route = "$ROUTE_STATEMENT/{$ARGUMENT_PERSON_ID}/{$ARGUMENT_MONTH}",
                arguments = listOf(
                    navArgument(ARGUMENT_PERSON_ID) { type = NavType.LongType },
                    navArgument(ARGUMENT_MONTH) { type = NavType.StringType },
                ),
            ) { entry ->
                val personId = entry.arguments?.getLong(ARGUMENT_PERSON_ID) ?: NEW_PERSON_ID
                val monthText = entry.arguments?.getString(ARGUMENT_MONTH)
                PersonStatementScreen(
                    personId = personId,
                    month = if (monthText == null) YearMonth.now() else YearMonth.parse(monthText),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(currentRoute: String?, onSelect: (String) -> Unit) {
    NavigationBar {
        for (tab in tabs) {
            NavigationBarItem(
                selected = tab.route == currentRoute,
                onClick = { onSelect(tab.route) },
                icon = {
                    if (tab.icon != null) {
                        Icon(imageVector = tab.icon, contentDescription = null)
                    } else {
                        Text(text = tab.glyph)
                    }
                },
                label = { Text(text = tab.label) },
                // The default selected label uses the secondary colour, which is ink blue here
                // and reads as a link next to the green selected icon.
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

private fun NavHostController.openTab(route: String) {
    navigate(route) {
        // Tabs replace each other instead of stacking, so Back from any tab leaves the app
        // after returning to Today.
        popUpTo(ROUTE_TODAY) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.openPersonEdit(personId: Long) {
    navigate("$ROUTE_PERSON_EDIT/$personId")
}
