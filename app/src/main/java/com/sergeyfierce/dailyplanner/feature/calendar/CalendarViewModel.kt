package com.sergeyfierce.dailyplanner.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class CalendarViewModel(
    private val repository: TaskRepository = InMemoryTaskRepository()
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val mode = MutableStateFlow(CalendarMode.DAY)
    private val dpPerMinute = MutableStateFlow(1.4f)

    val uiState = combine(
        selectedDate,
        mode,
        repository.tasks,
        dpPerMinute
    ) { date, currentMode, tasks, density ->
        CalendarUiState(
            selectedDate = date,
            mode = currentMode,
            dayTasks = tasks[date].orEmpty(),
            tasksByDate = tasks,
            dpPerMinute = density
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CalendarUiState(
            selectedDate = LocalDate.now(),
            mode = CalendarMode.DAY,
            dayTasks = repository.tasks.value[LocalDate.now()].orEmpty(),
            tasksByDate = repository.tasks.value,
            dpPerMinute = dpPerMinute.value
        )
    )

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
    }

    fun onModeChange(mode: CalendarMode) {
        this.mode.value = mode
    }

    fun toggleZoom() {
        val next = when {
            dpPerMinute.value < 1.2f -> 1.2f
            dpPerMinute.value < 1.6f -> 1.6f
            dpPerMinute.value < 2.2f -> 2.2f
            else -> 1.0f
        }
        dpPerMinute.value = next
    }

    fun addPointTask(atMinute: Int) {
        val minute = TimelineLayoutCalculator.clampMinute(atMinute)
        val date = selectedDate.value
        val time = LocalTime.of(minute / 60, minute % 60)
        val task = DayTask(
            id = System.currentTimeMillis(),
            title = "Новая задача",
            type = TaskType.POINT,
            startTime = time,
            endTime = null
        )
        repository.upsert(date, task)
    }

    fun updateTask(task: DayTask) {
        repository.upsert(selectedDate.value, task)
    }

    fun toggleTaskDone(task: DayTask) {
        updateTask(task.copy(isDone = !task.isDone))
    }

    fun deleteTask(task: DayTask) {
        repository.delete(selectedDate.value, task.id)
    }

    fun shiftMonth(offset: Long) {
        selectedDate.value = selectedDate.value.plusMonths(offset).withDayOfMonth(1)
    }

    fun selectFromPicker(date: LocalDate) {
        selectedDate.value = date
    }

    fun adjustTaskTime(task: DayTask, start: LocalTime, end: LocalTime?) {
        val updated = task.copy(
            startTime = start,
            endTime = when (task.type) {
                TaskType.POINT -> null
                TaskType.INTERVAL -> end ?: start.plus(1, ChronoUnit.HOURS)
            }
        )
        updateTask(updated)
    }
}
