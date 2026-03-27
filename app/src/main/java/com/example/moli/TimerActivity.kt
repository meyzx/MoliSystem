package com.example.moli

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class TimerActivity : AppCompatActivity() {

    private lateinit var tvTimerDisplay: TextView
    private lateinit var etMinutes: EditText
    private lateinit var etSeconds: EditText
    private lateinit var layoutTimePicker: LinearLayout
    private lateinit var cardTimerDisplay: MaterialCardView
    private lateinit var btnStart: Button
    private lateinit var btnReset: Button

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var timerRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        tvTimerDisplay = findViewById(R.id.tvTimerDisplay)
        etMinutes = findViewById(R.id.etTimerMinutes)
        etSeconds = findViewById(R.id.etTimerSeconds)
        layoutTimePicker = findViewById(R.id.layoutTimePicker)
        cardTimerDisplay = findViewById(R.id.cardTimerDisplay)
        btnStart = findViewById(R.id.btnStartTimer)
        btnReset = findViewById(R.id.btnResetTimer)

        findViewById<ImageButton>(R.id.btnBackFromTimer).setOnClickListener {
            finish()
        }

        btnStart.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                if (countDownTimer == null) {
                    setupAndStartNewTimer()
                } else {
                    startTimer()
                }
            }
        }

        btnReset.setOnClickListener {
            resetToInputMode()
        }
    }

    private fun setupAndStartNewTimer() {
        val minStr = etMinutes.text.toString()
        val secStr = etSeconds.text.toString()

        val minutes = if (minStr.isNotEmpty()) minStr.toLong() else 0L
        val seconds = if (secStr.isNotEmpty()) secStr.toLong() else 0L

        if (minutes == 0L && seconds == 0L) {
            Toast.makeText(this, "Ingresa un tiempo válido", Toast.LENGTH_SHORT).show()
            return
        }

        timeLeftInMillis = (minutes * 60 + seconds) * 1000
        
        // Cambiar a modo cuenta regresiva
        layoutTimePicker.visibility = View.GONE
        cardTimerDisplay.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
        
        startTimer()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timerRunning = false
                btnStart.text = "¡LISTO!"
                btnStart.isEnabled = false
            }
        }.start()

        timerRunning = true
        btnStart.text = "PAUSA"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        btnStart.text = "CONTINUAR"
    }

    private fun resetToInputMode() {
        countDownTimer?.cancel()
        countDownTimer = null
        timerRunning = false
        
        timeLeftInMillis = 0
        layoutTimePicker.visibility = View.VISIBLE
        cardTimerDisplay.visibility = View.GONE
        btnReset.visibility = View.GONE
        
        btnStart.text = "INICIAR"
        btnStart.isEnabled = true
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        tvTimerDisplay.text = timeLeftFormatted
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
