package com.example.pomodoro.utils

object Constants {

    const val DEFAULT_POMODORO_MINUTES = 25
    const val DEFAULT_POMODORO_MILLIS = DEFAULT_POMODORO_MINUTES * 60 * 1000L
    const val DATASTORE_NAME = "pomodoro_preferences"
    const val TASKS_KEY = "tasks"
    const val SESSIONS_KEY = "sessions"
    const val TIMER_END_TIME_KEY = "timer_end_time"
}