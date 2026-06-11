package com.app.mealmate.ui.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.mealmate.ui.MealMateUiState
import com.app.mealmate.ui.MealMateViewModel
import com.app.mealmate.ui.about.AboutScreen
import com.app.mealmate.ui.detail.DetailScreen
import com.app.mealmate.ui.explore.ExploreScreen
import com.app.mealmate.ui.favorites.FavoritesScreen
import com.app.mealmate.ui.navigation.MealMateDestination
import com.app.mealmate.ui.planner.PlannerScreen

@Composable
fun MealMateNavHost(
    uiState: MealMateUiState,
    viewModel: MealMateViewModel,
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = MealMateDestination.Explore,
        modifier = modifier.padding(contentPadding),
    ) {
        composable(MealMateDestination.Explore) {
            ExploreScreen(
                query = uiState.query,
                meals = uiState.filteredMeals,
                favoriteIds = uiState.favoriteIds,
                isLoading = uiState.isLoading,
                statusMessage = uiState.statusMessage,
                onQueryChange = viewModel::onQueryChange,
                onMealClick = { mealId ->
                    navController.navigate(MealMateDestination.detailRoute(mealId))
                },
                onFavoriteClick = viewModel::toggleFavorite,
            )
        }

        composable(MealMateDestination.Planner) {
            PlannerScreen(
                selectedDay = uiState.selectedPlannerDay,
                items = uiState.selectedDayPlans,
                hasMealPlan = uiState.mealPlan.isNotEmpty(),
                onDaySelected = viewModel::selectPlannerDay,
                onMealClick = { mealId ->
                    navController.navigate(MealMateDestination.detailRoute(mealId))
                },
                onRemovePlan = viewModel::removeMealPlan,
                onGenerateShoppingList = viewModel::generateShoppingList,
                onClearShoppingList = viewModel::clearShoppingList,
                isGeneratingShoppingList = uiState.isGeneratingShoppingList,
                shoppingList = uiState.shoppingList,
                shoppingListError = uiState.shoppingListError,
            )
        }

        composable(MealMateDestination.Favorites) {
            FavoritesScreen(
                meals = uiState.favoriteMeals,
                onMealClick = { mealId ->
                    navController.navigate(MealMateDestination.detailRoute(mealId))
                },
                onRemoveFavorite = viewModel::removeFavorite,
            )
        }

        composable(MealMateDestination.About) {
            AboutScreen()
        }

        composable(
            route = MealMateDestination.Detail,
            arguments = listOf(
                navArgument(MealMateDestination.MealIdArg) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString(MealMateDestination.MealIdArg)
            val meal = mealId?.let { id -> uiState.meals.firstOrNull { it.id == id } }
            DetailScreen(
                meal = meal,
                isFavorite = mealId in uiState.favoriteIds,
                onToggleFavorite = viewModel::toggleFavorite,
                onAddToPlanner = viewModel::addMealPlan,
            )
        }
    }
}
