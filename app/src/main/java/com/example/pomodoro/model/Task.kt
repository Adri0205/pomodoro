package com.example.pomodoro.model

data class Task(
    val id: Long,
    val title: String,
    val completed: Boolean = false,
    val selected: Boolean = false
)