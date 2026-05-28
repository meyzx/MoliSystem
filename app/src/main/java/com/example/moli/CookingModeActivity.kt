package com.example.moli

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.Locale

class CookingModeActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var currentStepIndex = 0
    private var recipe: Recipe? = null
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var tts: TextToSpeech? = null

    private lateinit var tvStepDescription: TextView
    private lateinit var tvStepNumber: TextView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var btnNext: MaterialButton
    private lateinit var btnPrev: MaterialButton
    private lateinit var timerContainer: LinearLayout
    private lateinit var tvTimerCountdown: TextView
    private lateinit var btnStartStepTimer: MaterialButton
    private lateinit var btnSpeakStep: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cooking_mode)

        recipe = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("RECIPE_DATA", Recipe::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("RECIPE_DATA") as? Recipe
        }
        tts = TextToSpeech(this, this)

        tvStepDescription = findViewById(R.id.tvStepDescription)
        tvStepNumber = findViewById(R.id.tvStepNumber)
        progressIndicator = findViewById(R.id.cookingProgress)
        btnNext = findViewById(R.id.btnNextStep)
        btnPrev = findViewById(R.id.btnPrevStep)
        timerContainer = findViewById(R.id.timerContainer)
        tvTimerCountdown = findViewById(R.id.tvTimerCountdown)
        btnStartStepTimer = findViewById(R.id.btnStartStepTimer)
        btnSpeakStep = findViewById(R.id.btnSpeakStep)

        btnNext.setOnClickListener {
            val stepsSize = recipe?.steps?.size ?: 0
            if (currentStepIndex < stepsSize - 1) {
                currentStepIndex++
                updateUI()
            } else {
                finish()
            }
        }

        btnPrev.setOnClickListener {
            if (currentStepIndex > 0) {
                currentStepIndex--
                updateUI()
            }
        }

        findViewById<ImageButton>(R.id.btnBackFromCooking).setOnClickListener {
            finish()
        }

        btnStartStepTimer.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                val currentStep = recipe?.steps?.get(currentStepIndex)
                currentStep?.timerDurationMinutes?.let { minutes ->
                    startTimer(minutes * 60 * 1000L)
                }
            }
        }

        btnSpeakStep.setOnClickListener {
            speakCurrentStep()
        }

        updateUI()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("es-MX"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "El idioma español no está disponible")
            }
        } else {
            Log.e("TTS", "Error en la inicialización de TTS")
        }
    }

    private fun speakCurrentStep() {
        val text = tvStepDescription.text.toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun updateUI() {
        val steps = recipe?.steps ?: return
        val currentStep = steps[currentStepIndex]

        tvStepDescription.text = currentStep.description
        tvStepNumber.text = "PASO ${currentStepIndex + 1} DE ${steps.size}"
        
        val progress = ((currentStepIndex + 1).toFloat() / steps.size.toFloat() * 100).toInt()
        progressIndicator.setProgress(progress, true)
        
        btnPrev.isEnabled = currentStepIndex > 0
        btnNext.text = if (currentStepIndex == steps.size - 1) "FINALIZAR" else "SIGUIENTE"

        // Detener voz al cambiar de paso si se desea, o dejar que el usuario la active
        tts?.stop()

        // Manejo del Timer
        stopTimer()
        if (currentStep.timerDurationMinutes != null) {
            timerContainer.visibility = View.VISIBLE
            updateTimerText(currentStep.timerDurationMinutes * 60 * 1000L)
            btnStartStepTimer.text = "INICIAR TIMER"
        } else {
            timerContainer.visibility = View.GONE
        }
    }

    private fun startTimer(durationMillis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished)
            }

            override fun onFinish() {
                tvTimerCountdown.text = "00:00"
                btnStartStepTimer.text = "¡LISTO!"
                isTimerRunning = false
                // Opcional: Avisar con voz que el tiempo terminó
                tts?.speak("¡El tiempo del paso ha terminado!", TextToSpeech.QUEUE_FLUSH, null, "")
            }
        }.start()
        
        btnStartStepTimer.text = "DETENER"
        isTimerRunning = true
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        val currentStep = recipe?.steps?.getOrNull(currentStepIndex)
        currentStep?.timerDurationMinutes?.let {
            updateTimerText(it * 60 * 1000L)
        }
        btnStartStepTimer.text = "INICIAR TIMER"
    }

    private fun updateTimerText(millisUntilFinished: Long) {
        val minutes = (millisUntilFinished / 1000) / 60
        val seconds = (millisUntilFinished / 1000) % 60
        tvTimerCountdown.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
