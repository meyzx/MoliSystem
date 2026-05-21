package com.example.moli

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val ingredientsJson: String, // Guardaremos la lista como JSON
    val stepsJson: String,       // Guardaremos la lista como JSON
    val imageResId: Int,
    val basePortions: Int,
    val totalTimeMinutes: Int
)

class Converters {
    @TypeConverter
    fun fromIngredientList(value: List<Ingredient>): String = Gson().toJson(value)

    @TypeConverter
    fun toIngredientList(value: String): List<Ingredient> {
        val listType = object : TypeToken<List<Ingredient>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromStepList(value: List<Step>): String = Gson().toJson(value)

    @TypeConverter
    fun toStepList(value: String): List<Step> {
        val listType = object : TypeToken<List<Step>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
