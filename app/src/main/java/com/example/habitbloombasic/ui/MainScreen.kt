package com.example.habitbloombasic.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.habitbloombasic.R
import com.example.habitbloombasic.data.HabitUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: UiState,
    onToggle: (Int) -> Unit,
    onAddClick: () -> Unit,
    onDelete: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onAddConfirm: () -> Unit,
    onDialogDismiss: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Habit Bloom") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            val lastMarkText = state.lastMarkDate?.let {
                LocalDate.ofEpochDay(it).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            } ?: "Нет"
            Text(
                text = "XP: ${state.xp} | Последняя отметка: $lastMarkText",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )

            Flower(progress = min(state.xp / 30f, 1f))  // Рост до 30 XP

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.habits) { habit ->
                    HabitItem(habit, onToggle, onDelete)
                }
            }

            if (state.showAddDialog) {
                AddDialog(
                    title = state.newTitle,
                    onTitleChange = onTitleChange,
                    onDismiss = onDialogDismiss,
                    onSave = onAddConfirm
                )
            }

            state.message?.let {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { /* TODO: Очистка сообщения, если нужно */ }) {
                            Text("OK")
                        }
                    }
                ) { Text(it) }
            }
        }
    }
}

@Composable
fun HabitItem(habit: HabitUi, onToggle: (Int) -> Unit, onDelete: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Checkbox(checked = habit.checkedToday, onCheckedChange = { onToggle(habit.id) })
        Text(habit.title, modifier = Modifier.weight(1f).padding(start = 8.dp))
        Text("Streak: ${habit.streak}", modifier = Modifier.padding(start = 8.dp))
        IconButton(onClick = { onDelete(habit.id) }) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить")
        }
    }
}

@Composable
fun Flower(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp)
    ) {
        val height = size.height * animatedProgress  // Высота стебля растёт
        val width = size.width

        // Стебель (зелёный)
        drawRect(
            color = Color.Green,
            topLeft = Offset(width / 2 - 5f, size.height - height),
            size = Size(10f, height)
        )

        // Листья (появляются на 30% и 60%)
        if (animatedProgress > 0.3f) {
            drawOval(
                color = Color.Green,
                topLeft = Offset(width / 2 - 20f, size.height - height * 0.7f),
                size = Size(40f, 20f)
            )
        }
        if (animatedProgress > 0.6f) {
            drawOval(
                color = Color.Green,
                topLeft = Offset(width / 2 - 20f, size.height - height * 0.4f),
                size = Size(40f, 20f)
            )
        }

        // Цветок (появляется на 90%, красный круг)
        if (animatedProgress > 0.9f) {
            drawCircle(
                color = Color.Red,
                center = Offset(width / 2, size.height - height - 20f),
                radius = 30f
            )
        }
    }
}