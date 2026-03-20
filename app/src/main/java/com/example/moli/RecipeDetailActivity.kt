package com.example.moli

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecipeDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Configurar botón de regreso
        findViewById<View>(R.id.btnBackToMain).setOnClickListener {
            finish()
        }

        // Configurar botón de Iniciar Modo Cocina
        findViewById<MaterialButton>(R.id.btnStartCooking).setOnClickListener {
            val intent = Intent(this, CookingModeActivity::class.java)
            startActivity(intent)
        }

        // Configurar botón del temporizador
        findViewById<FloatingActionButton>(R.id.fabTimer).setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }
    }
}