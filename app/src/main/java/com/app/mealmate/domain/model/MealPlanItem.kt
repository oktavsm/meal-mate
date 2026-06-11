package com.app.mealmate.domain.model

data class MealPlanItem(
    val id: String,
    val meal: Meal,
    val day: MealDay,
    val slot: MealSlot,
)
