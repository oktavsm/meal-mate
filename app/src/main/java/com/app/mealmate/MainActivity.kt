package com.app.mealmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.app.mealmate.ui.MealMateViewModel
import com.app.mealmate.ui.app.MealMateApp
import com.app.mealmate.ui.theme.MealMateTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MealMateViewModel by viewModels {
        MealMateViewModel.factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MealMateTheme {
                MealMateApp(viewModel = viewModel)
            }
        }
    }
}
