package com.app.mealmate.data

import com.app.mealmate.data.remote.TheMealDbRemoteDataSource
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
    private val remoteDataSource: TheMealDbRemoteDataSource = TheMealDbRemoteDataSource(),
) {
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _mealPlan = MutableStateFlow<List<MealPlanItem>>(emptyList())
    val mealPlan: StateFlow<List<MealPlanItem>> = _mealPlan.asStateFlow()

    fun getMeals(): List<Meal> = seedMeals

    fun getMeal(mealId: String): Meal? = seedMeals.firstOrNull { it.id == mealId }

    suspend fun loadDefaultMeals(): MealLoadResult {
        return runCatching { remoteDataSource.fetchDefaultMeals() }
            .fold(
                onSuccess = { remoteMeals ->
                    if (remoteMeals.isEmpty()) {
                        MealLoadResult(
                            meals = seedMeals,
                            usedFallback = true,
                            message = "Showing offline recipes because TheMealDB returned no meals.",
                        )
                    } else {
                        MealLoadResult(meals = mergeMeals(seedMeals, remoteMeals))
                    }
                },
                onFailure = {
                    MealLoadResult(
                        meals = seedMeals,
                        usedFallback = true,
                        message = "Showing offline recipes. Check your connection for live results.",
                    )
                },
            )
    }

    suspend fun searchMeals(query: String, localMeals: List<Meal>): MealLoadResult {
        val localResults = localMeals.filter {
            it.name.contains(query.trim(), ignoreCase = true)
        }

        return runCatching { remoteDataSource.searchMeals(query) }
            .fold(
                onSuccess = { remoteMeals ->
                    MealLoadResult(meals = mergeMeals(localResults, remoteMeals))
                },
                onFailure = {
                    MealLoadResult(
                        meals = localResults,
                        usedFallback = true,
                        message = "Search is using offline recipes for now.",
                    )
                },
            )
    }

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

    private fun mergeMeals(primary: List<Meal>, secondary: List<Meal>): List<Meal> {
        return (primary + secondary).distinctBy { it.id }
    }
}

data class MealLoadResult(
    val meals: List<Meal>,
    val usedFallback: Boolean = false,
    val message: String? = null,
)
