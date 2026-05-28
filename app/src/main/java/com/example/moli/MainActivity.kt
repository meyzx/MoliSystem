package com.example.moli

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var rvRecipes: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var etSearch: EditText
    private lateinit var chipGroupFilters: ChipGroup
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var tvSectionTitle: TextView
    private lateinit var tvEmptyStateTitle: TextView
    private lateinit var tvEmptyStateMessage: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var db: AppDatabase

    private var allRecipes = mutableListOf<Recipe>()
    private var selectedCategory: String = "Todas"
    private var isDiscoveryMode: Boolean = true

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private val RECORD_AUDIO_REQUEST_CODE = 101
    private val WAKE_WORD = "moli"
    private var isAlwaysListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)
        ensureAudioPermission()
        initViews()
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupNavigation()

        initVoiceAssistant()
        observeRecipes()
        updateGreeting()
    }

    private fun ensureAudioPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        }
    }

    private fun initViews() {
        rvRecipes = findViewById(R.id.rvRecipes)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        tvSectionTitle = findViewById(R.id.tvSectionTitle)
        tvEmptyStateTitle = emptyStateContainer.findViewById(R.id.tvEmptyStateTitle)
        tvEmptyStateMessage = emptyStateContainer.findViewById(R.id.tvEmptyStateMessage)
        tvGreeting = findViewById(R.id.tvGreeting)

        findViewById<FloatingActionButton>(R.id.fabAddNewRecipe).setOnClickListener {
            startActivity(Intent(this, NewRecipeActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnNotifications).setOnClickListener {
            toggleAlwaysListening()
        }
    }

    private fun setupRecyclerView() {
        rvRecipes.layoutManager = LinearLayoutManager(this)
        adapter = RecipeAdapter(mutableListOf()) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_DATA", recipe)
            startActivity(intent)
        }
        rvRecipes.adapter = adapter
    }

    private fun setupSearch() {
        etSearch = findViewById(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        chipGroupFilters = findViewById(R.id.chipGroupFilters)
        chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            selectedCategory = if (checkedId != null) {
                group.findViewById<Chip>(checkedId).text.toString()
            } else {
                "Todas"
            }
            applyFilters()
        }
    }

    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    isDiscoveryMode = true
                    tvSectionTitle.text = "Descubrir Recetas"
                    applyFilters()
                    true
                }
                R.id.nav_my_recipes -> {
                    isDiscoveryMode = false
                    tvSectionTitle.text = "Mis Creaciones"
                    applyFilters()
                    true
                }
                R.id.nav_timer -> {
                    startActivity(Intent(this, TimerActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
        if (isAlwaysListening) startListening()
    }

    private fun updateGreeting() {
        val prefs = getSharedPreferences("MoliPrefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "")
        tvGreeting.text = if (!userName.isNullOrEmpty()) "¡Hola, Chef $userName!" else "¡Hola, Chef!"
    }

    private fun toggleAlwaysListening() {
        isAlwaysListening = !isAlwaysListening
        if (isAlwaysListening) {
            Toast.makeText(this, "Modo Manos Libres Activo", Toast.LENGTH_SHORT).show()
            startListening()
        } else {
            speechRecognizer?.stopListening()
            Toast.makeText(this, "Modo Manos Libres Desactivado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initVoiceAssistant() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val heardText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.lowercase() ?: ""
                    if (heardText.contains(WAKE_WORD)) processVoiceCommand(heardText)
                    if (isAlwaysListening) Handler(Looper.getMainLooper()).postDelayed({ startListening() }, 500)
                }
                override fun onError(error: Int) {
                    if (isAlwaysListening) startListening()
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        tts = TextToSpeech(this, this)
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
        }
        speechRecognizer?.startListening(intent)
    }

    private fun processVoiceCommand(command: String) {
        val cleanCommand = command.replace(WAKE_WORD, "").trim()
        if (cleanCommand.isEmpty()) { speak("¿Sí? Dime."); return }

        when {
            cleanCommand.contains("buscar") -> {
                val q = cleanCommand.replace("buscar", "").trim()
                etSearch.setText(q)
                speak("Buscando $q")
            }
            cleanCommand.contains("recetas") -> {
                findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_my_recipes
            }
            else -> askClaude(cleanCommand)
        }
    }

    private fun askClaude(prompt: String) {
        speak("Déjame pensar...")
        Thread {
            try {
                val url = URL("https://api.anthropic.com/v1/messages")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("x-api-key", "TU_API_KEY_AQUI")
                connection.setRequestProperty("anthropic-version", "2023-06-01")
                connection.setRequestProperty("content-type", "application/json")
                connection.doOutput = true

                val body = """{"model":"claude-sonnet-4-6","max_tokens":1024,"messages":[{"role":"user","content":"$prompt"}]}"""
                connection.outputStream.write(body.toByteArray())
                connection.outputStream.flush()

                val response = connection.inputStream.bufferedReader().readText()
                val texto = JSONObject(response)
                    .optJSONArray("content")?.optJSONObject(0)?.optString("text")
                    ?: "No pude obtener respuesta"

                runOnUiThread { speak(texto) }
            } catch (e: Exception) {
                runOnUiThread { speak("No pude conectarme, intenta de nuevo.") }
            }
        }.start()
    }

    private fun speak(text: String) = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")

    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts?.setLanguage(Locale.forLanguageTag("es-MX")) }

    private fun observeRecipes() {
        lifecycleScope.launch {
            db.recipeDao().getAllRecipesFlow().collectLatest { entities ->
                val converters = Converters()
                val recipes = entities.map { entity ->
                    Recipe(
                        title = entity.title,
                        ingredients = converters.toIngredientList(entity.ingredientsJson),
                        steps = converters.toStepList(entity.stepsJson),
                        imageUrl = entity.imageUrl,
                        basePortions = entity.basePortions,
                        totalTimeMinutes = entity.totalTimeMinutes,
                        category = entity.category,
                        isDiscovery = entity.isDiscovery
                    )
                }
                allRecipes.clear()
                allRecipes.addAll(recipes)
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val query = etSearch.text.toString()
        val filteredList = allRecipes.filter { recipe ->
            val matchesSearch = query.isEmpty() ||
                    recipe.title.contains(query, ignoreCase = true) ||
                    recipe.ingredients.any { it.name.contains(query, ignoreCase = true) }
            val matchesCategory = query.isNotEmpty() || selectedCategory == "Todas" || recipe.category == selectedCategory
            val belongsToTab = recipe.isDiscovery == isDiscoveryMode
            matchesSearch && matchesCategory && belongsToTab
        }

        if (filteredList.isEmpty()) {
            rvRecipes.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
        } else {
            rvRecipes.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
        }
        adapter.updateList(filteredList)
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }
}