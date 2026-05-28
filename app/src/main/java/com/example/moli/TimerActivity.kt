package com.example.moli

import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar

class TimerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var adapter: TimerAdapter

    private val timers = mutableListOf<TimerData>()
    private val countDownTimers = HashMap<Int, CountDownTimer>()
    private var nextId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        recyclerView = findViewById(R.id.recyclerTimers)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val animator = DefaultItemAnimator()
        animator.supportsChangeAnimations = false
        recyclerView.itemAnimator = animator

        adapter = TimerAdapter(
            timers = timers,
            onStartPause = ::toggleTimer,
            onReset = ::resetTimer,
            onDelete = ::deleteTimer
        )
        recyclerView.adapter = adapter

        addNewTimer()

        findViewById<ImageButton>(R.id.btnBackFromTimer).setOnClickListener { finish() }
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddTimer).setOnClickListener {
            addNewTimer()
            recyclerView.smoothScrollToPosition(timers.size - 1)
        }
    }

    private fun addNewTimer() {
        val id = nextId++
        timers.add(TimerData(id = id, label = "Temporizador $id"))
        adapter.notifyItemInserted(timers.size - 1)
        updateEmptyState()
    }

    private fun toggleTimer(timer: TimerData) = when (timer.state) {
        TimerState.IDLE -> startTimer(timer)
        TimerState.RUNNING -> pauseTimer(timer)
        TimerState.PAUSED -> resumeTimer(timer)
        TimerState.FINISHED -> Unit
    }

    private fun startTimer(timer: TimerData) {
        val total = (timer.inputMinutes * 60L + timer.inputSeconds) * 1000L
        if (total == 0L) {
            Snackbar.make(recyclerView, "Ingresa un tiempo mayor a 0", Snackbar.LENGTH_SHORT).show()
            return
        }
        timer.totalMillis = total
        timer.timeLeftMillis = total
        timer.state = TimerState.RUNNING
        adapter.refreshTimer(timer.id)
        launchCountDown(timer)
    }

    private fun pauseTimer(timer: TimerData) {
        countDownTimers.remove(timer.id)?.cancel()
        timer.state = TimerState.PAUSED
        adapter.refreshTimer(timer.id)
    }

    private fun resumeTimer(timer: TimerData) {
        timer.state = TimerState.RUNNING
        adapter.refreshTimer(timer.id)
        launchCountDown(timer)
    }

    private fun resetTimer(timer: TimerData) {
        countDownTimers.remove(timer.id)?.cancel()
        timer.state = TimerState.IDLE
        timer.totalMillis = 0
        timer.timeLeftMillis = 0
        adapter.refreshTimer(timer.id)
    }

    private fun deleteTimer(timer: TimerData) {
        countDownTimers.remove(timer.id)?.cancel()
        val removedIndex = adapter.removeTimer(timer.id)
        updateEmptyState()

        Snackbar.make(recyclerView, "${timer.label} eliminado", Snackbar.LENGTH_LONG)
            .setAction("Deshacer") {
                val restoreIndex = removedIndex.coerceIn(0, timers.size)
                timers.add(restoreIndex, timer)
                adapter.notifyItemInserted(restoreIndex)
                updateEmptyState()
            }
            .show()
    }

    private fun launchCountDown(timer: TimerData) {
        countDownTimers.remove(timer.id)?.cancel()
        val countdown = object : CountDownTimer(timer.timeLeftMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timer.timeLeftMillis = millisUntilFinished
                adapter.tickTimer(timer.id)
            }

            override fun onFinish() {
                timer.timeLeftMillis = 0
                timer.state = TimerState.FINISHED
                countDownTimers.remove(timer.id)
                adapter.refreshTimer(timer.id)
                playFinishSound()
                Snackbar.make(recyclerView, "¡${timer.label} terminó!", Snackbar.LENGTH_LONG).show()
            }
        }.start()
        countDownTimers[timer.id] = countdown
    }

    private fun playFinishSound() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(applicationContext, uri)?.play()
        } catch (_: Exception) {}
    }

    private fun updateEmptyState() {
        layoutEmptyState.visibility = if (timers.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimers.values.forEach { it.cancel() }
    }
}
