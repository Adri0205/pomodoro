package com.example.pomodoro.timer

import android.os.CountDownTimer

class PomodoroTimer(
    private val onTick: (Long) -> Unit,
    private val onFinish: () -> Unit
) {
    private var timer: CountDownTimer? = null

    var endTime: Long = 0L
        private set

    var isRunning = false
        private set

    fun start(duration: Long) {

        endTime = System.currentTimeMillis() + duration
        startInternal(duration)
    }

    fun resume() {

        val remaining = endTime - System.currentTimeMillis()

        if (remaining > 0) {
            startInternal(remaining)

        } else {
            onFinish()
        }
    }

    fun pause() {

        timer?.cancel()
        isRunning = false
    }

    fun stop() {

        timer?.cancel()
        isRunning = false
        endTime = 0
    }

    fun getRemainingTime(): Long {

        return (endTime - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    private fun startInternal(duration: Long) {

        timer?.cancel()
        isRunning = true
        timer = object : CountDownTimer(duration, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                onTick(millisUntilFinished)
            }

            override fun onFinish() {

                isRunning = false
                endTime = 0
                onFinish()
            }
        }
        timer?.start()
    }
}