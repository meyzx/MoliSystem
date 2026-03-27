package com.example.moli

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class RecipeDetailActivity : AppCompatActivity() {

    private var currentPortions = 1
    private var basePortions = 1
    private var ingredientsList = listOf<Ingredient>()

    private lateinit var containerIngredients: LinearLayout
    private lateinit var tvPortionCount: TextView
    private lateinit var tvSteps: TextView
    private lateinit var tvTotalTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val recipe = intent.getSerializableExtra("RECIPE_DATA") as? Recipe

        containerIngredients = findViewById(R.id.containerIngredients)
        tvPortionCount = findViewById(R.id.tvPortionCount)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        val tvTitle = findViewById<TextView>(R.id.tvRecipeTitle)
        val ivRecipe = findViewById<ImageView>(R.id.ivRecipeDetail)
        tvSteps = findViewById(R.id.tvSteps)

        recipe?.let {
            tvTitle.text = it.title
            ivRecipe.setImageResource(it.imageResId)
            tvTotalTime.text = "${it.totalTimeMinutes} min"
            
            // Mostrar pasos
            val stepsText = it.steps.joinToString("\n\n") { step ->
                if (step.timerDurationMinutes != null) {
                    "${step.description} (⏲️ ${step.timerDurationMinutes} min)"
                } else {
                    step.description
                }
            }
            tvSteps.text = stepsText
            
            ingredientsList = it.ingredients
            basePortions = it.basePortions
            currentPortions = it.basePortions
            updateIngredientsUI()
        }

        findViewById<View>(R.id.btnBackToMain).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnIncreasePortions).setOnClickListener {
            currentPortions++
            updateIngredientsUI()
        }

        findViewById<ImageButton>(R.id.btnDecreasePortions).setOnClickListener {
            if (currentPortions > 1) {
                currentPortions--
                updateIngredientsUI()
            }
        }

        findViewById<MaterialButton>(R.id.btnStartCooking).setOnClickListener {
            val intent = Intent(this, CookingModeActivity::class.java)
            intent.putExtra("RECIPE_DATA", recipe)
            startActivity(intent)
        }

        findViewById<FloatingActionButton>(R.id.fabTimer).setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateIngredientsUI() {
        tvPortionCount.text = currentPortions.toString()
        containerIngredients.removeAllViews()
        
        for (ingredient in ingredientsList) {
            val scaledQuantity = (ingredient.quantity * currentPortions) / basePortions
            val formattedQuantity = if (scaledQuantity % 1.0 == 0.0) {
                scaledQuantity.toInt().toString()
            } else {
                String.format(Locale.getDefault(), "%.1f", scaledQuantity)
            }
            
            val checkBox = CheckBox(this).apply {
                text = "$formattedQuantity ${ingredient.unit} de ${ingredient.name}"
                textSize = 16f
                setTextColor(getColor(R.color.black))
                buttonTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.moli_verde))
                setPadding(16, 16, 16, 16)
            }
            containerIngredients.addView(checkBox)
        }
    }
}
