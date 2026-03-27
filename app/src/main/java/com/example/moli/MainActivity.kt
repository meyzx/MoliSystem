package com.example.moli

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
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
    private lateinit var etSearch: EditText
    
    private val recipeList = listOf(
        Recipe(
            "Enchiladas Verdes",
            listOf(
                Ingredient(1.0, "pza", "Pollo"),
                Ingredient(10.0, "pza", "Tortillas"),
                Ingredient(1.0, "taza", "Salsa verde")
            ),
            listOf(
                Step("Cocer el pollo con sal y cebolla.", 20),
                Step("Deshebrar el pollo."),
                Step("Pasar las tortillas por aceite."),
                Step("Bañar en salsa verde y servir.")
            ),
            android.R.drawable.ic_menu_gallery
        ),
        Recipe(
            "Chilaquiles",
            listOf(
                Ingredient(20.0, "pza", "Tortillas"),
                Ingredient(2.0, "tazas", "Salsa roja"),
                Ingredient(100.0, "g", "Queso")
            ),
            listOf(
                Step("Cortar las tortillas en triángulos y freír.", 10),
                Step("Calentar la salsa roja."),
                Step("Mezclar los totopos con la salsa."),
                Step("Servir con queso y crema.")
            ),
            android.R.drawable.ic_menu_gallery
        ),
        Recipe(
            "Tacos al Pastor",
            listOf(
                Ingredient(500.0, "g", "Cerdo"),
                Ingredient(1.0, "pza", "Piña"),
                Ingredient(12.0, "pza", "Tortillas")
            ),
            listOf(
                Step("Marinar la carne de cerdo.", 60),
                Step("Asar la carne con trozos de piña."),
                Step("Picar la carne."),
                Step("Servir en tortillas con cilantro y cebolla.")
            ),
            android.R.drawable.ic_menu_gallery
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        rvRecipes = findViewById(R.id.rvRecipes)
        rvRecipes.layoutManager = LinearLayoutManager(this)

        adapter = RecipeAdapter(recipeList) { recipe ->
            navigateToDetail(recipe)
        }
        rvRecipes.adapter = adapter

        etSearch = findViewById(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecipes(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Navegación Inferior
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_recipes -> {
                    // Acción para ver todas las recetas
                    true
                }
                R.id.nav_timer -> {
                    val intent = Intent(this, TimerActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, UserProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // FAB centrado
        findViewById<FloatingActionButton>(R.id.fabAddNewRecipe).setOnClickListener {
            val intent = Intent(this, NewRecipeActivity::class.java)
            startActivity(intent)
        }

        // Botón de Notificaciones
        findViewById<ImageView>(R.id.btnNotifications).setOnClickListener {
            // Acción de notificaciones
        }
    }

    private fun filterRecipes(query: String) {
        val filteredList = if (query.isEmpty()) {
            recipeList
        } else {
            recipeList.filter { 
                it.title.contains(query, ignoreCase = true) ||
                it.ingredients.any { ingredient -> ingredient.name.contains(query, ignoreCase = true) }
            }
        }
        adapter.updateList(filteredList)
    }

    private fun navigateToDetail(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra("RECIPE_DATA", recipe)
        startActivity(intent)
    }
}
