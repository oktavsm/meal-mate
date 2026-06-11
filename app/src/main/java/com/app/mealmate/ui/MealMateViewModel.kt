package com.app.mealmate.ui

import androidx.lifecycle.ViewModel
import com.app.mealmate.data.MealMateRepository
import com.app.mealmate.domain.model.Meal
import com.app.mealmate.domain.model.MealDay
import com.app.mealmate.domain.model.MealSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope

class MealMateViewModel(
    private val repository: MealMateRepository = MealMateRepository(),
) : ViewModel() {
    private val meals = MutableStateFlow(repository.getMeals())
    private val query = MutableStateFlow("")
    private val selectedPlannerDay = MutableStateFlow(MealDay.Monday)

    val uiState: StateFlow<MealMateUiState> = combine(
        meals,
        query,
        repository.favorites,
        repository.mealPlan,
        selectedPlannerDay,
    ) { meals, query, favorites, mealPlan, selectedDay ->
        val filtered = if (query.isBlank()) {
            meals
        } else {
            meals.filter { it.name.contains(query.trim(), ignoreCase = true) }
        }

        MealMateUiState(
            query = query,
            meals = meals,
            filteredMeals = filtered,
            favoriteIds = favorites,
            mealPlan = mealPlan,
            selectedPlannerDay = selectedDay,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MealMateUiState(
            meals = repository.getMeals(),
            filteredMeals = repository.getMeals(),
        ),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun getMeal(mealId: String): Meal? = repository.getMeal(mealId)

    fun toggleFavorite(mealId: String) {
        repository.toggleFavorite(mealId)
    }

    fun removeFavorite(mealId: String) {
        repository.removeFavorite(mealId)
    }

    fun addMealPlan(mealId: String, day: MealDay, slot: MealSlot) {
        repository.getMeal(mealId)?.let { meal ->
            repository.addMealPlan(meal = meal, day = day, slot = slot)
        }
    }

    fun removeMealPlan(itemId: String) {
        repository.removeMealPlan(itemId)
    }

    fun selectPlannerDay(day: MealDay) {
        selectedPlannerDay.update { day }
    }
}
