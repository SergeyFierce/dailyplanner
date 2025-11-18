package com.sergeyfierce.dailyplanner.feature.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotesScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Заметки") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Экран заметок (в разработке). Здесь в будущем будут списки/карточки заметок.",
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
