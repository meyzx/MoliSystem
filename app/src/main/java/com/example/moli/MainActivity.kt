package com.example.moli

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var rvRecipes: RecyclerView
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicializar RecyclerView
        rvRecipes = findViewById(R.id.rvRecipes)
        rvRecipes.layoutManager = LinearLayoutManager(this)

        // Lista de ejemplo de recetas
        val recipeList = listOf(
            Recipe("Enchiladas Verdes", "Pollo, Tortillas, Salsa verde", "1. Cocer pollo...", android.R.drawable.ic_menu_gallery),
            Recipe("Chilaquiles", "Tortillas, Salsa roja, Queso", "1. Freír tortillas...", android.R.drawable.ic_menu_gallery),
            Recipe("Tacos al Pastor", "Cerdo, Piña, Tortillas", "1. Marinar carne...", android.R.drawable.ic_menu_gallery)
        )

        adapter = RecipeAdapter(recipeList) { recipe ->
            navigateToDetail(recipe)
        }
        rvRecipes.adapter = adapter

        // Navegación de Usuario (Perfil) - Cambiado de ImageButton a ImageView
        findViewById<ImageView>(R.id.btnUserProfile).setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        // Navegación Inferior (Bottom Navigation)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_favorites -> {
                    // Aquí podrías abrir una pantalla de favoritos
                    true
                }
                R.id.nav_timer -> {
                    val intent = Intent(this, TimerActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Botón para agregar nueva receta
        findViewById<FloatingActionButton>(R.id.fabAddNewRecipe).setOnClickListener {
            val intent = Intent(this, NewRecipeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToDetail(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra("RECIPE_DATA", recipe)
        startActivity(intent)
    }
}