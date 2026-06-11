package com.app.mealmate.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.mealmate.domain.model.MealDay
import com.app.mealmate.domain.model.MealSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mealMateDataStore by preferencesDataStore(name = "meal_mate_store")

class LocalMealMateStore(context: Context) {
    private val dataStore = context.applicationContext.mealMateDataStore

    val favoriteIds: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[FavoriteIdsKey].orEmpty()
    }

    val mealPlans: Flow<List<StoredMealPlan>> = dataStore.data.map { preferences ->
        preferences[MealPlansKey]
            .orEmpty()
            .mapNotNull { it.toStoredMealPlanOrNull() }
            .sortedWith(compareBy<StoredMealPlan> { it.day.ordinal }.thenBy { it.slot.ordinal })
    }

    suspend fun saveFavoriteIds(ids: Set<String>) {
        dataStore.edit { preferences ->
            preferences[FavoriteIdsKey] = ids
        }
    }

    suspend fun saveMealPlans(items: List<StoredMealPlan>) {
        dataStore.edit { preferences ->
            preferences[MealPlansKey] = items.map { it.encode() }.toSet()
        }
    }

    private fun String.toStoredMealPlanOrNull(): StoredMealPlan? {
        val parts = split(Separator)
        if (parts.size != 4) return null

        return runCatching {
            StoredMealPlan(
                id = parts[0],
                mealId = parts[1],
                day = MealDay.valueOf(parts[2]),
                slot = MealSlot.valueOf(parts[3]),
            )
        }.getOrNull()
    }

    private fun StoredMealPlan.encode(): String {
        return listOf(id, mealId, day.name, slot.name).joinToString(Separator)
    }

    private companion object {
        const val Separator = "|"
        val FavoriteIdsKey = stringSetPreferencesKey("favorite_ids")
        val MealPlansKey = stringSetPreferencesKey("meal_plans")
    }
}

data class StoredMealPlan(
    val id: String,
    val mealId: String,
    val day: MealDay,
    val slot: MealSlot,
)
