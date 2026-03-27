package com.example.moli

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.Locale

class CookingModeActivity : AppCompatActivity() {

    private var currentStepIndex = 0
    private var recipe: Recipe? = null
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false

    private lateinit var tvStepDescription: TextView
    private lateinit var tvStepNumber: TextView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var btnNext: MaterialButton
    private lateinit var btnPrev: MaterialButton
    private lateinit var timerContainer: LinearLayout
    private lateinit var tvTimerCountdown: TextView
    private lateinit var btnStartStepTimer: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cooking_mode)

        recipe = intent.getSerializableExtra("RECIPE_DATA") as? Recipe

        tvStepDescription = findViewById(R.id.tvStepDescription)
        tvStepNumber = findViewById(R.id.tvStepNumber)
        progressIndicator = findViewById(R.id.cookingProgress)
        btnNext = findViewById(R.id.btnNextStep)
        btnPrev = findViewById(R.id.btnPrevStep)
        timerContainer = findViewById(R.id.timerContainer)
        tvTimerCountdown = findViewById(R.id.tvTimerCountdown)
        btnStartStepTimer = findViewById(R.id.btnStartStepTimer)

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

        updateUI()
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

        // Manejo del Timer
        stopTimer() // Detener cualquier timer previo al cambiar de paso
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
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
