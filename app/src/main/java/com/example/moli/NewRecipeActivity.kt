package com.example.moli

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class NewRecipeActivity : AppCompatActivity() {

    private lateinit var containerSteps: LinearLayout
    private val stepsList = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)

        containerSteps = findViewById(R.id.containerSteps)
        val btnAddStep = findViewById<MaterialButton>(R.id.btnAddStep)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveRecipe)

        // Agregar un primer paso por defecto
        addNewStep()

        btnAddStep.setOnClickListener {
            addNewStep()
        }

        findViewById<ImageButton>(R.id.btnBackFromNewRecipe).setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            saveRecipe()
        }
    }

    private fun addNewStep() {
        val stepView = LayoutInflater.from(this).inflate(R.layout.item_step_input, containerSteps, false)
        
        val tvStepNumber = stepView.findViewById<TextView>(R.id.tvStepInputNumber)
        val btnRemove = stepView.findViewById<ImageButton>(R.id.btnRemoveStep)
        
        stepsList.add(stepView)
        updateStepNumbers()
        
        btnRemove.setOnClickListener {
            if (stepsList.size > 1) {
                containerSteps.removeView(stepView)
                stepsList.remove(stepView)
                updateStepNumbers()
            }
        }
        
        containerSteps.addView(stepView)
    }

    private fun updateStepNumbers() {
        for (i in stepsList.indices) {
            val tvNumber = stepsList[i].findViewById<TextView>(R.id.tvStepInputNumber)
            tvNumber.text = (i + 1).toString()
        }
    }

    private fun saveRecipe() {
        val name = findViewById<EditText>(R.id.etRecipeName).text.toString()
        val ingredientsRaw = findViewById<EditText>(R.id.etIngredientsRaw).text.toString()
        
        val steps = mutableListOf<Step>()
        for (view in stepsList) {
            val desc = view.findViewById<EditText>(R.id.etStepDescription).text.toString()
            val timerStr = view.findViewById<EditText>(R.id.etStepTimer).text.toString()
            val timer = if (timerStr.isNotEmpty()) timerStr.toInt() else null
            
            if (desc.isNotEmpty()) {
                steps.add(Step(desc, timer))
            }
        }

        // Aquí se guardaría la receta (en una DB o enviándola de regreso)
        // Por ahora, solo cerramos la actividad
        finish()
    }
}
