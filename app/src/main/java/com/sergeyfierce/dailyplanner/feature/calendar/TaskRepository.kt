package com.sergeyfierce.dailyplanner.feature.calendar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.max

interface TaskRepository {
    val tasks: StateFlow<Map<LocalDate, List<DayTask>>>
    fun upsert(date: LocalDate, task: DayTask)
    fun delete(date: LocalDate, taskId: Long)
}

class InMemoryTaskRepository : TaskRepository {
    private val _tasks = MutableStateFlow<Map<LocalDate, List<DayTask>>>(sampleTasks())
    override val tasks: StateFlow<Map<LocalDate, List<DayTask>>> = _tasks

    override fun upsert(date: LocalDate, task: DayTask) {
        _tasks.value = _tasks.value.toMutableMap().apply {
            val dayList = (this[date] ?: emptyList()).toMutableList()
            val index = dayList.indexOfFirst { it.id == task.id }
            if (index >= 0) {
                dayList[index] = task
            } else {
                dayList.add(task)
            }
            dayList.sortBy { it.startTime }
            this[date] = dayList
        }
    }

    override fun delete(date: LocalDate, taskId: Long) {
        _tasks.value = _tasks.value.toMutableMap().apply {
            val dayList = (this[date] ?: emptyList()).filterNot { it.id == taskId }
            this[date] = dayList
        }
    }

    private fun sampleTasks(): Map<LocalDate, List<DayTask>> {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        return mapOf(
            today to listOf(
                DayTask(1L, "Утренняя пробежка", TaskType.INTERVAL, LocalTime.of(7, 0), LocalTime.of(7, 45)),
                DayTask(2L, "Команда: планирование", TaskType.INTERVAL, LocalTime.of(9, 30), LocalTime.of(10, 30)),
                DayTask(3L, "Обед", TaskType.POINT, LocalTime.of(13, 0), null),
                DayTask(4L, "Код ревью", TaskType.INTERVAL, LocalTime.of(15, 0), LocalTime.of(16, 0), isDone = true),
                DayTask(5L, "Вечерняя прогулка", TaskType.INTERVAL, LocalTime.of(18, 30), LocalTime.of(19, 30))
            ),
            tomorrow to listOf(
                DayTask(6L, "Звонок с партнёрами", TaskType.INTERVAL, LocalTime.of(11, 0), LocalTime.of(12, 0)),
                DayTask(7L, "Купить продукты", TaskType.POINT, LocalTime.of(19, 15), null)
            )
        )
    }
}

fun DayTask.durationMinutes(): Int {
    return when (type) {
        TaskType.POINT -> 45
        TaskType.INTERVAL -> max(30, ((endTime ?: startTime).toSecondOfDay() - startTime.toSecondOfDay()) / 60)
    }
}
