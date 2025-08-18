package com.example.habitbloombasic.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.habitbloombasic.R
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    // Список предопределённых привычек
    val predefinedHabits = listOf(
        "Уборка комнаты",
        "Мытьё окон",
        "Вынос мусора",
        "Чистка зубов",
        "Помыть голову",
        "Прогулка на свежем воздухе",
        "Чтение книги"
        // Добавьте больше, если нужно
    )

    var expanded by remember { mutableStateOf(false) }  // Состояние меню (открыто/закрыто)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_habit)) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),  // Обновлённая версия
                        label = { Text(stringResource(R.string.habit_title)) },
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        predefinedHabits.forEach { habit ->
                            DropdownMenuItem(
                                text = { Text(habit) },
                                onClick = {
                                    onTitleChange(habit)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}