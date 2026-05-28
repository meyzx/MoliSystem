package com.example.moli

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import kotlinx.coroutines.launch

class NewRecipeActivity : AppCompatActivity() {

    private lateinit var containerSteps: LinearLayout
    private val stepsList = mutableListOf<View>()
    private lateinit var db: AppDatabase
    private var selectedImageUri: Uri? = null
    private lateinit var ivRecipePreview: ImageView
    private lateinit var layoutPlaceholder: View

    // Registro del Photo Picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivRecipePreview.setImageURI(uri)
            ivRecipePreview.alpha = 1.0f
            layoutPlaceholder.visibility = View.GONE
            
            // Otorgar permisos persistentes para la URI (necesario para Room)
            contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)

        db = AppDatabase.getDatabase(this)
        containerSteps = findViewById(R.id.containerSteps)
        ivRecipePreview = findViewById(R.id.ivRecipePreview)
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder)
        
        val btnAddStep = findViewById<MaterialButton>(R.id.btnAddStep)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveRecipe)
        val cardSelectImage = findViewById<MaterialCardView>(R.id.cardSelectImage)

        addNewStep()

        cardSelectImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

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
        
        if (name.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val steps = mutableListOf<Step>()
        for (view in stepsList) {
            val desc = view.findViewById<EditText>(R.id.etStepDescription).text.toString()
            val timerStr = view.findViewById<EditText>(R.id.etStepTimer).text.toString()
            val timer = if (timerStr.isNotEmpty()) timerStr.toInt() else null
            if (desc.isNotEmpty()) steps.add(Step(desc, timer))
        }

        val ingredients = mutableListOf<Ingredient>()
        ingredientsRaw.split("\n").forEach { line ->
            if (line.isNotBlank()) {
                val parts = line.trim().split(" ", limit = 3)
                if (parts.size >= 3) {
                    val qty = parts[0].toDoubleOrNull() ?: 1.0
                    ingredients.add(Ingredient(qty, parts[1], parts[2]))
                } else {
                    ingredients.add(Ingredient(1.0, "pza", line.trim()))
                }
            }
        }

        val gson = Gson()
        // Usamos la URI seleccionada o una imagen por defecto
        val finalImageUrl = selectedImageUri?.toString() ?: "https://images.unsplash.com/photo-1495521821757-a1efb6729352?q=80&w=500&auto=format&fit=crop"

        val recipeEntity = RecipeEntity(
            title = name,
            ingredientsJson = gson.toJson(ingredients),
            stepsJson = gson.toJson(steps),
            imageUrl = finalImageUrl,
            basePortions = 1,
            totalTimeMinutes = steps.sumOf { it.timerDurationMinutes ?: 0 },
            isDiscovery = false
        )

        lifecycleScope.launch {
            db.recipeDao().insertRecipe(recipeEntity)
            runOnUiThread {
                Toast.makeText(this@NewRecipeActivity, "¡Receta guardada!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
