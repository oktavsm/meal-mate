package com.app.mealmate.ui.navigation

object MealMateDestination {
    const val Explore = "explore"
    const val Planner = "planner"
    const val Favorites = "favorites"
    const val About = "about"
    const val Detail = "detail/{mealId}"
    const val MealIdArg = "mealId"

    fun detailRoute(mealId: String): String = "detail/$mealId"
}
