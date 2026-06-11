package com.app.mealmate.data

import com.app.mealmate.data.local.LocalMealMateStore
import com.app.mealmate.data.local.StoredMealPlan
import com.app.mealmate.data.remote.GeminiRemoteDataSource
import com.app.mealmate.data.remote.TheMealDbRemoteDataSource
import com.app.mealmate.domain.model.Meal
import com.app.mealmate.domain.model.MealDay
import com.app.mealmate.domain.model.MealPlanItem
import com.app.mealmate.domain.model.MealSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

class MealMateRepository(
    private val seedMeals: List<Meal> = MealSeedData.meals,
    private val remoteDataSource: TheMealDbRemoteDataSource = TheMealDbRemoteDataSource(),
    private val geminiRemoteDataSource: GeminiRemoteDataSource? = null,
    private val localStore: LocalMealMateStore? = null,
) {
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: Flow<Set<String>> = localStore?.favoriteIds ?: _favorites.asStateFlow()

    private val _mealPlan = MutableStateFlow<List<MealPlanItem>>(emptyList())
    val mealPlan: Flow<List<MealPlanItem>> = localStore?.mealPlans
        ?.map { storedPlans -> storedPlans.toMealPlanItems() }
        ?: _mealPlan.asStateFlow()

    fun getMeals(): List<Meal> = seedMeals

    fun getMeal(mealId: String, knownMeals: List<Meal> = seedMeals): Meal? {
        return knownMeals.firstOrNull { it.id == mealId } ?: seedMeals.firstOrNull { it.id == mealId }
    }

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

    fun toggleFavorite(mealId: String, scope: CoroutineScope? = null, currentIds: Set<String>? = null) {
        if (localStore != null && scope != null && currentIds != null) {
            val nextIds = if (mealId in currentIds) currentIds - mealId else currentIds + mealId
            scope.launch { localStore.saveFavoriteIds(nextIds) }
            return
        }

        _favorites.update { current ->
            if (mealId in current) current - mealId else current + mealId
        }
    }

    fun removeFavorite(mealId: String, scope: CoroutineScope? = null, currentIds: Set<String>? = null) {
        if (localStore != null && scope != null && currentIds != null) {
            scope.launch { localStore.saveFavoriteIds(currentIds - mealId) }
            return
        }

        _favorites.update { it - mealId }
    }

    fun addMealPlan(
        meal: Meal,
        day: MealDay,
        slot: MealSlot,
        scope: CoroutineScope? = null,
        currentItems: List<MealPlanItem>? = null,
    ) {
        val itemId = "${day.name}-${slot.name}-${meal.id}"
        if (localStore != null && scope != null && currentItems != null) {
            if (currentItems.any { it.id == itemId }) return
            scope.launch {
                localStore.saveMealPlans(
                    items = (currentItems + MealPlanItem(id = itemId, meal = meal, day = day, slot = slot))
                        .toStoredMealPlans(),
                )
            }
            return
        }

        _mealPlan.update { current ->
            if (current.any { it.id == itemId }) current
            else current + MealPlanItem(id = itemId, meal = meal, day = day, slot = slot)
        }
    }

    fun removeMealPlan(
        itemId: String,
        scope: CoroutineScope? = null,
        currentItems: List<MealPlanItem>? = null,
    ) {
        if (localStore != null && scope != null && currentItems != null) {
            scope.launch {
                localStore.saveMealPlans(currentItems.filterNot { it.id == itemId }.toStoredMealPlans())
            }
            return
        }

        _mealPlan.update { current -> current.filterNot { it.id == itemId } }
    }

    suspend fun generateShoppingList(items: List<MealPlanItem>): Result<String> {
        if (items.isEmpty()) {
            return Result.failure(IllegalStateException("Add meals to your planner first."))
        }

        val localFallback = items.toLocalShoppingList()
        val dataSource = geminiRemoteDataSource ?: return Result.success(localFallback)

        return runCatching {
            dataSource.generateShoppingList(items.toShoppingListPrompt())
        }.recover {
            localFallback
        }
    }

    private fun mergeMeals(primary: List<Meal>, secondary: List<Meal>): List<Meal> {
        return (primary + secondary).distinctBy { it.id }
    }

    private fun List<StoredMealPlan>.toMealPlanItems(): List<MealPlanItem> {
        return mapNotNull { stored ->
            getMeal(stored.mealId)?.let { meal ->
                MealPlanItem(
                    id = stored.id,
                    meal = meal,
                    day = stored.day,
                    slot = stored.slot,
                )
            }
        }
    }

    private fun List<MealPlanItem>.toStoredMealPlans(): List<StoredMealPlan> {
        return map { item ->
            StoredMealPlan(
                id = item.id,
                mealId = item.meal.id,
                day = item.day,
                slot = item.slot,
            )
        }
    }

    private fun List<MealPlanItem>.toShoppingListPrompt(): String {
        val mealLines = joinToString(separator = "\n") { item ->
            val ingredients = item.meal.ingredients.joinToString { ingredient ->
                "${ingredient.measure} ${ingredient.name}".trim()
            }
            "- ${item.day.label} ${item.slot.label}: ${item.meal.name}. Ingredients: $ingredients"
        }

        return """
            You are a practical meal planning assistant.
            Create a concise shopping list from this weekly meal plan.

            Rules:
            - Merge duplicate or similar ingredients.
            - Group items by category.
            - Keep quantities simple and readable.
            - Return plain text only, no markdown table.
            - Use short section headers and bullet points.

            Meal plan:
            $mealLines
        """.trimIndent()
    }

    private fun List<MealPlanItem>.toLocalShoppingList(): String {
        val groupedIngredients = flatMap { item ->
            item.meal.ingredients.map { ingredient ->
                LocalShoppingIngredient(
                    name = ingredient.name.trim(),
                    measure = ingredient.measure.trim(),
                    category = ingredient.name.categoryLabel(),
                )
            }
        }
            .filter { it.name.isNotBlank() }
            .groupBy { it.category }
            .toSortedMap(compareBy { categoryOrder(it) })

        val sections = groupedIngredients.map { (category, ingredients) ->
            val lines = ingredients
                .groupBy { it.name.lowercase() }
                .values
                .map { duplicates ->
                    val first = duplicates.first()
                    val measures = duplicates
                        .map { it.measure }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .joinToString(" + ")
                    if (measures.isBlank()) "- ${first.name}" else "- ${first.name}: $measures"
                }
                .sorted()
                .joinToString("\n")
            "$category\n$lines"
        }.joinToString("\n\n")

        return """
            Generated locally because AI is unavailable.

            $sections
        """.trimIndent()
    }

    private fun String.categoryLabel(): String {
        val value = lowercase()
        return when {
            value in proteinIngredients -> "Protein"
            value in dairyIngredients -> "Dairy"
            value in produceIngredients -> "Produce"
            value in bakeryIngredients -> "Bakery"
            value in spiceIngredients -> "Spices"
            else -> "Pantry"
        }
    }

    private fun categoryOrder(category: String): Int {
        return when (category) {
            "Protein" -> 0
            "Produce" -> 1
            "Dairy" -> 2
            "Bakery" -> 3
            "Pantry" -> 4
            "Spices" -> 5
            else -> 6
        }
    }

    private data class LocalShoppingIngredient(
        val name: String,
        val measure: String,
        val category: String,
    )

    private companion object {
        val proteinIngredients = setOf(
            "beef fillet",
            "chicken",
            "chicken breast",
            "egg",
            "eggs",
            "egg yolk",
            "egg yolks",
            "minced beef",
            "prawns",
            "salmon",
            "smoked haddock",
            "white fish",
        )
        val dairyIngredients = setOf(
            "bechamel sauce",
            "cheddar",
            "cheese",
            "mascarpone",
            "milk",
            "parmesan",
            "yogurt",
        )
        val produceIngredients = setOf(
            "aubergine",
            "beansprouts",
            "carrot",
            "cucumber",
            "garlic",
            "onion",
            "potatoes",
            "romaine lettuce",
            "tomatoes",
        )
        val bakeryIngredients = setOf(
            "croutons",
            "ladyfingers",
            "puff pastry",
        )
        val spiceIngredients = setOf(
            "cinnamon",
            "curry powder",
            "garlic paste",
            "ginger paste",
            "red chilli flakes",
        )
    }
}

data class MealLoadResult(
    val meals: List<Meal>,
    val usedFallback: Boolean = false,
    val message: String? = null,
)
