package com.example.habitbloombasic.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.habitbloombasic.R
import com.example.habitbloombasic.data.HabitUi
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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
    // Средний стрик (нормируем к цели 21 день)
    val avgStreak = if (state.items.isEmpty()) 0f else state.items.map { it.streak }.average().toFloat()
    val targetDays = 21f
    val progress = min(1f, avgStreak / targetDays)

    // Плавная анимация при изменении прогресса
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "flowerProgress"
    )

    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // Цветок сверху
            Flower(
                progress = animProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .aspectRatio(1f)
            )

            if (state.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_hint))
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.items) { it ->
                        RowItem(
                            it,
                            onToggle = { onToggle(it.id) },
                            onDelete = { onDelete(it.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
    if (state.showDialog) {
        AddDialog(
            title = state.newTitle,
            onTitleChange = onTitleChange,
            onDismiss = onDialogDismiss,
            onSave = onAddConfirm
        )
    }
}

@Composable
private fun RowItem(item: HabitUi, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text("Стрик: ${item.streak}", style = MaterialTheme.typTypography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.checked_today))
                    Switch(checked = item.checkedToday, onCheckedChange = { onToggle() })
                }
                TextButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}

/** Цветок: растёт по progress [0f..1f] — больше лепестков/радиус/листья. */
@Composable
private fun Flower(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val d = size.minDimension
        val c = Offset(size.width / 2f, size.height / 2f)

        // Стебель — выше при росте
        val stemW = d * 0.04f
        val stemH = d * (0.30f + 0.18f * progress)
        drawRoundRect(
            color = Color(0xFF4CAF50),
            topLeft = Offset(c.x - stemW / 2f, c.y + d * 0.10f),
            size = Size(stemW, stemH),
            cornerRadius = CornerRadius(stemW / 2f)
        )

        // Кол-во лепестков и их радиус увеличиваются
        val petals = (6 + (progress * 6f)).toInt().coerceIn(6, 12)
        val petalRadius = d * (0.07f + 0.07f * progress)
        val ringR = d * (0.20f + 0.12f * progress)

        // Цвет лепестков становится насыщеннее
        val petalColor = Color(
            red = 0xFF,
            green = (0x70 + (0x1A * progress)).toInt().coerceAtMost(0xFF),
            blue = (0x55 + (0x10 * progress)).toInt().coerceAtMost(0xFF),
            alpha = 0xFF
        )

        repeat(petals) { i ->
            val ang = (2 * PI * i / petals).toFloat()
            val px = c.x + cos(ang) * ringR
            val py = c.y + sin(ang) * ringR
            drawCircle(color = petalColor, radius = petalRadius, center = Offset(px, py))
        }

        // Сердцевина — чуть растёт
        drawCircle(color = Color(0xFFFFC107), radius = d * (0.10f + 0.05f * progress), center = c)

        // Листья: один при progress>0.2, второй при >0.6
        val leafH = d * 0.10f
        val leafW = d * 0.20f
        if (progress > 0.2f) {
            drawRoundRect(
                color = Color(0xFF66BB6A),
                topLeft = Offset(c.x + stemW * 0.2f, c.y + d * 0.24f),
                size = Size(leafW, leafH),
                cornerRadius = CornerRadius(leafH / 2f)
            )
        }
        if (progress > 0.6f) {
            drawRoundRect(
                color = Color(0xFF66BB6A),
                topLeft = Offset(c.x - stemW - leafW, c.y + d * 0.28f),
                size = Size(leafW, leafH),
                cornerRadius = CornerRadius(leafH / 2f)
            )
        }
    }
}
