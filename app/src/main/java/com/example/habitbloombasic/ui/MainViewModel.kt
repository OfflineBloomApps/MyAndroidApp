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

/**
 * The main screen displays the user's list of habits and a dynamic flower
 * that grows as the user checks off healthy activities. The flower's
 * size, number of petals and color are driven by the user's level and
 * progress. When the user levels up or misses several days, a
 * motivational dialog appears.
 */
@Composable
fun MainScreen(
    state: UiState,
    onToggle: (Int) -> Unit,
    onAddClick: () -> Unit,
    onDelete: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onAddConfirm: () -> Unit,
    onDialogDismiss: () -> Unit,
    onMessageDismiss: () -> Unit
) {
    // Animate progress for smoother transitions
    val animProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "animateProgress"
    )

    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // Draw the flower at the top
            Flower(
                level = state.level,
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
    // Show motivational message if present
    state.message?.let { msg ->
        AlertDialog(
            onDismissRequest = onMessageDismiss,
            confirmButton = {
                TextButton(onClick = onMessageDismiss) {
                    Text(stringResource(R.string.ok))
                }
            },
            text = { Text(msg) }
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
                Text("Стрик: ${item.streak}", style = MaterialTheme.typography.bodySmall)
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

/**
 * Draw the flower using Canvas. The appearance is controlled by the user's
 * level and progress:
 *  - Higher levels add more petals and increase the overall size.
 *  - Progress within the current level increases petal size and stem height.
 *  - If progress is low (i.e., the user missed several days), the
 *    petals become paler to indicate that the flower needs attention.
 */
@Composable
private fun Flower(level: Int, progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val d = size.minDimension
        val c = Offset(size.width / 2f, size.height / 2f)

        // Stem: height increases slightly with progress
        val stemW = d * 0.04f
        val stemH = d * (0.30f + 0.18f * progress)
        drawRoundRect(
            color = Color(0xFF4CAF50),
            topLeft = Offset(c.x - stemW / 2f, c.y + d * 0.10f),
            size = Size(stemW, stemH),
            cornerRadius = CornerRadius(stemW / 2f)
        )

        // Determine petals: base 6 petals plus one extra per two levels
        val petals = (6 + (level / 2)).coerceIn(6, 14)
        // Petal size grows with level and progress
        val petalRadius = d * (0.07f + 0.05f * progress + 0.02f * (level - 1))
        val ringR = d * (0.20f + 0.10f * progress + 0.02f * (level - 1))

        // Petal color becomes more vibrant with progress; fade when progress is low
        val baseColor = Color(0xFFFF8A65)
        val fadeFactor = if (progress < 0.3f) 0.3f + progress else 1f
        val petalColor = Color(
            red = baseColor.red * fadeFactor,
            green = baseColor.green * fadeFactor,
            blue = baseColor.blue * fadeFactor,
            alpha = 1f
        )

        repeat(petals) { i ->
            val ang = (2 * PI * i / petals).toFloat()
            val px = c.x + cos(ang) * ringR
            val py = c.y + sin(ang) * ringR
            drawCircle(
                color = petalColor,
                radius = petalRadius,
                center = Offset(px, py)
            )
        }

        // Center of the flower
        val centerRadius = d * (0.10f + 0.05f * progress + 0.02f * (level - 1))
        drawCircle(color = Color(0xFFFFC107), radius = centerRadius, center = c)

        // Leaves: two leaves appear gradually; second appears when progress > 0.7
        val leafW = d * 0.25f
        val leafH = d * 0.12f
        val leafColor = Color(0xFF66BB6A)
        if (progress > 0.3f) {
            drawRoundRect(
                color = leafColor,
                topLeft = Offset(c.x + stemW * 0.2f, c.y + d * 0.24f),
                size = Size(leafW, leafH),
                cornerRadius = CornerRadius(leafH / 2f)
            )
        }
        if (progress > 0.7f) {
            drawRoundRect(
                color = leafColor,
                topLeft = Offset(c.x - stemW - leafW, c.y + d * 0.28f),
                size = Size(leafW, leafH),
                cornerRadius = CornerRadius(leafH / 2f)
            )
        }
    }
}