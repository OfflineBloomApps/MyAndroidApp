package com.example.habitbloombasic.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.habitbloombasic.R
import com.example.habitbloombasic.data.HabitUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

/**
 * Main screen of the HabitBloom application. This composable displays current
 * XP, last check‑in date, a flower that grows based on XP, and a list of habits
 * that can be toggled and removed. A floating action button allows users to
 * add new habits via a dialog.
 */
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
        topBar = { TopAppBar(title = { Text(stringResource(id = R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display XP and last mark date. If no marks yet, show "Нет".
            val lastMarkText = state.lastMarkDate?.let {
                LocalDate.ofEpochDay(it).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            } ?: stringResource(id = R.string.no_date)
            Text(
                text = stringResource(id = R.string.xp_and_last_mark, state.xp, lastMarkText),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )

            // Flower grows based on XP. See [Flower] for drawing details.
            Flower(xp = state.xp)

            // List of habits with checkboxes and delete buttons.
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.habits) { habit ->
                    HabitItem(habit, onToggle, onDelete)
                }
            }

            // Show add dialog when requested.
            if (state.showAddDialog) {
                AddDialog(
                    title = state.newTitle,
                    onTitleChange = onTitleChange,
                    onDismiss = onDialogDismiss,
                    onSave = onAddConfirm
                )
            }

            // Display transient message via Snackbar.
            state.message?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { /* message cleared by VM */ }) {
                            Text(stringResource(id = R.string.ok))
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    }
}

/**
 * Row representing a single habit. Shows a checkbox for toggling the habit,
 * the title, current streak, and a delete icon.
 */
@Composable
fun HabitItem(habit: HabitUi, onToggle: (Int) -> Unit, onDelete: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = habit.checkedToday, onCheckedChange = { onToggle(habit.id) })
        Text(
            text = habit.title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        Text(
            text = stringResource(id = R.string.streak_format, habit.streak),
            modifier = Modifier.padding(start = 8.dp)
        )
        IconButton(onClick = { onDelete(habit.id) }) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete))
        }
    }
}

/**
 * Draws a simple potted flower that grows based on the number of XP points. The
 * flower has multiple stages: a pot and soil are always shown, a stem grows
 * proportionally to the XP goal, leaves appear after 30% and 60% progress, and
 * a blossom appears after 80% progress. The XP goal is currently set to 10
 * unique days of completed habits; adjust [growthGoal] to change how quickly
 * the plant matures.
 *
 * @param xp Number of unique days with at least one habit completed.
 */
@Composable
fun Flower(xp: Int) {
    // Define how many XP points are needed for a fully grown flower.
    val growthGoal = 10f
    val targetProgress = min(xp / growthGoal, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    // Colours for the pot, soil and flower.
    val potColor = Color(0xFF8B4513)       // Brown pot
    val soilColor = Color(0xFF654321)      // Darker soil
    val stemColor = Color(0xFF228B22)      // Green stem
    val leafColor = Color(0xFF2E8B57)      // A different shade of green for leaves
    val blossomColor = Color(0xFFFF6F61)   // Coral blossom

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Dimensions for the pot and soil.
        val potHeight = 40f
        val potWidth = 90f
        val soilHeight = 12f
        val topMargin = 20f

        // Draw pot with rounded corners.
        drawRoundRect(
            color = potColor,
            topLeft = Offset((canvasWidth - potWidth) / 2f, canvasHeight - potHeight),
            size = Size(potWidth, potHeight),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // Draw soil inside the pot.
        val soilWidth = potWidth - 20f
        drawRect(
            color = soilColor,
            topLeft = Offset((canvasWidth - soilWidth) / 2f, canvasHeight - potHeight - soilHeight),
            size = Size(soilWidth, soilHeight)
        )

        // Calculate maximum stem height based on available space.
        val stemMaxHeight = canvasHeight - potHeight - soilHeight - topMargin
        val stemWidth = 8f
        val stemHeight = stemMaxHeight * animatedProgress

        // Draw stem if progress > 0.
        if (animatedProgress > 0f) {
            drawRect(
                color = stemColor,
                topLeft = Offset((canvasWidth - stemWidth) / 2f, canvasHeight - potHeight - soilHeight - stemHeight),
                size = Size(stemWidth, stemHeight)
            )

            // Leaves appear at different progress thresholds.
            if (animatedProgress > 0.3f) {
                val leafSize = Size(40f, 20f)
                drawOval(
                    color = leafColor,
                    topLeft = Offset((canvasWidth - leafSize.width) / 2f - 25f, canvasHeight - potHeight - soilHeight - stemHeight * 0.7f),
                    size = leafSize
                )
            }
            if (animatedProgress > 0.6f) {
                val leafSize = Size(40f, 20f)
                drawOval(
                    color = leafColor,
                    topLeft = Offset((canvasWidth - leafSize.width) / 2f + 25f, canvasHeight - potHeight - soilHeight - stemHeight * 0.4f),
                    size = leafSize
                )
            }

            // Blossom appears near the top when progress is high.
            if (animatedProgress > 0.8f) {
                val blossomRadius = 30f
                drawCircle(
                    color = blossomColor,
                    center = Offset(canvasWidth / 2f, canvasHeight - potHeight - soilHeight - stemHeight - blossomRadius),
                    radius = blossomRadius
                )
            }
        }
    }
}