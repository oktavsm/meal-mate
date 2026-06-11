package com.app.mealmate.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.mealmate.data.local.LocalMealMateStore
import com.app.mealmate.data.MealMateRepository
import com.app.mealmate.domain.model.Meal
import com.app.mealmate.domain.model.MealDay
import com.app.mealmate.domain.model.MealSlot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MealMateViewModel(
    private val repository: MealMateRepository = MealMateRepository(),
) : ViewModel() {
    private val knownMeals = MutableStateFlow(repository.getMeals())
    private val baseMeals = MutableStateFlow(repository.getMeals())
    private val searchResults = MutableStateFlow<List<Meal>?>(null)
    private val query = MutableStateFlow("")
    private val selectedPlannerDay = MutableStateFlow(MealDay.Monday)
    private val isLoading = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(null)
    private var searchJob: Job? = null

    val uiState: StateFlow<MealMateUiState> = combine(
        combine(knownMeals, baseMeals, searchResults, query) { knownMeals, baseMeals, searchResults, query ->
            val displayedMeals = if (query.isBlank()) {
                baseMeals
            } else {
                searchResults ?: baseMeals.filter { it.name.contains(query.trim(), ignoreCase = true) }
            }
            MealLists(
                knownMeals = knownMeals,
                displayedMeals = displayedMeals,
            )
        },
        repository.favorites,
        repository.mealPlan,
        selectedPlannerDay,
        combine(isLoading, statusMessage) { isLoading, statusMessage ->
            NetworkState(
                isLoading = isLoading,
                statusMessage = statusMessage,
            )
        },
    ) { mealLists, favorites, mealPlan, selectedDay, networkState ->
        MealMateUiState(
            query = query.value,
            meals = mealLists.knownMeals,
            filteredMeals = mealLists.displayedMeals,
            favoriteIds = favorites,
            mealPlan = mealPlan,
            selectedPlannerDay = selectedDay,
            isLoading = networkState.isLoading,
            statusMessage = networkState.statusMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MealMateUiState(
            meals = repository.getMeals(),
            filteredMeals = repository.getMeals(),
        ),
    )

    init {
        refreshDefaultMeals()
    }

    fun onQueryChange(value: String) {
        query.value = value
        searchJob?.cancel()

        if (value.isBlank()) {
            searchResults.value = null
            statusMessage.value = null
            isLoading.value = false
            return
        }

        val trimmedQuery = value.trim()
        searchResults.value = baseMeals.value.filter {
            it.name.contains(trimmedQuery, ignoreCase = true)
        }

        searchJob = viewModelScope.launch {
            delay(350)
            isLoading.value = true
            val result = repository.searchMeals(
                query = trimmedQuery,
                localMeals = knownMeals.value,
            )
            if (query.value.trim() == trimmedQuery) {
                searchResults.value = result.meals
                rememberMeals(result.meals)
                statusMessage.value = result.message
            }
            isLoading.value = false
        }
    }

    fun refreshDefaultMeals() {
        searchJob?.cancel()
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.loadDefaultMeals()
            baseMeals.value = result.meals
            searchResults.value = null
            rememberMeals(result.meals)
            statusMessage.value = result.message
            isLoading.value = false
        }
    }

    fun getMeal(mealId: String): Meal? = knownMeals.value.firstOrNull { it.id == mealId }

    fun toggleFavorite(mealId: String) {
        repository.toggleFavorite(
            mealId = mealId,
            scope = viewModelScope,
            currentIds = uiState.value.favoriteIds,
        )
    }

    fun removeFavorite(mealId: String) {
        repository.removeFavorite(
            mealId = mealId,
            scope = viewModelScope,
            currentIds = uiState.value.favoriteIds,
        )
    }

    fun addMealPlan(mealId: String, day: MealDay, slot: MealSlot) {
        getMeal(mealId)?.let { meal ->
            repository.addMealPlan(
                meal = meal,
                day = day,
                slot = slot,
                scope = viewModelScope,
                currentItems = uiState.value.mealPlan,
            )
        }
    }

    fun removeMealPlan(itemId: String) {
        repository.removeMealPlan(
            itemId = itemId,
            scope = viewModelScope,
            currentItems = uiState.value.mealPlan,
        )
    }

    fun selectPlannerDay(day: MealDay) {
        selectedPlannerDay.update { day }
    }

    private fun rememberMeals(meals: List<Meal>) {
        knownMeals.update { current -> (current + meals).distinctBy { it.id } }
    }

    private data class MealLists(
        val knownMeals: List<Meal>,
        val displayedMeals: List<Meal>,
    )

    private data class NetworkState(
        val isLoading: Boolean,
        val statusMessage: String?,
    )

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = MealMateRepository(
                        localStore = LocalMealMateStore(appContext),
                    )
                    return MealMateViewModel(repository) as T
                }
            }
        }
    }
}
