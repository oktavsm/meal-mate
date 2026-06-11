package com.app.mealmate.data.remote

import com.app.mealmate.domain.model.Ingredient
import com.app.mealmate.domain.model.Meal
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TheMealDbRemoteDataSource {
    suspend fun fetchDefaultMeals(): List<Meal> {
        return fetchMeals("search.php?f=a")
    }

    suspend fun searchMeals(query: String): List<Meal> {
        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        return fetchMeals("search.php?s=$encodedQuery")
    }

    private suspend fun fetchMeals(path: String): List<Meal> = withContext(Dispatchers.IO) {
        val connection = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        connection.connectTimeout = 8_000
        connection.readTimeout = 8_000
        connection.requestMethod = "GET"

        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("TheMealDB responded with ${connection.responseCode}")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(response)
                .optJSONArray("meals")
                ?.let { meals ->
                    buildList {
                        for (index in 0 until meals.length()) {
                            val meal = meals.optJSONObject(index)?.toMeal()
                            if (meal != null) add(meal)
                        }
                    }
                }
                .orEmpty()
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toMeal(): Meal? {
        val id = optCleanString("idMeal") ?: return null
        val name = optCleanString("strMeal") ?: return null
        val thumbnail = optCleanString("strMealThumb") ?: return null

        return Meal(
            id = id,
            name = name,
            thumbnailUrl = thumbnail,
            category = optCleanString("strCategory") ?: "Recipe",
            area = optCleanString("strArea") ?: "Global",
            tags = optCleanString("strTags")
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                .orEmpty(),
            ingredients = ingredients(),
            instructions = optCleanString("strInstructions") ?: "No instructions provided yet.",
            sourceUrl = optCleanString("strSource"),
            youtubeUrl = optCleanString("strYoutube"),
        )
    }

    private fun JSONObject.ingredients(): List<Ingredient> {
        return (1..20).mapNotNull { index ->
            val name = optCleanString("strIngredient$index") ?: return@mapNotNull null
            Ingredient(
                name = name,
                measure = optCleanString("strMeasure$index").orEmpty(),
            )
        }
    }

    private fun JSONObject.optCleanString(name: String): String? {
        return optString(name)
            .takeUnless { it.equals("null", ignoreCase = true) }
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private companion object {
        const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
    }
}
