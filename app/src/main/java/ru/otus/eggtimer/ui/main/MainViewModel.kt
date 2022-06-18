package ru.otus.eggtimer.ui.main

import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.otus.eggtimer.R

class MainViewModel : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Default(""))
    val timerState = _timerState.asStateFlow()

    private var timer: CountDownTimer = createTimer(EggState.Soft)

    private var alarm: Int = 0
    private var soundPool: SoundPool? = null

    // === Vibration ===
    private var vibrator: Vibrator? = null
    private val vibratorPattern = longArrayOf(
        960, 125, 85, 125, 690,
        125, 85, 125, 690,
        125, 85, 125, 690,
        125, 85, 125, 690,
        125, 85, 125, 690
    )
    private var vibratorEffect: VibrationEffect? = null
    // === Vibration ===

    fun initSound(fileDescriptor: AssetFileDescriptor) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
            .apply {
                alarm = load(fileDescriptor, 1)
            }
    }

    // === Vibration ===
    fun initVibrator(vibrator: Vibrator) {
        this.vibrator = vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibratorEffect = VibrationEffect.createWaveform(vibratorPattern, -1)
        }
    }
    // === Vibration ===

    fun onItemSelected(itemId: Int) {
        if (_timerState.value is TimerState.Running) return
        when (itemId) {
            R.id.action_soft -> installTimer(EggState.Soft)
            R.id.action_medium -> installTimer(EggState.Medium)
            R.id.action_hard -> installTimer(EggState.Hard)
        }
    }

    private fun installTimer(state: EggState) {
        timer.cancel()
        timer = createTimer(state)
    }

    private fun createTimer(state: EggState): CountDownTimer {
        _timerState.value = TimerState.Default(getTime(state.time))
        return object : CountDownTimer(state.time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timerState.value = TimerState.Running(getTime(millisUntilFinished))
            }

            override fun onFinish() {
                _timerState.value = TimerState.Done
                if (alarm > 0) {
                    soundPool?.play(alarm, 1f, 1f, 1, 0, 1f)
                }

                // === Vibration ===
                vibrator?.let { vibrator ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(vibratorEffect)
                    } else {
                        vibrator.vibrate(vibratorPattern, -1)
                    }
                }
                // === Vibration ===
            }
        }
    }

    private fun getTime(millis: Long): String {
        val seconds = millis / 1000
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
    }

    fun onStartBtnClick() {
        if (_timerState.value is TimerState.Running) {
            timer.cancel()
            _timerState.value = TimerState.Done
        } else {
            timer.start()
        }
        soundPool?.stop(alarm)
        // === Vibration ===
        vibrator?.cancel()
    }
}