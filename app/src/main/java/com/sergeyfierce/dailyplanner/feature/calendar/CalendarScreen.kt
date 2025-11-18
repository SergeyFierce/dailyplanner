package com.sergeyfierce.dailyplanner.feature.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var taskForEdit by remember { mutableStateOf<DayTask?>(null) }

    Scaffold(
        floatingActionButton = {
            if (state.mode == CalendarMode.DAY) {
                FloatingAddButton { viewModel.addPointTask((LocalTime.now().toSecondOfDay() / 60)) }
            }
        },
        topBar = {
            CalendarTopBar(
                state = state,
                onTitleClick = { showDatePicker = true },
                onZoomClick = viewModel::toggleZoom,
                onTodayClick = { viewModel.onDateSelected(LocalDate.now()) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CalendarModeSwitcher(current = state.mode, onModeSelected = viewModel::onModeChange)
            when (state.mode) {
                CalendarMode.CALENDAR -> MonthModeContent(state, onDateSelect = viewModel::onDateSelected, onTaskClick = { taskForEdit = it }, onShiftMonth = viewModel::shiftMonth)
                CalendarMode.DAY -> DayModeContent(state, onAddTask = viewModel::addPointTask, onTaskLongPress = { taskForEdit = it })
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerStateWithDate(state.selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.selectFromPicker(date)
                    }
                    showDatePicker = false
                }) {
                    Text(text = "Выбрать")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    taskForEdit?.let { task ->
        TaskEditDialog(
            task = task,
            onDismiss = { taskForEdit = null },
            onSave = { updated ->
                viewModel.updateTask(updated)
                taskForEdit = null
            },
            onToggleDone = {
                viewModel.toggleTaskDone(task)
                taskForEdit = null
            },
            onDelete = {
                viewModel.deleteTask(task)
                taskForEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(
    state: CalendarUiState,
    onTitleClick: () -> Unit,
    onZoomClick: () -> Unit,
    onTodayClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column(modifier = Modifier.pointerInput(Unit) { detectTapGestures(onTap = { onTitleClick() }) }) {
                Text(text = formatDateTitle(state.selectedDate))
                Text(
                    text = "Дневной план", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(onClick = onTodayClick) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
            }
            IconButton(onClick = onZoomClick) {
                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = null)
            }
        }
    )
}

@Composable
private fun CalendarModeSwitcher(current: CalendarMode, onModeSelected: (CalendarMode) -> Unit) {
    val modes = listOf(CalendarMode.CALENDAR, CalendarMode.DAY)
    val titles = listOf("Календарь", "День")
    TabRow(selectedTabIndex = modes.indexOf(current)) {
        modes.forEachIndexed { index, mode ->
            Tab(
                selected = mode == current,
                onClick = { onModeSelected(mode) },
                text = { Text(titles[index]) }
            )
        }
    }
}

@Composable
private fun MonthModeContent(
    state: CalendarUiState,
    onDateSelect: (LocalDate) -> Unit,
    onTaskClick: (DayTask) -> Unit,
    onShiftMonth: (Long) -> Unit
) {
    val currentMonth = YearMonth.from(state.selectedDate)
    Column(modifier = Modifier.fillMaxSize()) {
        MonthHeader(month = currentMonth, onPrev = { onShiftMonth(-1) }, onNext = { onShiftMonth(1) })
        MonthGrid(
            month = currentMonth,
            selectedDate = state.selectedDate,
            tasksByDate = state.tasksByDate,
            onDateSelect = onDateSelect
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        DayTaskList(tasks = state.tasksByDate[state.selectedDate].orEmpty(), onTaskClick = onTaskClick)
    }
}

@Composable
private fun DayModeContent(
    state: CalendarUiState,
    onAddTask: (Int) -> Unit,
    onTaskLongPress: (DayTask) -> Unit
) {
    val layout = remember(state.dayTasks, state.dpPerMinute) {
        TimelineLayoutCalculator.layout(state.dayTasks, state.dpPerMinute)
    }
    DayTimeline(
        tasks = layout,
        dpPerMinute = state.dpPerMinute,
        onBackgroundTap = onAddTask,
        onTaskLongPress = onTaskLongPress
    )
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        Text(
            text = month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")) + " " + month.year,
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onNext) { Icon(Icons.Filled.ArrowForward, contentDescription = null) }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate, List<DayTask>>,
    onDateSelect: (LocalDate) -> Unit
) {
    val firstDayOfMonth = month.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    val dates = (1..daysInMonth).map { day -> month.atDay(day) }
    val leadingEmpty = if (firstDayOfWeek == 0) 6 else firstDayOfWeek - 1
    val gridItems = List(leadingEmpty) { null } + dates

    Column(modifier = Modifier.fillMaxWidth()) {
        val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            weekDays.forEach { Text(text = it, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)) }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.heightIn(max = 360.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(gridItems) { date ->
                if (date == null) {
                    Spacer(modifier = Modifier.size(40.dp))
                } else {
                    val hasTasks = tasksByDate[date].orEmpty().isNotEmpty()
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    DayCell(
                        date = date,
                        isSelected = isSelected,
                        isToday = isToday,
                        hasTasks = hasTasks,
                        onClick = { onDateSelect(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasTasks: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = when {
        isSelected -> colorScheme.primaryContainer
        else -> colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val textColor = when {
        isSelected -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSurface
    }
    Column(
        modifier = Modifier
            .padding(4.dp)
            .size(44.dp)
            .background(background, RoundedCornerShape(12.dp))
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = date.dayOfMonth.toString(), color = textColor, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium)
        if (hasTasks) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(6.dp)
                    .background(colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
private fun DayTaskList(tasks: List<DayTask>, onTaskClick: (DayTask) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Задачи дня", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(tasks.size) { index ->
                val task = tasks[index]
                TaskCard(task = task, onClick = { onTaskClick(task) })
            }
        }
    }
}

@Composable
private fun TaskCard(task: DayTask, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                )
                Text(
                    text = formatTaskTime(task),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
            if (task.isDone) {
                Text(text = "Готово", color = colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayTimeline(
    tasks: List<TaskLayoutInfo>,
    dpPerMinute: Float,
    onBackgroundTap: (Int) -> Unit,
    onTaskLongPress: (DayTask) -> Unit
) {
    val scrollState = rememberScrollState()
    val trackHeightDp = (24 * 60 * dpPerMinute).dp
    val timeColumnWidth = 64.dp
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val initialScrollPx = with(density) { (8 * 60 * dpPerMinute).dp.toPx().toInt() }

    LaunchedEffect(Unit) {
        scope.launch { scrollState.scrollTo(initialScrollPx) }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.width(timeColumnWidth)) {
            for (hour in 0..24) {
                Box(
                    modifier = Modifier.height((60 * dpPerMinute).dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    if (hour < 24) {
                        Text(
                            text = "%02d:00".format(hour),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeightDp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .pointerInput(dpPerMinute) {
                    detectTapGestures { offset ->
                        val pixelsPerMinute = dpPerMinute * density.density
                        val minute = (offset.y / pixelsPerMinute).toInt()
                        onBackgroundTap(minute)
                    }
                }
        ) {
            HourLines(dpPerMinute = dpPerMinute)
            tasks.forEach { info ->
                TaskBlock(info = info, dpPerMinute = dpPerMinute, onLongPress = onTaskLongPress)
            }
        }
    }
}

@Composable
private fun HourLines(dpPerMinute: Float) {
    Column(modifier = Modifier.fillMaxSize()) {
        val color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        for (hour in 0..24) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((60 * dpPerMinute).dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun TaskBlock(info: TaskLayoutInfo, dpPerMinute: Float, onLongPress: (DayTask) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val widthFraction = 1f / info.columns
    val leftPadding = 8.dp + (info.column * widthFraction * 0.9f).dp
    val background = if (info.task.isDone) colorScheme.surfaceVariant else colorScheme.primaryContainer
    val contentColor = if (info.task.isDone) colorScheme.onSurfaceVariant else colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .padding(top = info.top.dp, start = leftPadding, end = 8.dp)
            .fillMaxWidth(widthFraction)
            .height(info.height.dp)
            .background(background, RoundedCornerShape(12.dp))
            .border(1.dp, colorScheme.outline, RoundedCornerShape(12.dp))
            .pointerInput(info.task.id) {
                detectTapGestures(onLongPress = { onLongPress(info.task) })
            }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = formatTaskTime(info.task),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = info.task.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = contentColor,
                textDecoration = if (info.task.isDone) TextDecoration.LineThrough else null
            )
        }
    }
}

@Composable
private fun TaskEditDialog(
    task: DayTask,
    onDismiss: () -> Unit,
    onSave: (DayTask) -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit
) {
    var title by remember(task) { mutableStateOf(task.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onToggleDone() }) { Text(if (task.isDone) "Вернуть" else "Выполнено") }
                Button(onClick = { onSave(task.copy(title = title)) }) { Text("Сохранить") }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDelete) { Text("Удалить") }
                Button(onClick = onDismiss) { Text("Отмена") }
            }
        },
        title = { Text(text = "Задача") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true
                )
                Text(text = formatTaskTime(task), style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}

@Composable
private fun FloatingAddButton(onClick: () -> Unit) {
    androidx.compose.material3.FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = null)
    }
}

@Composable
private fun formatDateTitle(date: LocalDate): String {
    val today = LocalDate.now()
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
    val formattedDate = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.FULL, Locale("ru"))} ${date.year}"
    return if (date == today) "Сегодня, $dayOfWeek, $formattedDate" else "$dayOfWeek, $formattedDate"
}

@Composable
private fun formatTaskTime(task: DayTask): String {
    return when (task.type) {
        TaskType.POINT -> task.startTime.toString()
        TaskType.INTERVAL -> "${task.startTime} – ${task.endTime}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberDatePickerStateWithDate(date: LocalDate): androidx.compose.material3.DatePickerState {
    val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return rememberDatePickerState(initialSelectedDateMillis = millis)
}
