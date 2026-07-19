package com.example.pomodoro

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.ItemSessionBinding
import com.example.pomodoro.databinding.ItemTaskBinding
import com.example.pomodoro.model.Session
import com.example.pomodoro.model.Task
import com.example.pomodoro.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        binding.btnAddTask.setOnClickListener {

            val title = binding.etTask.text.toString().trim()

            if (title.isEmpty()) {
                binding.etTask.error = "Ingrese una tarea"
                return@setOnClickListener
            }

            viewModel.addTask(title)
            binding.etTask.text?.clear()
        }

        binding.btnStart.setOnClickListener {

            viewModel.startTimer()
        }

        binding.btnPause.setOnClickListener {

            viewModel.pauseTimer()
        }

        binding.btnResume.setOnClickListener {

            viewModel.resumeTimer()
        }

        binding.btnRestart.setOnClickListener {

            viewModel.restartTimer()
        }

        binding.btnSetDuration.setOnClickListener {
            val minutesStr = binding.etDuration.text.toString()
            if (minutesStr.isNotEmpty()) {
                val minutes = minutesStr.toInt()
                if (minutes > 0) {
                    viewModel.setPomodoroMinutes(minutes)
                    binding.etDuration.text?.clear()
                    Toast.makeText(this, "Duración actualizada a $minutes min", Toast.LENGTH_SHORT).show()
                } else {
                    binding.etDuration.error = "Mínimo 1 min"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTimer()
    }

    private fun observeViewModel() {

        viewModel.uiState.observe(this) { state ->

            redrawTasks(state.tasks)
            redrawSessions(state.sessions)
            updateSummary()

            val minutes = state.remainingTime / 1000 / 60
            val seconds = (state.remainingTime / 1000) % 60

            binding.tvTimer.text =
                String.format("%02d:%02d", minutes, seconds)

            binding.progressTimer.progress =
                state.progress
        }

        viewModel.timerFinishedEvent.observe(this) { finished ->
            if (finished == true) {
                Toast.makeText(this, "¡Pomodoro finalizado!", Toast.LENGTH_LONG).show()
                viewModel.onTimerFinishedEventHandled()
            }
        }
    }

    private fun redrawTasks(tasks: List<Task>) {

        binding.layoutTasks.removeAllViews()

        binding.tvNoTasks.visibility =
            if (tasks.isEmpty()) View.VISIBLE else View.GONE

        tasks.forEach { task ->

            val itemBinding = ItemTaskBinding.inflate(layoutInflater)
            itemBinding.tvTaskTitle.text = task.title
            itemBinding.checkCompleted.setOnCheckedChangeListener(null)
            itemBinding.checkCompleted.isChecked = task.completed
            itemBinding.checkCompleted.setOnCheckedChangeListener { _, checked -> viewModel.completeTask(task, checked)
            }

            itemBinding.btnDeleteTask.setOnClickListener {

                viewModel.deleteTask(task)
            }

            itemBinding.taskContainer.setOnClickListener {

                if (!task.completed) {
                    viewModel.selectTask(task)
                }
            }

            updateTaskAppearance(itemBinding, task)
            binding.layoutTasks.addView(itemBinding.root)
        }
    }

    private fun redrawSessions(sessions: List<Session>) {

        binding.layoutSessions.removeAllViews()

        binding.tvNoSessions.visibility =
            if (sessions.isEmpty()) View.VISIBLE else View.GONE

        sessions.asReversed().forEach { session ->

            val itemBinding = ItemSessionBinding.inflate(layoutInflater)
            itemBinding.tvSessionTask.text = session.taskName
            itemBinding.tvSessionDuration.text = "Duración: ${session.durationMinutes} minutos"
            
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            itemBinding.tvSessionDate.text = sdf.format(Date(session.completedAt))

            binding.layoutSessions.addView(itemBinding.root)
        }
    }

    private fun updateTaskAppearance(
        itemBinding: ItemTaskBinding,
        task: Task
    ) {

        when {

            task.completed -> {

                itemBinding.taskContainer.setBackgroundResource(R.drawable.bg_task_completed)
                itemBinding.tvTaskTitle.paintFlags = itemBinding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                itemBinding.tvTaskTitle.alpha = 0.5f
            }

            task.selected -> {

                itemBinding.taskContainer.setBackgroundResource(R.drawable.bg_task_selected)
                itemBinding.tvTaskTitle.paintFlags = itemBinding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                itemBinding.tvTaskTitle.alpha = 1f
            }

            else -> {

                itemBinding.taskContainer.setBackgroundResource(R.drawable.bg_task)
                itemBinding.tvTaskTitle.paintFlags = itemBinding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                itemBinding.tvTaskTitle.alpha = 1f
            }
        }
    }

    private fun updateSummary() {

        binding.tvPendingTasks.text = "Pendientes: ${viewModel.getPendingTasks()}"
        binding.tvCompletedSessions.text = "Sesiones completadas: ${viewModel.getCompletedSessions()}"
    }
}