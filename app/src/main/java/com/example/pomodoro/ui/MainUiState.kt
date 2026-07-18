package com.example.pomodoro.ui

import com.example.pomodoro.model.Session
import com.example.pomodoro.model.Task
import com.example.pomodoro.utils.Constants

data class MainUiState(

    val tasks: List<Task> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val remainingTime: Long = Constants.DEFAULT_POMODORO_MILLIS,
    val progress: Int = 100,
    val timerRunning: Boolean = false
)