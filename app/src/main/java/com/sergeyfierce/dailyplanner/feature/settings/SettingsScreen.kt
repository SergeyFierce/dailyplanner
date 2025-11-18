package com.sergeyfierce.dailyplanner.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButtonRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class ThemeOption(val title: String) { System("Системная"), Light("Светлая"), Dark("Тёмная") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var themeOption by remember { mutableStateOf(ThemeOption.System) }

    Scaffold(topBar = { TopAppBar(title = { Text("Настройки") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Тема")
            SegmentedButtonRow {
                ThemeOption.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = option == themeOption,
                        onClick = { themeOption = option },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeOption.entries.size)
                    ) {
                        Text(option.title)
                    }
                }
            }
            Text(text = "Здесь будут настройки напоминаний, валюты, языка и т.д.")
        }
    }
}
