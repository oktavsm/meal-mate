package com.app.mealmate.ui.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.mealmate.ui.MealMateViewModel
import com.app.mealmate.ui.components.AppHeader
import com.app.mealmate.ui.components.BottomNavItem
import com.app.mealmate.ui.components.MealMateBottomBar
import com.app.mealmate.ui.navigation.MealMateDestination

@Composable
fun MealMateApp(
    viewModel: MealMateViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute in bottomDestinations.map { it.route }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppHeader(
                title = titleForRoute(currentRoute),
                onAboutClick = {
                    if (currentRoute != MealMateDestination.About) {
                        navController.navigate(MealMateDestination.About)
                    }
                },
                onBackClick = if (showBottomBar) null else ({ navController.navigateUp() }),
            )
        },
        bottomBar = {
            if (showBottomBar) {
                MealMateBottomBar(
                    items = bottomDestinations,
                    currentRoute = currentDestination.topLevelRoute(),
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo(MealMateDestination.Explore) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        MealMateNavHost(
            uiState = uiState,
            viewModel = viewModel,
            navController = navController,
            contentPadding = innerPadding,
        )
    }
}

private val bottomDestinations = listOf(
    BottomNavItem(
        route = MealMateDestination.Explore,
        label = "Explore",
        icon = Icons.Rounded.Explore,
    ),
    BottomNavItem(
        route = MealMateDestination.Planner,
        label = "Planner",
        icon = Icons.Rounded.CalendarMonth,
    ),
    BottomNavItem(
        route = MealMateDestination.Favorites,
        label = "Favorites",
        icon = Icons.Rounded.Bookmark,
    ),
)

private fun titleForRoute(route: String?): String = when (route) {
    MealMateDestination.Explore -> "MealMate"
    MealMateDestination.Planner -> "Planner"
    MealMateDestination.Favorites -> "Favorites"
    MealMateDestination.About -> "About Developer"
    MealMateDestination.Detail -> "Recipe Detail"
    else -> "MealMate"
}

private fun NavDestination?.topLevelRoute(): String? {
    return this?.hierarchy?.firstOrNull { destination ->
        destination.route in bottomDestinations.map { it.route }
    }?.route
}
