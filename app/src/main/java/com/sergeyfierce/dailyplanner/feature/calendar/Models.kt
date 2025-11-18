package com.sergeyfierce.dailyplanner.feature.calendar

import java.time.LocalDate
import java.time.LocalTime

enum class CalendarMode { CALENDAR, DAY }

enum class TaskType { POINT, INTERVAL }

data class DayTask(
    val id: Long,
    val title: String,
    val type: TaskType,
    val startTime: LocalTime,
    val endTime: LocalTime?,
    val isDone: Boolean = false
)

data class CalendarUiState(
    val selectedDate: LocalDate,
    val mode: CalendarMode,
    val dayTasks: List<DayTask>,
    val tasksByDate: Map<LocalDate, List<DayTask>>,
    val dpPerMinute: Float
)

data class TaskLayoutInfo(
    val task: DayTask,
    val top: Float,
    val height: Float,
    val column: Int,
    val columns: Int
)
