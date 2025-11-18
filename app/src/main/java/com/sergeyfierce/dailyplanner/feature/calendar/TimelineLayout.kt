package com.sergeyfierce.dailyplanner.feature.calendar

import kotlin.math.max

object TimelineLayoutCalculator {
    private const val MinutesInDay = 24 * 60

    fun layout(tasks: List<DayTask>, dpPerMinute: Float): List<TaskLayoutInfo> {
        val intervals = tasks.map { task ->
            val start = task.startTime.toSecondOfDay() / 60
            val end = (task.endTime?.toSecondOfDay()?.div(60)) ?: start + task.durationMinutes()
            TaskInterval(task, start, end)
        }.sortedBy { it.start }

        val active = mutableListOf<TaskIntervalWithColumn>()
        val placed = mutableListOf<TaskLayoutInfo>()
        var maxColumn = 0

        for (interval in intervals) {
            active.removeAll { it.end <= interval.start }
            val usedColumns = active.map { it.column }.toSet()
            val column = generateSequence(0) { it + 1 }.first { it !in usedColumns }
            maxColumn = max(maxColumn, column)
            val item = TaskIntervalWithColumn(interval, column)
            active.add(item)
            placed.add(
                TaskLayoutInfo(
                    task = interval.task,
                    top = interval.start * dpPerMinute,
                    height = max(interval.durationMinutes() * dpPerMinute, 44f),
                    column = column,
                    columns = 1 // placeholder, will be updated later
                )
            )
        }

        val totalColumns = maxColumn + 1
        return placed.map { it.copy(columns = totalColumns) }
    }

    fun clampMinute(minute: Int): Int = minute.coerceIn(0, MinutesInDay - 1)
}

private data class TaskInterval(
    val task: DayTask,
    val start: Int,
    val end: Int
) {
    fun durationMinutes(): Int = max( task.durationMinutes(), end - start)
}

private data class TaskIntervalWithColumn(
    val interval: TaskInterval,
    val column: Int
) {
    val task get() = interval.task
    val start get() = interval.start
    val end get() = interval.end
}
