package com.example.moli

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

enum class TimerState { IDLE, RUNNING, PAUSED, FINISHED }

data class TimerData(
    val id: Int,
    var label: String,
    var totalMillis: Long = 0L,
    var timeLeftMillis: Long = 0L,
    var state: TimerState = TimerState.IDLE,
    var inputMinutes: Int = 5,
    var inputSeconds: Int = 0
)

class TimerAdapter(
    private val timers: MutableList<TimerData>,
    private val onStartPause: (TimerData) -> Unit,
    private val onReset: (TimerData) -> Unit,
    private val onDelete: (TimerData) -> Unit
) : RecyclerView.Adapter<TimerAdapter.TimerViewHolder>() {

    companion object {
        private const val PAYLOAD_TICK = "tick"
    }

    inner class TimerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val tvTimerName: TextView = view.findViewById(R.id.tvTimerName)
        val tvTimerStatus: TextView = view.findViewById(R.id.tvTimerStatus)
        val tvTimeDisplay: TextView = view.findViewById(R.id.tvTimeDisplay)
        val progressTimer: CircularProgressIndicator = view.findViewById(R.id.progressTimer)
        val layoutTimePicker: LinearLayout = view.findViewById(R.id.layoutTimePicker)
        val etMinutes: TextInputEditText = view.findViewById(R.id.etTimerMinutes)
        val etSeconds: TextInputEditText = view.findViewById(R.id.etTimerSeconds)
        val btnStartPause: MaterialButton = view.findViewById(R.id.btnStartPause)
        val btnReset: MaterialButton = view.findViewById(R.id.btnResetTimer)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteTimer)

        var minutesWatcher: TextWatcher? = null
        var secondsWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timer, parent, false)
        return TimerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads.contains(PAYLOAD_TICK)) {
            val timer = timers[position]
            holder.tvTimeDisplay.text = formatTime(timer.timeLeftMillis)
            holder.progressTimer.setProgressCompat(calcProgress(timer), true)
            if (timer.state == TimerState.FINISHED) bindFull(holder, timer)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        bindFull(holder, timers[position])
    }

    private fun bindFull(holder: TimerViewHolder, timer: TimerData) {
        val ctx = holder.itemView.context

        // Remove stale text watchers before setting text
        holder.minutesWatcher?.let { holder.etMinutes.removeTextChangedListener(it) }
        holder.secondsWatcher?.let { holder.etSeconds.removeTextChangedListener(it) }

        holder.tvTimerName.text = timer.label

        val displayMillis = if (timer.state == TimerState.IDLE)
            (timer.inputMinutes * 60L + timer.inputSeconds) * 1000L
        else timer.timeLeftMillis

        holder.tvTimeDisplay.text = formatTime(displayMillis)
        holder.progressTimer.setProgressCompat(calcProgress(timer), false)

        // Show input only when idle
        holder.layoutTimePicker.visibility = if (timer.state == TimerState.IDLE) View.VISIBLE else View.GONE

        if (timer.state == TimerState.IDLE) {
            holder.etMinutes.setText(timer.inputMinutes.toString())
            holder.etSeconds.setText(String.format(Locale.getDefault(), "%02d", timer.inputSeconds))
        }

        // State-specific visuals
        when (timer.state) {
            TimerState.IDLE -> {
                holder.btnStartPause.text = "INICIAR"
                holder.btnStartPause.isEnabled = true
                holder.btnReset.visibility = View.GONE
                holder.tvTimerStatus.text = ""
                holder.tvTimeDisplay.setTextColor(ctx.getColor(R.color.moli_terracota))
                holder.progressTimer.setIndicatorColor(ctx.getColor(R.color.moli_terracota))
                holder.card.strokeColor = ctx.getColor(R.color.moli_terracota)
            }
            TimerState.RUNNING -> {
                holder.btnStartPause.text = "PAUSAR"
                holder.btnStartPause.isEnabled = true
                holder.btnReset.visibility = View.VISIBLE
                holder.tvTimerStatus.text = "▶ Corriendo"
                holder.tvTimeDisplay.setTextColor(ctx.getColor(R.color.moli_terracota))
                holder.progressTimer.setIndicatorColor(ctx.getColor(R.color.moli_terracota))
                holder.card.strokeColor = ctx.getColor(R.color.moli_terracota)
            }
            TimerState.PAUSED -> {
                holder.btnStartPause.text = "CONTINUAR"
                holder.btnStartPause.isEnabled = true
                holder.btnReset.visibility = View.VISIBLE
                holder.tvTimerStatus.text = "⏸ Pausado"
                holder.tvTimeDisplay.setTextColor(ctx.getColor(R.color.gray_dark))
                holder.progressTimer.setIndicatorColor(ctx.getColor(R.color.gray_dark))
                holder.card.strokeColor = ctx.getColor(R.color.gray_dark)
            }
            TimerState.FINISHED -> {
                holder.btnStartPause.text = "✓ LISTO"
                holder.btnStartPause.isEnabled = false
                holder.btnReset.visibility = View.VISIBLE
                holder.tvTimerStatus.text = "¡Terminó!"
                holder.tvTimeDisplay.setTextColor(ctx.getColor(R.color.moli_verde))
                holder.progressTimer.setIndicatorColor(ctx.getColor(R.color.moli_verde))
                holder.card.strokeColor = ctx.getColor(R.color.moli_verde)
            }
        }

        // Click listeners
        holder.btnStartPause.setOnClickListener { onStartPause(timer) }
        holder.btnReset.setOnClickListener { onReset(timer) }
        holder.btnDelete.setOnClickListener { onDelete(timer) }

        // TextWatchers for live preview when idle
        val minutesWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pos = holder.adapterPosition
                if (pos == RecyclerView.NO_POSITION) return
                val t = timers.getOrNull(pos) ?: return
                if (t.state != TimerState.IDLE) return
                val min = s?.toString()?.toIntOrNull()?.coerceIn(0, 99) ?: 0
                t.inputMinutes = min
                holder.tvTimeDisplay.text = formatTime((min * 60L + t.inputSeconds) * 1000L)
            }
        }
        val secondsWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pos = holder.adapterPosition
                if (pos == RecyclerView.NO_POSITION) return
                val t = timers.getOrNull(pos) ?: return
                if (t.state != TimerState.IDLE) return
                val sec = s?.toString()?.toIntOrNull()?.coerceIn(0, 59) ?: 0
                t.inputSeconds = sec
                holder.tvTimeDisplay.text = formatTime((t.inputMinutes * 60L + sec) * 1000L)
            }
        }
        holder.etMinutes.addTextChangedListener(minutesWatcher)
        holder.etSeconds.addTextChangedListener(secondsWatcher)
        holder.minutesWatcher = minutesWatcher
        holder.secondsWatcher = secondsWatcher
    }

    private fun calcProgress(timer: TimerData): Int {
        return when {
            timer.state == TimerState.IDLE -> 100
            timer.totalMillis > 0 ->
                ((timer.timeLeftMillis.toFloat() / timer.totalMillis) * 100).toInt().coerceIn(0, 100)
            else -> 0
        }
    }

    fun tickTimer(timerId: Int) {
        val index = timers.indexOfFirst { it.id == timerId }
        if (index >= 0) notifyItemChanged(index, PAYLOAD_TICK)
    }

    fun refreshTimer(timerId: Int) {
        val index = timers.indexOfFirst { it.id == timerId }
        if (index >= 0) notifyItemChanged(index)
    }

    fun removeTimer(timerId: Int): Int {
        val index = timers.indexOfFirst { it.id == timerId }
        if (index >= 0) {
            timers.removeAt(index)
            notifyItemRemoved(index)
        }
        return index
    }

    fun insertTimer(index: Int, timer: TimerData) {
        timers.add(index.coerceIn(0, timers.size), timer)
        notifyItemInserted(index.coerceIn(0, timers.size - 1))
    }

    private fun formatTime(millis: Long): String {
        val totalSec = millis / 1000
        return String.format(Locale.getDefault(), "%02d:%02d", totalSec / 60, totalSec % 60)
    }

    override fun getItemCount() = timers.size
}
