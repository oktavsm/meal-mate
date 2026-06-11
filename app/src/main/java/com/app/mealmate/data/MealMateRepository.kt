package com.app.mealmate.data

import com.app.mealmate.domain.model.Meal
import com.app.mealmate.domain.model.MealDay
import com.app.mealmate.domain.model.MealPlanItem
import com.app.mealmate.domain.model.MealSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MealMateRepository(
    private val seedMeals: List<Meal> = MealSeedData.meals,
) {
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _mealPlan = MutableStateFlow<List<MealPlanItem>>(emptyList())
    val mealPlan: StateFlow<List<MealPlanItem>> = _mealPlan.asStateFlow()

    fun getMeals(): List<Meal> = seedMeals

    fun getMeal(mealId: String): Meal? = seedMeals.firstOrNull { it.id == mealId }

    fun toggleFavorite(mealId: String) {
        _favorites.update { current ->
            if (mealId in current) current - mealId else current + mealId
        }
    }

    fun removeFavorite(mealId: String) {
        _favorites.update { it - mealId }
    }

    fun addMealPlan(meal: Meal, day: MealDay, slot: MealSlot) {
        val itemId = "${day.name}-${slot.name}-${meal.id}"
        _mealPlan.update { current ->
            if (current.any { it.id == itemId }) current
            else current + MealPlanItem(id = itemId, meal = meal, day = day, slot = slot)
        }
    }

    fun removeMealPlan(itemId: String) {
        _mealPlan.update { current -> current.filterNot { it.id == itemId } }
    }
}
