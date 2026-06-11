package com.app.mealmate.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.app.mealmate.domain.model.Meal
import com.app.mealmate.ui.components.CompactMealRow
import com.app.mealmate.ui.components.DeleteIconButton
import com.app.mealmate.ui.components.EmptyState

@Composable
fun FavoritesScreen(
    meals: List<Meal>,
    onMealClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Saved Recipes",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (meals.isEmpty()) {
            EmptyState(
                title = "No saved recipes yet.",
                message = "Start exploring and save meals you like.",
                modifier = Modifier.testTag("empty_favorites"),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("favorites_list"),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = meals,
                    key = { it.id },
                ) { meal ->
                    CompactMealRow(
                        meal = meal,
                        subtitle = "${meal.category} • ${meal.area}",
                        onClick = { onMealClick(meal.id) },
                        trailing = {
                            DeleteIconButton(
                                contentDescription = "Remove favorite ${meal.name}",
                                onClick = { onRemoveFavorite(meal.id) },
                            )
                        },
                    )
                }
            }
        }
    }
}
