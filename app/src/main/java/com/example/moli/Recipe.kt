package com.example.moli

import java.io.Serializable

data class Ingredient(
    val quantity: Double,
    val unit: String,
    val name: String
) : Serializable

data class Step(
    val description: String,
    val timerDurationMinutes: Int? = null
) : Serializable

data class Recipe(
    val title: String,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
    val imageResId: Int,
    val basePortions: Int = 1,
    val totalTimeMinutes: Int = 30
) : Serializable