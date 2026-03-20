package com.example.moli

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class CookingModeActivity : AppCompatActivity() {

    private var currentStep = 0
    private val steps = listOf(
        "Lava y desinfecta todos los vegetales antes de comenzar.",
        "Corta la cebolla y el ajo finamente para el sofrito.",
        "Calienta el aceite en una sartén grande a fuego medio.",
        "Añade la carne y cocina hasta que esté dorada.",
        "Sirve caliente y disfruta de tu creación Moli."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cooking_mode)

        val tvStepDescription = findViewById<TextView>(R.id.tvStepDescription)
        val tvStepNumber = findViewById<TextView>(R.id.tvStepNumber)
        val progressIndicator = findViewById<LinearProgressIndicator>(R.id.cookingProgress)
        val btnNext = findViewById<MaterialButton>(R.id.btnNextStep)
        val btnPrev = findViewById<MaterialButton>(R.id.btnPrevStep)

        fun updateUI() {
            tvStepDescription.text = steps[currentStep]
            tvStepNumber.text = "PASO ${currentStep + 1} DE ${steps.size}"
            val progress = ((currentStep + 1).toFloat() / steps.size.toFloat() * 100).toInt()
            progressIndicator.setProgress(progress, true)
            
            btnPrev.isEnabled = currentStep > 0
            btnNext.text = if (currentStep == steps.size - 1) "FINALIZAR" else "SIGUIENTE"
        }

        btnNext.setOnClickListener {
            if (currentStep < steps.size - 1) {
                currentStep++
                updateUI()
            } else {
                finish() // Terminar modo cocina
            }
        }

        btnPrev.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                updateUI()
            }
        }

        findViewById<ImageButton>(R.id.btnBackFromCooking).setOnClickListener {
            finish()
        }

        updateUI()
    }
}