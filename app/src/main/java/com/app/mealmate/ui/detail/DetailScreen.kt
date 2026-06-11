package com.app.mealmate.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.mealmate.R
import com.app.mealmate.domain.model.Meal
import com.app.mealmate.domain.model.MealDay
import com.app.mealmate.domain.model.MealSlot
import com.app.mealmate.ui.components.EmptyState
import com.app.mealmate.ui.components.MetaPill
import com.app.mealmate.ui.components.PrimaryMealButton
import com.app.mealmate.ui.components.SelectableChip
import com.app.mealmate.ui.theme.MealMateMutedText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    meal: Meal?,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onAddToPlanner: (String, MealDay, MealSlot) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (meal == null) {
        EmptyState(
            title = "Recipe not found",
            message = "This recipe is no longer available.",
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    var showPlannerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("detail_screen"),
    ) {
        AsyncImage(
            model = meal.thumbnailUrl,
            contentDescription = meal.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.meal_placeholder),
            error = painterResource(R.drawable.meal_placeholder),
        )
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.headlineLarge,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetaPill(meal.category)
                    MetaPill(meal.area)
                    meal.tags.forEach { tag -> MetaPill(tag) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onToggleFavorite(meal.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("favorite_detail_button"),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                    )
                    Text(
                        text = if (isFavorite) "Saved" else "Save",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                PrimaryMealButton(
                    text = "Add to Planner",
                    onClick = { showPlannerDialog = true },
                    modifier = Modifier.weight(1f),
                )
            }

            DetailSection(title = "Ingredients") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    meal.ingredients.forEach { ingredient ->
                        Text(
                            text = "${ingredient.measure} ${ingredient.name}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            DetailSection(title = "Instructions") {
                Text(
                    text = meal.instructions,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MealMateMutedText,
                )
            }
        }
    }

    if (showPlannerDialog) {
        PlannerPickerDialog(
            mealName = meal.name,
            onDismiss = { showPlannerDialog = false },
            onConfirm = { day, slot ->
                onAddToPlanner(meal.id, day, slot)
                showPlannerDialog = false
            },
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlannerPickerDialog(
    mealName: String,
    onDismiss: () -> Unit,
    onConfirm: (MealDay, MealSlot) -> Unit,
) {
    var selectedDay by remember { mutableStateOf(MealDay.Monday) }
    var selectedSlot by remember { mutableStateOf(MealSlot.Lunch) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Planner") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = mealName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text("Day", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealDay.entries.forEach { day ->
                        SelectableChip(
                            text = day.label,
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text("Slot", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealSlot.entries.forEach { slot ->
                        SelectableChip(
                            text = slot.label,
                            selected = selectedSlot == slot,
                            onClick = { selectedSlot = slot },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedDay, selectedSlot) },
                modifier = Modifier.testTag("confirm_planner_button"),
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
