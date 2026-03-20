package com.example.moli

import java.io.Serializable

data class Recipe(
    val title: String,
    val ingredients: String,
    val steps: String,
    val imageResId: Int // Para usar recursos locales por ahora
) : Serializable