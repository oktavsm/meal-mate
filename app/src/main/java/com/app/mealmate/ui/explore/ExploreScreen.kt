package com.app.mealmate.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.app.mealmate.domain.model.Meal
import com.app.mealmate.ui.components.EmptyState
import com.app.mealmate.ui.components.MealSearchBar
import com.app.mealmate.ui.components.RecipeCard

@Composable
fun ExploreScreen(
    query: String,
    meals: List<Meal>,
    favoriteIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onMealClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "What do you want to cook today?",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        MealSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.testTag("search_field"),
        )
        Text(
            text = if (query.isBlank()) "Recommended recipes" else "Search results",
            style = MaterialTheme.typography.titleMedium,
        )
        if (meals.isEmpty()) {
            EmptyState(
                title = "No recipes found",
                message = "Try another keyword.",
                modifier = Modifier.testTag("empty_search"),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("recipe_grid"),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(
                    items = meals,
                    key = { it.id },
                ) { meal ->
                    RecipeCard(
                        meal = meal,
                        isFavorite = meal.id in favoriteIds,
                        onClick = { onMealClick(meal.id) },
                        onFavoriteClick = { onFavoriteClick(meal.id) },
                    )
                }
            }
        }
    }
}
