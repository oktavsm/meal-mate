package com.app.mealmate.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MealMateGreen,
    secondary = MealMateSoftGreen,
    tertiary = MealMateWarning,
    background = MealMateDarkBackground,
    surface = MealMateDarkSurface,
    onPrimary = MealMateDarkBackground,
    onSecondary = MealMateDarkBackground,
    onTertiary = MealMateDarkBackground,
    onBackground = MealMateDarkText,
    onSurface = MealMateDarkText,
)

private val LightColorScheme = lightColorScheme(
    primary = MealMateGreen,
    secondary = MealMateSoftGreen,
    tertiary = MealMateWarning,
    background = MealMateBackground,
    surface = MealMateSurface,
    onPrimary = Color.White,
    onSecondary = MealMateGreenDark,
    onTertiary = Color.White,
    onBackground = MealMateText,
    onSurface = MealMateText,
    outline = MealMateBorder,
)

@Composable
fun MealMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
