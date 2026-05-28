package com.example.moli

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAllRecipesFlow(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :search || '%'")
    suspend fun searchRecipes(search: String): List<RecipeEntity>

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM recipes WHERE isDiscovery = 0")
    suspend fun countUserCreated(): Int

    @Query("SELECT COALESCE(SUM(totalTimeMinutes), 0) FROM recipes")
    suspend fun totalCookingMinutes(): Int
}
