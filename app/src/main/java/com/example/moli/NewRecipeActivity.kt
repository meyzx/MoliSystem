package com.example.moli

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class NewRecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)

        findViewById<ImageButton>(R.id.btnBackFromNewRecipe).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnSaveRecipe).setOnClickListener {
            // Lógica para guardar la receta
            finish()
        }
    }
}