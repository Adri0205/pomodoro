package com.example.pomodoro.storage

import com.example.pomodoro.model.Session
import com.example.pomodoro.model.Task

data class AppData(

    val tasks: List<Task> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val timerEndTime: Long = 0L,
    val timerRunning: Boolean = false,
    val pomodoroMinutes: Int = 25
)