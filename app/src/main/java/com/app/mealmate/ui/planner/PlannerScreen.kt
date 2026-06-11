package com.app.mealmate.ui.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.app.mealmate.domain.model.MealDay
import com.app.mealmate.domain.model.MealPlanItem
import com.app.mealmate.ui.components.CompactMealRow
import com.app.mealmate.ui.components.DeleteIconButton
import com.app.mealmate.ui.components.EmptyState
import com.app.mealmate.ui.components.SelectableChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerScreen(
    selectedDay: MealDay,
    items: List<MealPlanItem>,
    hasMealPlan: Boolean,
    onDaySelected: (MealDay) -> Unit,
    onMealClick: (String) -> Unit,
    onRemovePlan: (String) -> Unit,
    onGenerateShoppingList: () -> Unit,
    onClearShoppingList: () -> Unit,
    isGeneratingShoppingList: Boolean,
    shoppingList: String?,
    shoppingListError: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Meal Planner",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealDay.entries.forEach { day ->
                SelectableChip(
                    text = day.label,
                    selected = selectedDay == day,
                    onClick = { onDaySelected(day) },
                )
            }
        }
        ShoppingListPanel(
            hasMealPlan = hasMealPlan,
            isGenerating = isGeneratingShoppingList,
            shoppingList = shoppingList,
            error = shoppingListError,
            onGenerateClick = onGenerateShoppingList,
            onClearClick = onClearShoppingList,
        )
        if (items.isEmpty()) {
            EmptyState(
                title = "Your meal plan is still empty.",
                message = "Add recipes from the detail page.",
                modifier = Modifier.testTag("empty_planner"),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("planner_list"),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = items,
                    key = { it.id },
                ) { item ->
                    CompactMealRow(
                        meal = item.meal,
                        subtitle = "${item.day.label} • ${item.slot.label}",
                        onClick = { onMealClick(item.meal.id) },
                        trailing = {
                            DeleteIconButton(
                                contentDescription = "Remove meal plan ${item.meal.name}",
                                onClick = { onRemovePlan(item.id) },
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingListPanel(
    hasMealPlan: Boolean,
    isGenerating: Boolean,
    shoppingList: String?,
    error: String?,
    onGenerateClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shopping_list_panel"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "AI Shopping List",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Generate a merged grocery list from your whole planner.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onGenerateClick,
                enabled = hasMealPlan && !isGenerating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                }
                Text(if (isGenerating) "Generating..." else "Generate Shopping List")
            }
            error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            shoppingList?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                        TextButton(
                            onClick = onClearClick,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }
    }
}
