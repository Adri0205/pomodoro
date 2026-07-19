package com.example.pomodoro.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.model.Session
import com.example.pomodoro.model.Task
import com.example.pomodoro.storage.AppData
import com.example.pomodoro.storage.DataStoreManager
import com.example.pomodoro.ui.MainUiState
import com.example.pomodoro.utils.Constants
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)

    private val _uiState = MutableLiveData(MainUiState())

    val uiState: LiveData<MainUiState>
        get() = _uiState

    private val _timerFinishedEvent = MutableLiveData<Boolean>()
    val timerFinishedEvent: LiveData<Boolean> get() = _timerFinishedEvent

    private var timer: CountDownTimer? = null

    private var endTime: Long = 0L

    private var remainingMillis = Constants.DEFAULT_POMODORO_MILLIS

    private var totalDurationMillis = Constants.DEFAULT_POMODORO_MILLIS

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            dataStoreManager.appDataFlow.collect { appData ->
                val current = _uiState.value ?: MainUiState()
                
                // Si el temporizador estaba corriendo, calculamos el tiempo restante real
                var newRemaining = remainingMillis
                var isRunning = appData.timerRunning
                
                if (appData.timerRunning && appData.timerEndTime > System.currentTimeMillis()) {
                    newRemaining = appData.timerEndTime - System.currentTimeMillis()
                } else if (appData.timerRunning && appData.timerEndTime <= System.currentTimeMillis() && appData.timerEndTime != 0L) {
                    // El temporizador terminó mientras la app estaba cerrada
                    isRunning = false
                    newRemaining = appData.pomodoroMinutes * 60 * 1000L
                    // Registramos la sesión pendiente
                    registerSessionInternal(appData.tasks, appData.sessions, appData.pomodoroMinutes)
                } else {
                    newRemaining = appData.pomodoroMinutes * 60 * 1000L
                }

                totalDurationMillis = appData.pomodoroMinutes * 60 * 1000L
                remainingMillis = newRemaining
                endTime = appData.timerEndTime

                updateState(current.copy(
                    tasks = appData.tasks,
                    sessions = appData.sessions,
                    remainingTime = newRemaining,
                    progress = if (totalDurationMillis > 0) (newRemaining.toFloat() / totalDurationMillis * 100).toInt() else 100,
                    timerRunning = isRunning
                ))
                
                if (isRunning && timer == null) {
                    startTimer()
                }
            }
        }
    }

    private fun saveData() {
        val current = _uiState.value ?: return
        viewModelScope.launch {
            dataStoreManager.save(AppData(
                tasks = current.tasks,
                sessions = current.sessions,
                timerEndTime = endTime,
                timerRunning = timer != null,
                pomodoroMinutes = (totalDurationMillis / 60000).toInt()
            ))
        }
    }

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
        saveData()

        return true
    }

    fun deleteTask(task: Task) {

        val current = _uiState.value!!
        val newTasks = current.tasks.filter {

                it.id != task.id
            }

        updateState(current.copy(tasks = newTasks))
        saveData()
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
        saveData()
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
        saveData()
    }

    fun startTimer() {

        if (timer != null) return

        endTime = System.currentTimeMillis() + remainingMillis
        saveData()
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
                _timerFinishedEvent.postValue(true)
                registerSession()
            }
        }
        timer?.start()
    }

    fun pauseTimer() {

        timer?.cancel()
        timer = null

        updateState(_uiState.value!!.copy(timerRunning = false))
        saveData()
    }

    fun resumeTimer() {

        if (remainingMillis > 0L) {

            startTimer()
        }
    }

    fun restartTimer() {

        timer?.cancel()
        timer = null
        remainingMillis = totalDurationMillis
        updateState(_uiState.value!!.copy(remainingTime = remainingMillis, progress = 100, timerRunning = false))
        endTime = 0
        saveData()
    }

    private fun registerSession() {
        val current = _uiState.value!!
        registerSessionInternal(current.tasks, current.sessions, (totalDurationMillis / 60000).toInt())
    }

    private fun registerSessionInternal(tasks: List<Task>, sessions: List<Session>, durationMinutes: Int) {
        val activeTask = tasks.find {
            it.selected
        }

        if (activeTask == null) {
            restartTimer()
            return
        }

        val newSessions = sessions.toMutableList()

        newSessions.add(Session(
            id = System.currentTimeMillis(),
            taskName = activeTask.title,
            completedAt = System.currentTimeMillis(),
            durationMinutes = durationMinutes)
        )

        remainingMillis = durationMinutes * 60 * 1000L
        endTime = 0
        
        val current = _uiState.value!!
        updateState(
            current.copy(
                sessions = newSessions,
                remainingTime = remainingMillis,
                progress = 100,
                timerRunning = false
            )
        )
        saveData()
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

    fun onTimerFinishedEventHandled() {
        _timerFinishedEvent.value = false
    }

    fun refreshTimer() {
        if (timer != null && endTime > 0) {
            val remaining = endTime - System.currentTimeMillis()
            if (remaining <= 0) {
                timer?.cancel()
                timer = null
                remainingMillis = totalDurationMillis
                _timerFinishedEvent.postValue(true)
                registerSession()
            }
        }
    }

    override fun onCleared() {

        super.onCleared()
        timer?.cancel()
    }
}