package com.example.moli

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class TimerActivity : AppCompatActivity() {

    private lateinit var tvTimerDisplay: TextView
    private lateinit var btnStart: Button
    private lateinit var btnReset: Button
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 900000 // 15 minutos por defecto
    private var timerRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        tvTimerDisplay = findViewById(R.id.tvTimerDisplay)
        btnStart = findViewById(R.id.btnStartTimer)
        btnReset = findViewById(R.id.btnResetTimer)

        findViewById<ImageButton>(R.id.btnBackFromTimer).setOnClickListener {
            finish()
        }

        btnStart.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }

        updateCountDownText()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timerRunning = false
                btnStart.text = "INICIAR"
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

    private fun resetTimer() {
        timeLeftInMillis = 900000
        updateCountDownText()
        pauseTimer()
        btnStart.text = "INICIAR"
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        tvTimerDisplay.text = timeLeftFormatted
    }
}