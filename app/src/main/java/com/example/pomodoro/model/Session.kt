package com.example.pomodoro.model

data class Session(
    val id: Long,
    val taskName: String,
    val completedAt: Long,
    val durationMinutes: Int
)