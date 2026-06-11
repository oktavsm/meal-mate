package com.app.mealmate.ui

import com.app.mealmate.domain.model.Meal
import com.app.mealmate.domain.model.MealDay
import com.app.mealmate.domain.model.MealPlanItem

data class MealMateUiState(
    val query: String = "",
    val meals: List<Meal> = emptyList(),
    val filteredMeals: List<Meal> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val mealPlan: List<MealPlanItem> = emptyList(),
    val selectedPlannerDay: MealDay = MealDay.Monday,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val isGeneratingShoppingList: Boolean = false,
    val shoppingList: String? = null,
    val shoppingListError: String? = null,
) {
    val favoriteMeals: List<Meal>
        get() = meals.filter { it.id in favoriteIds }

    val selectedDayPlans: List<MealPlanItem>
        get() = mealPlan
            .filter { it.day == selectedPlannerDay }
            .sortedBy { it.slot.ordinal }
}
