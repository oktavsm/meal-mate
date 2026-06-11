package com.app.mealmate

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class MealMateUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeShowsRecipeList() {
        composeTestRule.onNodeWithText("What do you want to cook today?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chicken Handi").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recipe_grid").assertIsDisplayed()
    }

    @Test
    fun searchFiltersRecipesAndCanReturnToAllRecipes() {
        composeTestRule.onNodeWithTag("search_field").performTextInput("Chicken")
        composeTestRule.onNodeWithText("Chicken Handi").assertIsDisplayed()

        composeTestRule.onNodeWithTag("search_field").performTextClearance()
        composeTestRule.onNodeWithText("Beef Wellington").assertIsDisplayed()
    }

    @Test
    fun searchShowsEmptyStateWhenRecipeIsNotFound() {
        composeTestRule.onNodeWithTag("search_field").performTextInput("zzzzzz")
        composeTestRule.onNodeWithTag("empty_search").assertIsDisplayed()
        composeTestRule.onNodeWithText("No recipes found").assertIsDisplayed()
    }

    @Test
    fun recipeCardOpensDetail() {
        composeTestRule.onNodeWithText("Chicken Handi").performClick()

        composeTestRule.onNodeWithTag("detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ingredients").assertIsDisplayed()
        composeTestRule.onNodeWithText("Instructions").assertIsDisplayed()
    }

    @Test
    fun aboutPageCanBeOpenedFromToolbar() {
        composeTestRule.onNodeWithContentDescription("about_page").performClick()

        composeTestRule.onNodeWithTag("about_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oktavianus Samuel Minarto").assertIsDisplayed()
        composeTestRule.onNodeWithText("oktaavsm@student.ub.ac.id").assertIsDisplayed()
    }

    @Test
    fun canAddAndRemoveFavorite() {
        composeTestRule.onNodeWithText("Chicken Handi").performClick()
        composeTestRule.onNodeWithTag("favorite_detail_button").performClick()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        composeTestRule.onNodeWithText("Favorites").performClick()
        composeTestRule.onNodeWithText("Chicken Handi").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Remove favorite Chicken Handi").performClick()
        composeTestRule.onNodeWithTag("empty_favorites").assertIsDisplayed()
    }

    @Test
    fun canAddAndRemoveMealPlan() {
        composeTestRule.onNodeWithText("Chicken Handi").performClick()
        composeTestRule.onNodeWithText("Add to Planner").performClick()
        composeTestRule.onNodeWithTag("confirm_planner_button").performClick()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        composeTestRule.onNodeWithText("Planner").performClick()
        composeTestRule.onNodeWithText("Chicken Handi").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Remove meal plan Chicken Handi").performClick()
        composeTestRule.onNodeWithTag("empty_planner").assertIsDisplayed()
    }
}
