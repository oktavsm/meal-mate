package com.app.mealmate.domain.model

data class Meal(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
    val category: String,
    val area: String,
    val tags: List<String>,
    val ingredients: List<Ingredient>,
    val instructions: String,
    val sourceUrl: String? = null,
    val youtubeUrl: String? = null,
)
