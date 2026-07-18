package com.example.pomodoro.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pomodoro.model.Session
import com.example.pomodoro.model.Task
import com.example.pomodoro.storage.AppData
import com.example.pomodoro.ui.MainUiState
import com.example.pomodoro.utils.Constants

class MainViewModel : ViewModel() {

    private val _uiState = MutableLiveData(MainUiState())

    val uiState: LiveData<MainUiState>
        get() = _uiState

    private var timer: CountDownTimer? = null

    private var endTime: Long = 0L

    private var remainingMillis = Constants.DEFAULT_POMODORO_MILLIS

    private var totalDurationMillis = Constants.DEFAULT_POMODORO_MILLIS

    private fun updateState(state: MainUiState) {

        _uiState.value = state
    }
    fun addTask(title: String): Boolean {

        if (title.isBlank()) {

            return false
        }

        val current = _uiState.value!!
        val newTasks = current.tasks.toMutableList()

        newTasks.add(Task(id = System.currentTimeMillis(), title = title.trim()))
        updateState(current.copy(tasks = newTasks))

        return true
    }

    fun deleteTask(task: Task) {

        val current = _uiState.value!!
        val newTasks = current.tasks.filter {

                it.id != task.id
            }

        updateState(current.copy(tasks = newTasks))
    }

    fun completeTask(task: Task, completed: Boolean) {

        val current = _uiState.value!!
        val newTasks = current.tasks.map {

                if (it.id == task.id)

                    it.copy(completed = completed, selected = if (completed) false else it.selected)

                else
                    it
            }

        updateState(current.copy(tasks = newTasks))
    }

    fun selectTask(task: Task) {

        val current = _uiState.value!!
        val newTasks = current.tasks.map {

                when {

                    it.completed -> it.copy(selected = false)
                    it.id == task.id -> it.copy(selected = true)
                    else -> it.copy(selected = false)
                }

            }

        updateState(current.copy(tasks = newTasks))
    }

    fun startTimer() {

        if (timer != null) return

        endTime = System.currentTimeMillis() + remainingMillis
        timer = object : CountDownTimer(remainingMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                remainingMillis = millisUntilFinished
                val progress = (millisUntilFinished.toFloat() / totalDurationMillis.toFloat() * 100).toInt()

                updateState(_uiState.value!!.copy(

                        remainingTime = millisUntilFinished,
                        progress = progress,
                        timerRunning = true
                    )
                )
            }

            override fun onFinish() {

                timer = null
                remainingMillis = totalDurationMillis
                registerSession()
            }
        }
        timer?.start()
    }

    fun pauseTimer() {

        timer?.cancel()
        timer = null

        updateState(_uiState.value!!.copy(timerRunning = false))
    }

    fun resumeTimer() {

        if (remainingMillis > 0L) {

            startTimer()
        }
    }

    fun restartTimer() {

        timer?.cancel()
        timer = null
        remainingMillis = Constants.DEFAULT_POMODORO_MILLIS
        updateState(_uiState.value!!.copy(remainingTime = remainingMillis, progress = 100, timerRunning = false))
    }

    private fun registerSession() {

        val current = _uiState.value!!
        val activeTask = current.tasks.find {

            it.selected
        }

        if (activeTask == null) {

            restartTimer()

            return
        }

        val newSessions = current.sessions.toMutableList()

        newSessions.add(Session(

                id = System.currentTimeMillis(),
                taskName = activeTask.title,
                completedAt = System.currentTimeMillis(),
                durationMinutes = Constants.DEFAULT_POMODORO_MINUTES)
        )

        updateState(

            current.copy(
                sessions = newSessions,
                remainingTime = Constants.DEFAULT_POMODORO_MILLIS,
                progress = 100,
                timerRunning = false
            )
        )

        remainingMillis = Constants.DEFAULT_POMODORO_MILLIS
    }
    fun getPendingTasks(): Int {

        return _uiState.value!!.tasks.count {

                !it.completed
            }
    }

    fun getCompletedSessions(): Int {

        return _uiState.value!!.sessions.size
    }

    fun setPomodoroMinutes(minutes: Int) {

        if (minutes <= 0) return

        totalDurationMillis = minutes * 60 * 1000L
        remainingMillis = totalDurationMillis
        updateState(_uiState.value!!.copy(remainingTime = remainingMillis, progress = 100))
    }

    fun restoreTimer() {

        if (endTime == 0L)
            return

        val currentRemaining = endTime - System.currentTimeMillis()

        if (currentRemaining <= 0L) {

            registerSession()
            return
        }

        remainingMillis = currentRemaining

        startTimer()
    }

    fun getSummary(): Pair<Int, Int> {

        val pending = _uiState.value!!.tasks.count {

            !it.completed

                }

        val sessions = _uiState.value!!.sessions.size
        return Pair(pending, sessions)
    }

    override fun onCleared() {

        super.onCleared()
        timer?.cancel()
    }
}