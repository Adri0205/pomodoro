package com.example.pomodoro

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.ItemTaskBinding
import com.example.pomodoro.model.Task
import com.example.pomodoro.viewmodel.MainViewModel


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
    }

    private fun observeViewModel() {

        viewModel.tasks.observe(this) { tasks ->

            redrawTasks(tasks)
            updateSummary()
        }

        viewModel.remainingTime.observe(this) {

            val minutes = it / 1000 / 60
            val seconds = (it / 1000) % 60
            binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
        }

        viewModel.progress.observe(this){

            binding.progressTimer.progress = it
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