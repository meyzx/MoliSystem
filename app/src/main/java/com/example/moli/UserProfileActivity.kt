package com.example.moli

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileActivity : AppCompatActivity() {

    private lateinit var etUserName: TextInputEditText
    private lateinit var ivUserPhoto: ShapeableImageView
    private lateinit var tvDisplayName: TextView
    private lateinit var tvStatTotal: TextView
    private lateinit var tvStatCreadas: TextView
    private lateinit var tvStatTiempo: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: AppDatabase
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivUserPhoto.setImageURI(uri)
            ivUserPhoto.imageTintList = null   // remove default tint once real photo is set
            ivUserPhoto.setPadding(0, 0, 0, 0) // fill entire circle
            contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        db = AppDatabase.getDatabase(this)
        sharedPreferences = getSharedPreferences("MoliPrefs", Context.MODE_PRIVATE)

        // ── Bind views ──────────────────────────────────────────
        etUserName     = findViewById(R.id.etUserName)
        ivUserPhoto    = findViewById(R.id.ivUserPhoto)
        tvDisplayName  = findViewById(R.id.tvDisplayName)
        tvStatTotal    = findViewById(R.id.tvStatTotal)
        tvStatCreadas  = findViewById(R.id.tvStatCreadas)
        tvStatTiempo   = findViewById(R.id.tvStatTiempo)

        val btnSave   = findViewById<MaterialButton>(R.id.btnSaveProfile)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val btnBack   = findViewById<ImageButton>(R.id.btnBackFromProfile)
        val ivEdit    = findViewById<ImageView>(R.id.ivEditPhoto)

        // ── Cargar datos guardados ───────────────────────────────
        val savedName     = sharedPreferences.getString("user_name", "") ?: ""
        val savedPhotoUri = sharedPreferences.getString("user_photo", null)

        etUserName.setText(savedName)
        updateDisplayName(savedName)

        if (savedPhotoUri != null) {
            val uri = Uri.parse(savedPhotoUri)
            ivUserPhoto.setImageURI(uri)
            ivUserPhoto.imageTintList = null
            ivUserPhoto.setPadding(0, 0, 0, 0)
        }

        // ── Cargar estadísticas desde la BD ─────────────────────
        loadStats()

        // ── Abrir selector de foto ───────────────────────────────
        val openPicker = {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardProfilePhoto)
            .setOnClickListener { openPicker() }
        ivEdit.setOnClickListener { openPicker() }

        // ── Guardar cambios ──────────────────────────────────────
        btnSave.setOnClickListener {
            val name = etUserName.text?.toString()?.trim() ?: ""
            if (name.isNotEmpty()) {
                saveProfile(name)
            } else {
                Toast.makeText(this, "Por favor, ingresa un nombre", Toast.LENGTH_SHORT).show()
            }
        }

        // ── Cerrar sesión (limpia perfil) ────────────────────────
        btnLogout.setOnClickListener {
            sharedPreferences.edit().remove("user_name").remove("user_photo").apply()
            etUserName.setText("")
            updateDisplayName("")
            ivUserPhoto.setImageResource(R.drawable.ic_mex_profile)
            ivUserPhoto.imageTintList =
                android.content.res.ColorStateList.valueOf(getColor(R.color.moli_terracota))
            val p = (16 * resources.displayMetrics.density).toInt()
            ivUserPhoto.setPadding(p, p, p, p)
            selectedImageUri = null
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener { finish() }
    }

    // ── Helpers ─────────────────────────────────────────────────

    private fun updateDisplayName(name: String) {
        tvDisplayName.text = if (name.isNotEmpty()) name else "Tu nombre"
    }

    private fun saveProfile(name: String) {
        sharedPreferences.edit().apply {
            putString("user_name", name)
            selectedImageUri?.let { putString("user_photo", it.toString()) }
            apply()
        }
        updateDisplayName(name)
        Toast.makeText(this, "¡Perfil actualizado, Chef $name! 🍳", Toast.LENGTH_SHORT).show()
    }

    private fun loadStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            val total    = db.recipeDao().countAll()
            val creadas  = db.recipeDao().countUserCreated()
            val minutos  = db.recipeDao().totalCookingMinutes()
            withContext(Dispatchers.Main) {
                tvStatTotal.text   = total.toString()
                tvStatCreadas.text = creadas.toString()
                tvStatTiempo.text  = minutos.toString()
            }
        }
    }
}
